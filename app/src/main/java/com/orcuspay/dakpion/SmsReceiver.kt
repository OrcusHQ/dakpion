package com.orcuspay.dakpion

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
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

        // Take the SMS straight from the broadcast (multipart PDUs concatenated)
        // so we don't depend on the messaging app writing the inbox in time.
        val messages = try {
            Telephony.Sms.Intents.getMessagesFromIntent(intent)
        } catch (e: Exception) {
            null
        }
        val first = messages?.firstOrNull()
        val sender = first?.displayOriginatingAddress ?: first?.originatingAddress
        val body = messages?.joinToString("") { it.displayMessageBody ?: it.messageBody ?: "" }
        val timestampMs = first?.timestampMillis ?: System.currentTimeMillis()
        // Which SIM it arrived on (for the merchant's SIM filter); -1 if unknown.
        val subscriptionId = intent.getIntExtra(
            "subscription",
            intent.getIntExtra("android.telephony.extra.SUBSCRIPTION_INDEX", -1),
        )

        val pendingResult = goAsync()
        scope.launch {
            var outcome: SmsSyncer.Outcome? = null
            try {
                outcome = withTimeoutOrNull(RECEIVER_BUDGET_MS) {
                    if (!sender.isNullOrBlank() && !body.isNullOrBlank()) {
                        smsSyncer.ingestAndSync(sender, body, timestampMs, subscriptionId)
                    } else {
                        // Couldn't read the PDUs; fall back to the inbox after
                        // a short pause for the messaging app to write it.
                        delay(INBOX_SETTLE_MS)
                        smsSyncer.sync(fast = true)
                    }
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
        // for a foreground broadcast is ~10 s).
        private const val RECEIVER_BUDGET_MS = 8_500L
    }
}
