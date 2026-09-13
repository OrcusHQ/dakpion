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
}
