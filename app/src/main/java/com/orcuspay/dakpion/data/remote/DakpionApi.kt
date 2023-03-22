package com.orcuspay.dakpion.data.remote

import com.orcuspay.dakpion.data.remote.dto.request.SendMessageRequestDto
import com.orcuspay.dakpion.data.remote.dto.request.VerifyRequestDto
import com.orcuspay.dakpion.data.remote.dto.response.SendMessageResponseDto
import com.orcuspay.dakpion.data.remote.dto.response.VerifyResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface DakpionApi {

    @POST("dakpion/verify")
    suspend fun verify(@Body verifyRequestDto: VerifyRequestDto): Result<VerifyResponseDto>

    @POST("dakpion")
    suspend fun send(@Body sendMessageRequestDto: SendMessageRequestDto): Result<SendMessageResponseDto>

    companion object {
        const val BASE_URL = "https://53ce-118-179-94-37.ngrok.io/api/"
    }
}