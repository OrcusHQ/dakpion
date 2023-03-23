package com.orcuspay.dakpion.domain.repository

import androidx.lifecycle.LiveData
import com.orcuspay.dakpion.data.remote.ApiResult
import com.orcuspay.dakpion.domain.model.*

interface DakpionRepository {
    suspend fun verify(verifyRequest: VerifyRequest): ApiResult<VerifyResponse>
    suspend fun send(credential: Credential, sms: SMS): ApiResult<SendMessageResponse>
    fun getCredentialsLiveData(): LiveData<List<Credential>>
    suspend fun getCredentials(): List<Credential>
    suspend fun deleteCredential(credential: Credential)
    suspend fun setCredentialEnabled(credential: Credential, enabled: Boolean)
    suspend fun getCredentialWithSMS(): List<CredentialWithSMS>
    fun getCredentialWithSMSLiveData(): LiveData<List<CredentialWithSMS>>
}