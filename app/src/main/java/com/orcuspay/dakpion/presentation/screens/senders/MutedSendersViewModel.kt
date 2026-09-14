package com.orcuspay.dakpion.presentation.screens.senders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.orcuspay.dakpion.util.DakpionPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MutedSendersViewModel @Inject constructor(
    private val preference: DakpionPreference,
) : ViewModel() {

    var disabled by mutableStateOf(preference.getDisabledSenders())
        private set

    fun setMuted(senderKey: String, muted: Boolean) {
        val key = senderKey.lowercase()
        val next = disabled.toMutableSet()
        if (muted) next.add(key) else next.remove(key)
        preference.setDisabledSenders(next)
        disabled = next
    }
}
