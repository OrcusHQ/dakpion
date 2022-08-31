package com.orcuspay.dakpion.domain.repository

import androidx.lifecycle.LiveData
import com.orcuspay.dakpion.data.remote.ApiResult
import com.orcuspay.dakpion.domain.model.Credential
import com.orcuspay.dakpion.domain.model.SendMessageRequest
import com.orcuspay.dakpion.domain.model.VerifyRequest
import com.orcuspay.dakpion.domain.model.VerifyResponse

interface DakpionRepository {
    suspend fun verify(verifyRequest: VerifyRequest): ApiResult<VerifyResponse>
    suspend fun send(sendMessageRequest: SendMessageRequest): ApiResult<Unit>
    suspend fun getCredentials(): LiveData<List<Credential>>
}