package com.orcuspay.dakpion.presentation.screens.home

import android.util.Log
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
import com.orcuspay.dakpion.domain.repository.SmsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val text: String,
    private val dakpionRepository: DakpionRepository,
    private val smsRepository: SmsRepository,
) : ViewModel() {

    var state by mutableStateOf(HomeState())

    init {
        viewModelScope.launch {
            val test = dakpionRepository.getCredentialWithSMS()
            for (t in test) {
                Log.d("kraken", t.toString())
            }
        }
    }

    fun getCredentials(): LiveData<List<Credential>> {
        return dakpionRepository.getCredentialsLiveData()
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

}


data class HomeState(
    val loading: Boolean = false
)