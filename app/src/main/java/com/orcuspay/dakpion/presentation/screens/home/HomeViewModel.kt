package com.orcuspay.dakpion.presentation.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orcuspay.dakpion.domain.model.SendMessageRequest
import com.orcuspay.dakpion.domain.model.VerifyRequest
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

    fun testVerify() {
        viewModelScope.launch {
            state = state.copy(loading = true)
            dakpionRepository.verify(
                VerifyRequest(
                    accessKey = "",
                    secretKey = "",
                    mode = "test"
                )
            )
            state = state.copy(loading = false)
        }
    }

    fun testSend() {
        viewModelScope.launch {
            state = state.copy(loading = true)
            dakpionRepository.send(
                SendMessageRequest(
                    accessKey = "test",
                    secretKey = "test",
                    mode = "test",
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