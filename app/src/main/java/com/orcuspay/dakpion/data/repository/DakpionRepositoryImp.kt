package com.orcuspay.dakpion.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.orcuspay.dakpion.data.exception.InternalServerException
import com.orcuspay.dakpion.data.local.CredentialEntity
import com.orcuspay.dakpion.data.local.DakpionDatabase
import com.orcuspay.dakpion.data.mapper.*
import com.orcuspay.dakpion.data.remote.ApiResult
import com.orcuspay.dakpion.data.remote.DakpionApi
import com.orcuspay.dakpion.domain.model.Credential
import com.orcuspay.dakpion.domain.model.SendMessageRequest
import com.orcuspay.dakpion.domain.model.VerifyRequest
import com.orcuspay.dakpion.domain.model.VerifyResponse
import com.orcuspay.dakpion.domain.repository.DakpionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DakpionRepositoryImp @Inject constructor(
    private val api: DakpionApi,
    private val db: DakpionDatabase
) : DakpionRepository {

    private val dao = db.dao

    override suspend fun verify(verifyRequest: VerifyRequest): ApiResult<VerifyResponse> {

        val result = api.verify(verifyRequest.toVerifyRequestDto())

        return if (result.isSuccess) {
            val response = result.getOrNull()
            val verifyResponse = response?.toVerifyResponse()
            if (verifyResponse != null) {
                dao.createCredential(
                    CredentialEntity(
                        accessKey = verifyRequest.accessKey,
                        secretKey = verifyRequest.secretKey,
                        mode = verifyRequest.mode,
                        credentialId = verifyResponse.id,
                        businessName = verifyResponse.name,
                        enabled = true,
                    )
                )
                ApiResult.Success(data = response.toVerifyResponse())
            } else {
                ApiResult.Error(message = InternalServerException().message)
            }
        } else {
            ApiResult.Error(message = result.exceptionOrNull()?.message)
        }
    }

    override suspend fun send(sendMessageRequest: SendMessageRequest): ApiResult<Unit> {
        val result = api.send(sendMessageRequest.toSendMessageRequestDto())
        if (result.isSuccess) {
            Log.d("kraken", "success!")
            val response = result.getOrNull()
            if (response == null) {
                Log.d("kraken", "null")
            } else {
                Log.d("kraken", "not null")
                Log.d("kraken", response.toString())
            }
        } else {
            Log.d("kraken", "failure")
            Log.d("kraken", result.exceptionOrNull().toString())
        }

        return ApiResult.Success(data = null)
    }

    override fun getCredentials(): LiveData<List<Credential>> {
        return Transformations.map(dao.getCredentials()) {
            it.map { ce ->
                ce.toCredential()
            }
        }
    }

    override suspend fun deleteCredential(credential: Credential) {
        dao.deleteCredential(credential.toCredentialEntity())
    }

    override suspend fun setCredentialEnabled(credential: Credential, enabled: Boolean) {
        dao.updateCredential(credential.copy(enabled = enabled).toCredentialEntity())
    }
}