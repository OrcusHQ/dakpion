package com.orcuspay.dakpion.data.remote

import com.orcuspay.dakpion.data.remote.dto.request.SendMessageRequestDto
import com.orcuspay.dakpion.data.remote.dto.request.VerifyRequestDto
import com.orcuspay.dakpion.data.remote.dto.response.VerifyResponseDto
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface DakpionApi {

    @POST("dak-pion/verify")
    suspend fun verify(@Body verifyRequestDto: VerifyRequestDto): Response<VerifyResponseDto>

    @POST("dak-pion")
    suspend fun send(@Body sendMessageRequestDto: SendMessageRequestDto): Response<Unit>

    companion object {
        const val BASE_URL = "https://stage.dashboard.orcuspay.com/api/"
    }
}