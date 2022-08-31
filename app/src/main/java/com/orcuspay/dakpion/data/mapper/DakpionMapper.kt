package com.orcuspay.dakpion.data.mapper

import com.orcuspay.dakpion.data.local.CredentialEntity
import com.orcuspay.dakpion.data.remote.dto.request.SendMessageRequestDto
import com.orcuspay.dakpion.data.remote.dto.request.VerifyRequestDto
import com.orcuspay.dakpion.data.remote.dto.response.VerifyResponseDto
import com.orcuspay.dakpion.domain.model.Credential
import com.orcuspay.dakpion.domain.model.SendMessageRequest
import com.orcuspay.dakpion.domain.model.VerifyRequest
import com.orcuspay.dakpion.domain.model.VerifyResponse

fun VerifyRequest.toVerifyRequestDto(): VerifyRequestDto {
    return VerifyRequestDto(
        accessKey = accessKey,
        secretKey = secretKey,
        mode = mode
    )
}

fun VerifyResponseDto.toVerifyResponse(): VerifyResponse {
    return VerifyResponse(
        id = business.id,
        mode = mode,
        name = business.name,
        userId = business.userId
    )
}

fun SendMessageRequest.toSendMessageRequestDto(): SendMessageRequestDto {
    return SendMessageRequestDto(
        accessKey = accessKey,
        secretKey = secretKey,
        mode = mode,
        senderId = senderId,
        body = body
    )
}

fun CredentialEntity.toCredential(): Credential {
    return Credential(
        id = id,
        accessKey = accessKey,
        secretKey = secretKey,
        mode = mode,
        credentialId = credentialId,
        businessName = businessName
    )
}