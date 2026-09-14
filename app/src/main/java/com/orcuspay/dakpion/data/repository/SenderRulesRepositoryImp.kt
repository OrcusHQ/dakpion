package com.orcuspay.dakpion.data.repository

import android.util.Log
import com.orcuspay.dakpion.data.remote.DakpionApi
import com.orcuspay.dakpion.data.remote.dto.request.FilterRulesRequestDto
import com.orcuspay.dakpion.data.remote.dto.response.FilterRulesResponseDto
import com.orcuspay.dakpion.domain.model.Credential
import com.orcuspay.dakpion.domain.model.SenderRules
import com.orcuspay.dakpion.domain.repository.SenderRulesRepository
import com.orcuspay.dakpion.util.DakpionPreference
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SenderRulesRepositoryImp @Inject constructor(
    private val api: DakpionApi,
    private val preference: DakpionPreference,
) : SenderRulesRepository {

    override suspend fun refreshIfStale(credentials: List<Credential>) {
        val now = System.currentTimeMillis()
        for (credential in credentials) {
            val cached = readCache(credential.accessKey)
            val intervalMs =
                (cached?.checkIntervalHours ?: SenderRules.DEFAULT.checkIntervalHours)
                    .coerceAtLeast(1) * 60L * 60L * 1000L
            val fetchedAt = readFetchedAt(credential.accessKey)
            val isStale = fetchedAt == null || (now - fetchedAt) > intervalMs
            if (!isStale) continue

            try {
                val result = api.getFilterRules(
                    FilterRulesRequestDto(
                        accessKey = credential.accessKey,
                        secretKey = credential.secretKey,
                    )
                )
                if (result.isSuccess) {
                    result.getOrNull()?.let { dto ->
                        writeCache(credential.accessKey, dto.toSenderRules(), now)
                    }
                } else {
                    Log.d("kraken", "Filter rules fetch failed: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                // Keep the last-known rules; never block ingestion on this.
                Log.d("kraken", "Filter rules fetch error: ${e.message}")
            }
        }
    }

    override fun getWhitelist(accessKey: String): List<String> {
        return (readCache(accessKey) ?: SenderRules.DEFAULT).allowedSenders
    }

    override fun getRules(accessKey: String): SenderRules {
        val base = readCache(accessKey) ?: SenderRules.DEFAULT
        // #4 Sender mute: locally-disabled senders are removed from the
        // whitelist. Strictly subtractive — can only ever narrow the list, never
        // add a sender HQ didn't whitelist.
        val disabled = preference.getDisabledSenders()
        if (disabled.isEmpty()) return base
        val filtered = base.allowedSenders.filter { sender ->
            disabled.none { d -> sender.equals(d, ignoreCase = true) }
        }
        return base.copy(allowedSenders = filtered)
    }

    // ---- cache (SharedPreferences JSON, keyed by accessKey) ----

    private fun cacheKey(accessKey: String) = "$KEY_PREFIX$accessKey"
    private fun timeKey(accessKey: String) = "$TIME_PREFIX$accessKey"

    private fun readFetchedAt(accessKey: String): Long? {
        val raw = preference.getString(timeKey(accessKey)) ?: return null
        return raw.toLongOrNull()
    }

    private fun writeCache(accessKey: String, rules: SenderRules, fetchedAt: Long) {
        val json = JSONObject().apply {
            put("version", rules.version)
            put("checkIntervalHours", rules.checkIntervalHours)
            put("allowedSenders", JSONArray(rules.allowedSenders))
            put("blockedSenders", JSONArray(rules.blockedSenders))
            put("negativeKeywords", JSONArray(rules.negativeKeywords))
            put("positiveKeywords", JSONArray(rules.positiveKeywords))
        }
        preference.putString(cacheKey(accessKey), json.toString())
        preference.putString(timeKey(accessKey), fetchedAt.toString())
    }

    private fun readCache(accessKey: String): SenderRules? {
        val raw = preference.getString(cacheKey(accessKey)) ?: return null
        return try {
            val json = JSONObject(raw)
            SenderRules(
                version = json.optInt("version", 0),
                checkIntervalHours = json.optInt("checkIntervalHours", 6),
                allowedSenders = json.optStringList("allowedSenders"),
                blockedSenders = json.optStringList("blockedSenders"),
                negativeKeywords = json.optStringList("negativeKeywords"),
                positiveKeywords = json.optStringList("positiveKeywords"),
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun JSONObject.optStringList(name: String): List<String> {
        val arr = optJSONArray(name) ?: return emptyList()
        val out = ArrayList<String>(arr.length())
        for (i in 0 until arr.length()) {
            val s = arr.optString(i, "")
            if (s.isNotBlank()) out.add(s)
        }
        return out
    }

    private fun FilterRulesResponseDto.toSenderRules() = SenderRules(
        version = version,
        checkIntervalHours = if (checkIntervalHours < 1) 6 else checkIntervalHours,
        allowedSenders = allowedSenders,
        blockedSenders = blockedSenders,
        negativeKeywords = negativeKeywords,
        positiveKeywords = positiveKeywords,
    )

    companion object {
        private const val KEY_PREFIX = "SENDER_RULES_"
        private const val TIME_PREFIX = "SENDER_RULES_TIME_"
    }
}
