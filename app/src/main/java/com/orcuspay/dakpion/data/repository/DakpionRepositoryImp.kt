package com.orcuspay.dakpion.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.orcuspay.dakpion.data.local.DakpionDatabase
import com.orcuspay.dakpion.data.mapper.toVerifyRequestDto
import com.orcuspay.dakpion.data.remote.ApiResult
import com.orcuspay.dakpion.data.remote.DakpionApi
import com.orcuspay.dakpion.domain.model.Credential
import com.orcuspay.dakpion.domain.model.SendMessageRequest
import com.orcuspay.dakpion.domain.model.VerifyRequest
import com.orcuspay.dakpion.domain.model.VerifyResponse
import com.orcuspay.dakpion.domain.repository.DakpionRepository
import okio.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DakpionRepositoryImp @Inject constructor(
    private val api: DakpionApi,
    private val db: DakpionDatabase
) : DakpionRepository {

    private val dao = db.dao

    override suspend fun verify(verifyRequest: VerifyRequest): ApiResult<VerifyResponse> {
        try {
            val code = api.verify(verifyRequest.toVerifyRequestDto()).code()
            Log.d("kraken", code.toString())
        } catch (e: IOException) {
            Log.d("kraken", "device net")
        }

        return ApiResult.Success(data = null)
    }

    override suspend fun send(sendMessageRequest: SendMessageRequest): ApiResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getCredentials(): LiveData<List<Credential>> {
        TODO("Not yet implemented")
    }
}