package com.orcuspay.dakpion

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Telephony
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.orcuspay.dakpion.util.BackgroundProtection
import com.orcuspay.dakpion.util.Diag
import com.orcuspay.dakpion.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.FileDescriptor
import java.io.PrintWriter

/**
 * Keeps the Dakpion process alive in the background with a persistent, silent
 * notification — the same approach PipraPay's and OwnPay's companion apps use.
 *
 * Why: on Infinix/Tecno, Xiaomi, Oppo, Vivo the OEM app-killer force-stops
 * background apps, after which the SMS_RECEIVED broadcast never reaches
 * [SmsReceiver] until the app is reopened. A foreground service is the one
 * thing those killers treat as "in use", so the process survives and the
 * receiver fires the moment a payment SMS lands.
 *
 * Started from MainActivity (app open), on boot, and after the app is
 * updated. Declared as `remoteMessaging` (targetSdk 34+ needs a type; this
 * one has no runtime time-limit and may be started from those broadcasts).
 */
class SmsKeepAliveService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val observers = mutableListOf<ContentObserver>()
    private var pendingSync: Job? = null

    override fun onCreate() {
        super.onCreate()
        Diag.init(this)
        Diag.log("service: onCreate")
        promote()
        watchInbox()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        promote()
        watchInbox()
        // If the OEM kills us anyway, ask Android to bring us back.
        return START_STICKY
    }

    override fun onDestroy() {
        Diag.log("service: onDestroy")
        isRunning = false
        observers.forEach { contentResolver.unregisterContentObserver(it) }
        observers.clear()
        pendingSync?.cancel()
        super.onDestroy()
    }

    // The app's task was removed (user cleared it). Stock Android keeps a
    // started service alive through this; some OEMs don't. Re-assert.
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Diag.log("service: onTaskRemoved")
        start(applicationContext)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * `adb shell dumpsys activity service com.dakpion.app/.SmsKeepAliveService`
     * — the remote-debugging view when the ROM hides app logs.
     */
    override fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {
        val w = writer ?: return
        w.println("Dakpion keep-alive")
        w.println("  running=$isRunning observers=${observers.size}")
        val status = BackgroundProtection.status(this)
        w.println("  oem=${status.oem} batteryOptimized=${status.batteryOptimized} bgRestricted=${status.backgroundRestricted}")
        w.println("Recent events (oldest first):")
        Diag.events(this).forEach { w.println("  $it") }
    }

    private fun promote() {
        try {
            val notification = NotificationHelper(applicationContext).buildKeepAliveNotification()
            // remoteMessaging: Android's type for "transferring text messages to
            // another device" — a precise fit, no time limit on Android 15+,
            // allowed from boot/update broadcasts, no Play declaration form.
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING
            } else {
                0
            }
            ServiceCompat.startForeground(
                this,
                NotificationHelper.KEEP_ALIVE_NOTIFICATION_ID,
                notification,
                type,
            )
            isRunning = true
        } catch (e: Exception) {
            // Android 12+ refuses foreground starts from some background
            // contexts; nothing to do but stand down. The receiver path and the
            // periodic sync still work without us.
            Diag.log("service: startForeground refused ${e.javaClass.simpleName}: ${e.message}")
            isRunning = false
            stopSelf()
        }
    }

    /**
     * Second, independent capture path: watch the SMS database itself.
     *
     * SMS_RECEIVED is an *ordered* broadcast — any app registered above us
     * (Truecaller, some OEM spam filters) can abort it and [SmsReceiver] never
     * fires. But the default messaging app still writes the SMS to the inbox,
     * and that write is observable. So while this service is alive, every
     * inbox change triggers a fast sync (debounced), whatever happened to the
     * broadcast.
     */
    private fun watchInbox() {
        if (observers.isNotEmpty()) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Diag.log("service: READ_SMS not granted — inbox observer off")
            return
        }

        // Some providers notify content://sms, others only content://mms-sms.
        val uris = listOf(Telephony.Sms.CONTENT_URI, Telephony.MmsSms.CONTENT_URI)
        uris.forEach { uri ->
            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean, changed: Uri?) {
                    Diag.log("observer: change ${changed ?: uri}")
                    // Several notifications arrive per SMS (insert, thread
                    // update, read flag…). Coalesce them into one sync.
                    pendingSync?.cancel()
                    pendingSync = scope.launch {
                        delay(INBOX_DEBOUNCE_MS)
                        try {
                            val outcome = DakpionApplication.syncer(applicationContext).sync(fast = true)
                            Diag.log("observer: sync outcome=$outcome")
                        } catch (e: Exception) {
                            Diag.log("observer: sync threw ${e.javaClass.simpleName}: ${e.message}")
                        }
                    }
                }
            }
            try {
                contentResolver.registerContentObserver(uri, true, observer)
                observers.add(observer)
            } catch (e: Exception) {
                Diag.log("service: cannot observe $uri: ${e.message}")
            }
        }
        Diag.log("service: inbox observers registered=${observers.size}")
    }

    companion object {
        private const val INBOX_DEBOUNCE_MS = 800L

        /** Best-effort, read by the Device tab to show whether protection is active. */
        @Volatile
        var isRunning: Boolean = false
            private set

        fun start(context: Context) {
            try {
                ContextCompat.startForegroundService(
                    context.applicationContext,
                    Intent(context.applicationContext, SmsKeepAliveService::class.java),
                )
            } catch (e: Exception) {
                // ForegroundServiceStartNotAllowedException and friends: the OS
                // didn't let a background caller start us. Not fatal.
                Diag.log("service: start refused ${e.javaClass.simpleName}: ${e.message}")
            }
        }
    }
}
