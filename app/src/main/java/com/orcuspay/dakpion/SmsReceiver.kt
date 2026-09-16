package com.orcuspay.dakpion

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingWorkPolicy
import com.orcuspay.dakpion.worker.SmsSyncer
import com.orcuspay.dakpion.worker.SyncScheduler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Fires the moment a payment SMS arrives and uploads it right here, inside
 * the receiver's wake-up window — instead of only scheduling a job for later.
 *
 * Why: on Infinix/Tecno, Xiaomi, Oppo, Vivo etc. the OEM battery manager keeps
 * background apps in a force-stopped-like state and defers their scheduled
 * jobs until the app is next opened. An SMS broadcast is one of the few things
 * that still wakes the process with elevated priority, so we use that window
 * (≈10 s) to do the upload directly. If it fails or times out, an expedited
 * WorkManager job takes over as the retry.
 */
class SmsReceiver : BroadcastReceiver() {

    // BroadcastReceiver.onReceive is abstract, so @AndroidEntryPoint injection
    // (which needs a super call) can't be used; resolve the syncer manually.
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SmsReceiverEntryPoint {
        fun smsSyncer(): SmsSyncer
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != SMS_RECEIVED) return
        Log.d("pluton", "Receiver Start")

        val smsSyncer = EntryPointAccessors
            .fromApplication(context.applicationContext, SmsReceiverEntryPoint::class.java)
            .smsSyncer()

        // If the OEM killed the keep-alive service, try to bring it back now
        // (works on Android 8–11; 12+ may refuse from here — harmless).
        SmsKeepAliveService.start(context)

        val pendingResult = goAsync()
        scope.launch {
            var outcome: SmsSyncer.Outcome? = null
            try {
                // SMS_RECEIVED is broadcast slightly before the messaging app
                // writes the SMS to the inbox, and the sync reads the inbox.
                // A short pause avoids scanning a moment too early.
                delay(INBOX_SETTLE_MS)
                outcome = withTimeoutOrNull(RECEIVER_BUDGET_MS) {
                    smsSyncer.sync(fast = true)
                }
            } catch (e: Exception) {
                Log.d("pluton", "In-receiver sync failed: ${e.message}")
            } finally {
                if (outcome != SmsSyncer.Outcome.SUCCESS) {
                    // Upload failed, timed out, or threw: hand off to the
                    // expedited job (with backoff) so nothing is lost.
                    SyncScheduler.enqueueImmediate(
                        context.applicationContext,
                        policy = ExistingWorkPolicy.APPEND_OR_REPLACE,
                    )
                }
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"
        const val SMS_BUNDLE = "pdus"
        const val EXTRA_BUNDLE = "pdus"

        private const val INBOX_SETTLE_MS = 1_200L
        // Total time we allow ourselves inside the broadcast (system limit
        // for a foreground broadcast is ~10 s), minus the settle delay.
        private const val RECEIVER_BUDGET_MS = 7_500L
    }
}
