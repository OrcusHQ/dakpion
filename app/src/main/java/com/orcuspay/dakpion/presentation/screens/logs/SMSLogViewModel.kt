package com.orcuspay.dakpion.presentation.screens.logs

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orcuspay.dakpion.domain.model.Credential
import com.orcuspay.dakpion.domain.model.CredentialWithSMS
import com.orcuspay.dakpion.domain.model.SMS
import com.orcuspay.dakpion.domain.repository.DakpionRepository
import com.orcuspay.dakpion.util.toSimpleDateString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SMSLogViewModel @Inject constructor(
    private val dakpionRepository: DakpionRepository,
) : ViewModel() {

    fun getCredentialsWithSMS(): LiveData<List<CredentialWithSMS>> {
        return dakpionRepository.getCredentialWithSMSLiveData()
    }

    fun groupSMSByDate(smsList: List<SMS>): Map<String, List<SMS>> {
        return smsList
            .sortedByDescending { it.date }
            .groupBy { it.date.toSimpleDateString() }
    }

    fun retry(credential: Credential, sms: SMS) {
        viewModelScope.launch {
            Log.d("kraken", "Retry Sending $sms")
            dakpionRepository.send(
                credential = credential,
                sms = sms
            )
        }
    }
}