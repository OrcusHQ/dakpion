package com.orcuspay.dakpion.domain.repository

import com.orcuspay.dakpion.domain.model.SMS
import java.util.*

interface SmsRepository {
    /**
     * Scan the inbox for SMS newer than [after] and store the payment ones.
     * @param refreshRules Fetch the latest sender rules from HQ first. Pass
     * false on time-critical paths (in-receiver upload) to use cached rules.
     */
    suspend fun loadSMSAfter(after: Date, refreshRules: Boolean = true)

    /**
     * Store an SMS delivered by the SMS_RECEIVED broadcast (before/without it
     * being in the inbox). Runs the same sender/keyword/filter gate as the
     * inbox scan. Cached sender rules are used (no network).
     */
    suspend fun ingestIncoming(
        sender: String,
        body: String,
        timestampMs: Long,
        subscriptionId: Int = -1,
    )
    suspend fun updateSMS(sms: SMS)
}