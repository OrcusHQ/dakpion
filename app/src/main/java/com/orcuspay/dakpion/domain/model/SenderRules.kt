package com.orcuspay.dakpion.domain.model

/**
 * Server-driven, on-device SMS filter rules for one business. Fetched from
 * /api/dakpion/filters and applied before an SMS is stored or forwarded:
 * forward only whitelisted senders, drop blocked/marketing senders and
 * OTP/PIN-style bodies.
 */
data class SenderRules(
    val version: Int,
    val checkIntervalHours: Int,
    val allowedSenders: List<String>,
    val blockedSenders: List<String>,
    val negativeKeywords: List<String>,
    val positiveKeywords: List<String>,
) {
    /** Whitelisted (substring, case-insensitive) and not a blocked sender. */
    fun isSenderAllowed(sender: String): Boolean {
        val s = sender.lowercase()
        val allowed = allowedSenders.any { it.isNotBlank() && s.contains(it.lowercase()) }
        if (!allowed) return false
        val blocked = blockedSenders.any { it.isNotBlank() && s.contains(it.lowercase()) }
        return !blocked
    }

    /** True when the body contains an OTP/PIN-style term that must be dropped. */
    fun isNegativeBody(body: String): Boolean {
        val b = body.lowercase()
        return negativeKeywords.any { it.isNotBlank() && b.contains(it.lowercase()) }
    }

    /**
     * When the server configures positive keywords (e.g. "received", "credited"),
     * the body must contain at least one to pass. Empty list = no-op, so this can
     * never drop a payment unless HQ opts in.
     */
    fun passesPositiveKeywords(body: String): Boolean {
        if (positiveKeywords.isEmpty()) return true
        val b = body.lowercase()
        return positiveKeywords.any { it.isNotBlank() && b.contains(it.lowercase()) }
    }

    companion object {
        /**
         * Fail-safe fallback = the app's historical hardcoded behavior. Used
         * when no rules have been fetched yet (or a fetch failed), so a missing
         * server response never drops a genuine payment.
         */
        val DEFAULT = SenderRules(
            version = 0,
            checkIntervalHours = 6,
            allowedSenders = listOf(
                "bkash", "nagad", "upay", "16216", "ibbl",
                "01847-348685", "16259", "pathaopay", "telecash", "ipay", "tap",
            ),
            blockedSenders = listOf("bkashnotice"),
            negativeKeywords = emptyList(),
            positiveKeywords = emptyList(),
        )
    }
}
