package com.orcuspay.dakpion.domain.repository

import com.orcuspay.dakpion.domain.model.Credential
import com.orcuspay.dakpion.domain.model.SenderRules

interface SenderRulesRepository {
    /**
     * Fetch the latest rules for each credential from the server and cache them.
     * Network failures are swallowed (the last-known cache, or DEFAULT, keeps
     * working). Called on every sync and when the Senders screen opens, so a
     * newly-configured sender reaches the device promptly instead of waiting for
     * a cache TTL.
     */
    suspend fun refresh(credentials: List<Credential>)

    /** Cached rules for a credential, or [SenderRules.DEFAULT] if none. */
    fun getRules(accessKey: String): SenderRules

    /**
     * The raw whitelist the server sent for this credential (before local
     * mutes are subtracted). Used by the Senders screen to show every sender
     * HQ actually allowed, not a hardcoded list.
     */
    fun getWhitelist(accessKey: String): List<String>
}
