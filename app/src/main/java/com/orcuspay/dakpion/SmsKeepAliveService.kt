package com.orcuspay.dakpion

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.orcuspay.dakpion.util.NotificationHelper

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

    override fun onCreate() {
        super.onCreate()
        promote()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        promote()
        // If the OEM kills us anyway, ask Android to bring us back.
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
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
