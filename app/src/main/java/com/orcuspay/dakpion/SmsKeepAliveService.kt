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
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.orcuspay.dakpion.util.NotificationHelper
import com.orcuspay.dakpion.worker.SmsSyncer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
 * updated. Declared as `specialUse` (targetSdk 34+ needs a type; specialUse
 * has no runtime time-limit and may be started from those broadcasts).
 */
class SmsKeepAliveService : Service() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface KeepAliveEntryPoint {
        fun smsSyncer(): SmsSyncer
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var inboxObserver: ContentObserver? = null
    private var pendingSync: Job? = null

    override fun onCreate() {
        super.onCreate()
        promote()
        watchInbox()
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
        if (inboxObserver != null) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) !=
            PackageManager.PERMISSION_GRANTED
        ) return

        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                // Several notifications arrive per SMS (insert, thread update,
                // read flag…). Coalesce them into one sync shortly after the
                // last one.
                pendingSync?.cancel()
                pendingSync = scope.launch {
                    delay(INBOX_DEBOUNCE_MS)
                    try {
                        EntryPointAccessors
                            .fromApplication(applicationContext, KeepAliveEntryPoint::class.java)
                            .smsSyncer()
                            .sync(fast = true)
                    } catch (e: Exception) {
                        Log.d("kraken", "Inbox-observer sync failed: ${e.message}")
                    }
                }
            }
        }
        try {
            contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true, observer)
            inboxObserver = observer
        } catch (e: Exception) {
            Log.d("kraken", "Could not observe inbox: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        promote()
        // If the OEM kills us anyway, ask Android to bring us back.
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        inboxObserver?.let { contentResolver.unregisterContentObserver(it) }
        inboxObserver = null
        pendingSync?.cancel()
        super.onDestroy()
    }

    // The app's task was removed (user cleared it). Stock Android keeps a
    // started service alive through this; some OEMs don't. Re-assert.
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        start(applicationContext)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun promote() {
        try {
            val notification = NotificationHelper(applicationContext).buildKeepAliveNotification()
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
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
            Log.d("kraken", "Keep-alive service could not start: ${e.message}")
            isRunning = false
            stopSelf()
        }
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
                Log.d("kraken", "Keep-alive start refused: ${e.message}")
            }
        }
    }
}
