package com.orcuspay.dakpion.worker

import android.util.Log
import com.orcuspay.dakpion.data.exception.InvalidCredentialException
import com.orcuspay.dakpion.data.remote.ApiResult
import com.orcuspay.dakpion.domain.model.SMS
import com.orcuspay.dakpion.domain.model.SMSStatus
import com.orcuspay.dakpion.domain.repository.DakpionRepository
import com.orcuspay.dakpion.domain.repository.SmsRepository
import com.orcuspay.dakpion.util.DakpionPreference
import com.orcuspay.dakpion.util.Diag
import com.orcuspay.dakpion.util.NotificationHelper
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The one SMS sync pipeline: scan the inbox, store payment SMS, upload them to
 * HQ. Shared by [SmsReceiver] (immediate upload the moment an SMS lands) and
 * [DakpionKamla] (WorkManager retry + periodic catch-up), so both paths behave
 * identically. A mutex serialises overlapping runs.
 */
@Singleton
class SmsSyncer @Inject constructor(
    private val application: android.app.Application,
    private val dakpionRepository: DakpionRepository,
    private val smsRepository: SmsRepository,
    private val dakpionPreference: DakpionPreference,
    private val notificationHelper: NotificationHelper,
) {

    enum class Outcome { SUCCESS, RETRY }

    private val mutex = Mutex()

    /**
     * Store an SMS taken straight from the SMS_RECEIVED broadcast, then run a
     * fast sync to upload it. Doesn't depend on the messaging app having
     * written the inbox yet — the same approach PipraPay/OwnPay use.
     */
    suspend fun ingestAndSync(
        sender: String,
        body: String,
        timestampMs: Long,
        subscriptionId: Int = -1,
    ): Outcome {
        try {
            smsRepository.ingestIncoming(
                sender = sender,
                body = body,
                timestampMs = timestampMs,
                subscriptionId = subscriptionId,
            )
            Diag.log("ingest: stored candidate from=$sender")
        } catch (e: Exception) {
            Diag.log("ingest: threw ${e.javaClass.simpleName}: ${e.message}")
        }
        return sync(fast = true)
    }

    /**
     * @param fast Skip the credential heartbeat and the sender-rules refresh so
     * the upload fits inside a broadcast receiver's time budget. Cached rules
     * (or DEFAULT) are used. The periodic sync runs the full version.
     */
    suspend fun sync(fast: Boolean = false): Outcome = mutex.withLock {
        // Hold the CPU for the duration of the sync so a doze/sleep transition
        // can't suspend an upload halfway (the receiver and FCM paths already
        // hold a system wake lock; the inbox-observer path does not).
        val wakeLock = try {
            (application.getSystemService(android.content.Context.POWER_SERVICE) as? android.os.PowerManager)
                ?.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "dakpion:sync")
                ?.apply { setReferenceCounted(false); acquire(WAKE_LOCK_TIMEOUT_MS) }
        } catch (_: Exception) {
            null
        }
        try {
            syncLocked(fast)
        } finally {
            try {
                if (wakeLock?.isHeld == true) wakeLock.release()
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun syncLocked(fast: Boolean): Outcome {
        try {
            val syncStartedAt = Date()
            Diag.log("sync: start fast=$fast")
            if (!fast) {
                // Full syncs also (re)try push registration until a token is
                // stored, so a failed first attempt heals on its own.
                if (dakpionPreference.getPushToken().isNullOrBlank()) {
                    (application as? com.orcuspay.dakpion.DakpionApplication)?.registerPushToken()
                }
                dakpionRepository.syncCredentials()
            }

            // Full syncs rescan the last 24h so an SMS skipped earlier (rules
            // momentarily excluded its sender, a wrong SIM filter) gets
            // re-evaluated with the current rules instead of being lost. The
            // fast path (receiver / inbox observer / push) only needs what
            // just arrived — a short window keeps it cheap on low-end phones.
            // Already-stored SMS are deduped by the unique index either way.
            val lookback = if (fast) FAST_LOOKBACK_MS else SCAN_LOOKBACK_MS
            val scanAfter = Date((syncStartedAt.time - lookback).coerceAtLeast(0L))

            smsRepository.loadSMSAfter(after = scanAfter, refreshRules = !fast)
            dakpionPreference.setLastSyncTime(syncStartedAt)

            val credentialWithSMSList = dakpionRepository.getCredentialWithSMS()

            var hasError = false
            var successCount = 0
            var lastSuccessfulSms: SMS? = null

            credentialWithSMSList.forEach { cs ->
                val credential = cs.credential
                if (!credential.enabled) return@forEach

                cs.smsList
                    .filter {
                        // Only retry messages that never got a definitive
                        // server verdict: PROCESSING (never acked) and ERROR
                        // (transient network/5xx). NOT_STORED is a terminal
                        // "this is not a payment" decision — re-sending it
                        // every cycle floods the API forever. SUSPICIOUS still
                        // needs its first upload; it self-terminates to
                        // DUPLICATE on the next cycle once stored.
                        it.status == SMSStatus.PROCESSING ||
                            it.status == SMSStatus.ERROR ||
                            it.status == SMSStatus.SUSPICIOUS
                    }
                    .forEach { sms ->
                        // Retry cap by AGE, not attempt count: a payment SMS
                        // must keep retrying through a flaky network for as
                        // long as it could still matter. (An attempt-count cap
                        // silently abandoned fresh SMS on phones with bad DNS.)
                        if (syncStartedAt.time - sms.date.time > MAX_RETRY_AGE_MS) {
                            return@forEach
                        }

                        when (val result = dakpionRepository.send(credential, sms)) {
                            is ApiResult.Error -> {
                                Diag.log("sync: send FAILED sms=${sms.smsId} from=${sms.sender}: ${result.message}")
                                if (result.exception !is InvalidCredentialException) {
                                    hasError = true
                                    dakpionPreference.incrementSendAttempts(sms.smsId)
                                } else {
                                    notificationHelper.showNotification(
                                        notificationId = credential.id,
                                        title = "Invalid credential",
                                        content = "${credential.businessName} has invalid credentials. We have disabled it."
                                    )
                                }
                            }
                            is ApiResult.Success -> {
                                Diag.log("sync: send OK sms=${sms.smsId} from=${sms.sender} stored=${result.data?.stored}")
                                successCount++
                                lastSuccessfulSms = sms
                                dakpionPreference.clearSendAttempts(sms.smsId)
                            }
                        }
                    }
            }

            val last = lastSuccessfulSms
            if (successCount == 1 && last != null) {
                notificationHelper.showNotification(
                    notificationId = last.smsId,
                    title = "Payment Detected",
                    content = "New payment from ${last.sender}: ${last.body.take(50)}..."
                )
            } else if (successCount > 1) {
                notificationHelper.showNotification(
                    notificationId = 999,
                    title = "Multiple Payments Detected",
                    content = "Successfully processed $successCount new transactions."
                )
            }

            Diag.log("sync: done sent=$successCount error=$hasError")
            return if (hasError) Outcome.RETRY else Outcome.SUCCESS
        } catch (e: Exception) {
            Diag.log("sync: threw ${e.javaClass.simpleName}: ${e.message}")
            return Outcome.RETRY
        }
    }

    companion object {
        private const val SCAN_LOOKBACK_MS = 24L * 60L * 60L * 1000L
        private const val FAST_LOOKBACK_MS = 30L * 60L * 1000L
        // Upper bound for the sync wake lock; a normal sync takes 1–5 s.
        private const val WAKE_LOCK_TIMEOUT_MS = 60_000L
        // Stop retrying an SMS once it is this old, so a permanently-failing
        // message never hammers the API forever — but never before then.
        private const val MAX_RETRY_AGE_MS = 48L * 60L * 60L * 1000L
    }
}
