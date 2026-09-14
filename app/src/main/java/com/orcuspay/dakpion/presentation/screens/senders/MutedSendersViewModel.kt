package com.orcuspay.dakpion.presentation.screens.senders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orcuspay.dakpion.data.local.DakpionDatabase
import com.orcuspay.dakpion.data.mapper.toCredential
import com.orcuspay.dakpion.domain.model.SenderRules
import com.orcuspay.dakpion.domain.repository.SenderRulesRepository
import com.orcuspay.dakpion.util.DakpionPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MutedSendersViewModel @Inject constructor(
    private val preference: DakpionPreference,
    private val senderRulesRepository: SenderRulesRepository,
    db: DakpionDatabase,
) : ViewModel() {

    private val dao = db.dao

    // Every sender HQ whitelisted for this device (union across credentials).
    var senders by mutableStateOf<List<String>>(emptyList())
        private set

    var disabled by mutableStateOf(preference.getDisabledSenders())
        private set

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val credentials = dao.getCredentials().map { it.toCredential() }
            // Pull the latest rules from the server so a just-configured sender
            // shows up immediately instead of after a cache TTL.
            senderRulesRepository.refresh(credentials)
            val union = linkedSetOf<String>()
            if (credentials.isEmpty()) {
                union.addAll(SenderRules.DEFAULT.allowedSenders)
            } else {
                credentials.forEach { c ->
                    union.addAll(senderRulesRepository.getWhitelist(c.accessKey))
                }
            }
            senders = union.toList()
        }
    }

    fun setMuted(senderKey: String, muted: Boolean) {
        val key = senderKey.lowercase()
        val next = disabled.toMutableSet()
        if (muted) next.add(key) else next.remove(key)
        preference.setDisabledSenders(next)
        disabled = next
    }
}
