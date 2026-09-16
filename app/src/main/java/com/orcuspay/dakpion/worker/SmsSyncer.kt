package com.orcuspay.dakpion.worker

import android.util.Log
import com.orcuspay.dakpion.data.exception.InvalidCredentialException
import com.orcuspay.dakpion.data.remote.ApiResult
import com.orcuspay.dakpion.domain.model.SMS
import com.orcuspay.dakpion.domain.model.SMSStatus
import com.orcuspay.dakpion.domain.repository.DakpionRepository
import com.orcuspay.dakpion.domain.repository.SmsRepository
import com.orcuspay.dakpion.util.DakpionPreference
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
    private val dakpionRepository: DakpionRepository,
    private val smsRepository: SmsRepository,
    private val dakpionPreference: DakpionPreference,
    private val notificationHelper: NotificationHelper,
) {

    enum class Outcome { SUCCESS, RETRY }

    private val mutex = Mutex()

    /**
     * @param fast Skip the credential heartbeat and the sender-rules refresh so
     * the upload fits inside a broadcast receiver's time budget. Cached rules
     * (or DEFAULT) are used. The periodic sync runs the full version.
     */
    suspend fun sync(fast: Boolean = false): Outcome = mutex.withLock {
        try {
            val syncStartedAt = Date()
            if (!fast) {
                dakpionRepository.syncCredentials()
            }

            // Always rescan the last 24h, not just since the previous sync. An
            // SMS skipped earlier (rules momentarily excluded its sender, the
            // sender was muted, a SIM filter was wrong) gets re-evaluated with
            // the current rules on every run instead of falling out of a
            // narrow window and being lost. Already-stored SMS are deduped by
            // the (credentialId, smsId) unique index, so nothing re-sends.
            val scanAfter = Date((syncStartedAt.time - SCAN_LOOKBACK_MS).coerceAtLeast(0L))

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
                        // Retry cap: give up on a single SMS after too many
                        // failed uploads so it stops flooding the API.
                        if (dakpionPreference.getSendAttempts(sms.smsId) >= MAX_SEND_ATTEMPTS) {
                            return@forEach
                        }

                        when (val result = dakpionRepository.send(credential, sms)) {
                            is ApiResult.Error -> {
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

            if (hasError) Outcome.RETRY else Outcome.SUCCESS
        } catch (e: Exception) {
            Log.d("kraken", "SmsSyncer failed: ${e.message}")
            Outcome.RETRY
        }
    }

    companion object {
        private const val SCAN_LOOKBACK_MS = 24L * 60L * 60L * 1000L
        // Stop retrying a single SMS after this many failed uploads, so a
        // permanently-failing message never hammers the API forever.
        private const val MAX_SEND_ATTEMPTS = 25
    }
}
