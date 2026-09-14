package com.orcuspay.dakpion.domain.repository

import com.orcuspay.dakpion.domain.model.Credential
import com.orcuspay.dakpion.domain.model.SenderRules

interface SenderRulesRepository {
    /**
     * Refresh rules for any credential whose cache is missing or stale. Network
     * failures are swallowed (the last-known rules, or DEFAULT, keep working).
     */
    suspend fun refreshIfStale(credentials: List<Credential>)

    /** Cached rules for a credential, or [SenderRules.DEFAULT] if none. */
    fun getRules(accessKey: String): SenderRules

    /**
     * The raw whitelist the server sent for this credential (before local
     * mutes are subtracted). Used by the Senders screen to show every sender
     * HQ actually allowed, not a hardcoded list.
     */
    fun getWhitelist(accessKey: String): List<String>
}
