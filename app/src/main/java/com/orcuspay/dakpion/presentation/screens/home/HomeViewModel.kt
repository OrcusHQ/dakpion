package com.orcuspay.dakpion.presentation.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orcuspay.dakpion.domain.model.Credential
import com.orcuspay.dakpion.domain.model.Mode
import com.orcuspay.dakpion.domain.model.SendMessageRequest
import com.orcuspay.dakpion.domain.repository.DakpionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val text: String,
    private val dakpionRepository: DakpionRepository
) : ViewModel() {

    var state by mutableStateOf(HomeState())

    fun getCredentials(): LiveData<List<Credential>> {
        return dakpionRepository.getCredentials()
    }

    fun deleteCredential(credential: Credential) {
        viewModelScope.launch {
            dakpionRepository.deleteCredential(credential)
        }
    }

    fun setCredentialEnabled(credential: Credential, enabled: Boolean) {
        viewModelScope.launch {
            dakpionRepository.setCredentialEnabled(credential, enabled)
        }
    }


    fun testSend() {
        viewModelScope.launch {
            state = state.copy(loading = true)
            dakpionRepository.send(
                SendMessageRequest(
                    accessKey = "test",
                    secretKey = "test",
                    mode = Mode.TEST,
                    senderId = "bKash",
                    body = "You have received Tk 1.00 from 01878287735. Fee Tk 0.00. Balance Tk 1.62. TrxID 9H7396WNSF at 07/08/2022 06:05"
                )
            )
            state = state.copy(loading = false)
        }
    }

}


data class HomeState(
    val loading: Boolean = false
)