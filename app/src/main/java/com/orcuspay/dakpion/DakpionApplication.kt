package com.orcuspay.dakpion

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.orcuspay.dakpion.util.Diag
import com.orcuspay.dakpion.worker.SmsSyncer
import com.orcuspay.dakpion.worker.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DakpionApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    /**
     * Shared by the SMS receiver and the keep-alive service. Field-injected
     * here so components that Hilt can't inject directly reach it with a
     * plain cast, no entry-point lookup.
     */
    @Inject
    lateinit var smsSyncer: SmsSyncer

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        Diag.init(this)
        Diag.log("app: process start")
        // Periodic catch-up sync (15 min). The immediate path is SmsReceiver.
        SyncScheduler.ensurePeriodic(this)
        registerPushToken()
    }

    /**
     * Fetch the FCM token so HQ can wake this phone when a customer is
     * waiting and the OEM has frozen us. No-op when Firebase isn't configured
     * (no google-services.json) or Play services are missing. Safe to call
     * repeatedly (app open, sync) — the first attempt after install often
     * fails while Play services are still settling.
     */
    fun registerPushToken() {
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    if (!token.isNullOrBlank()) {
                        val prefs = com.orcuspay.dakpion.util.DakpionPreference(this)
                        if (prefs.getPushToken() != token) {
                            prefs.setPushToken(token)
                            Diag.log("push: token registered")
                        }
                    }
                }
                .addOnFailureListener { e -> Diag.log("push: token fetch failed ${e.message}") }
        } catch (e: Exception) {
            Diag.log("push: unavailable (${e.javaClass.simpleName})")
        }
    }

    companion object {
        fun syncer(context: android.content.Context): SmsSyncer =
            (context.applicationContext as DakpionApplication).smsSyncer
    }
}
