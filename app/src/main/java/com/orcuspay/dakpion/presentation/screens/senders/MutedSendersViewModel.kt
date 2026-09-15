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
            // Show the cached list immediately (no empty flash), then refresh
            // from the server and update so a just-configured sender appears.
            senders = computeSenders(credentials)
            senderRulesRepository.refresh(credentials)
            senders = computeSenders(credentials)
        }
    }

    private fun computeSenders(credentials: List<com.orcuspay.dakpion.domain.model.Credential>): List<String> {
        val union = linkedSetOf<String>()
        if (credentials.isEmpty()) {
            union.addAll(SenderRules.DEFAULT.allowedSenders)
        } else {
            credentials.forEach { c ->
                union.addAll(senderRulesRepository.getWhitelist(c.accessKey))
            }
        }
        return union.toList()
    }

    fun setMuted(senderKey: String, muted: Boolean) {
        val key = senderKey.lowercase()
        val next = disabled.toMutableSet()
        if (muted) next.add(key) else next.remove(key)
        preference.setDisabledSenders(next)
        disabled = next
    }
}
