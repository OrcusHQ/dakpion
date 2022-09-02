package com.orcuspay.dakpion.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.orcuspay.dakpion.data.local.DakpionDatabase
import com.orcuspay.dakpion.data.mapper.toSendMessageRequestDto
import com.orcuspay.dakpion.data.mapper.toVerifyRequestDto
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

    override suspend fun getCredentials(): LiveData<List<Credential>> {
        TODO("Not yet implemented")
    }
}