package com.orcuspay.dakpion.data.mapper

import com.orcuspay.dakpion.data.local.CredentialEntity
import com.orcuspay.dakpion.data.remote.dto.request.SendMessageRequestDto
import com.orcuspay.dakpion.data.remote.dto.request.VerifyRequestDto
import com.orcuspay.dakpion.data.remote.dto.response.SendMessageResponseDto
import com.orcuspay.dakpion.data.remote.dto.response.VerifyResponseDto
import com.orcuspay.dakpion.domain.model.*

fun VerifyRequest.toVerifyRequestDto(): VerifyRequestDto {
    return VerifyRequestDto(
        accessKey = accessKey,
        secretKey = secretKey,
        mode = if (mode == Mode.LIVE) "prod" else "test"
    )
}

fun VerifyResponseDto.toVerifyResponse(): VerifyResponse {
    return VerifyResponse(
        id = business.id,
        mode = if (mode == "prod") Mode.LIVE else Mode.TEST,
        name = business.name,
        userId = business.userId,
        icon = business.icon,
    )
}

fun SendMessageRequest.toSendMessageRequestDto(): SendMessageRequestDto {
    return SendMessageRequestDto(
        accessKey = accessKey,
        secretKey = secretKey,
        mode = if (mode == Mode.LIVE) "prod" else "test",
        senderId = senderId,
        body = body
    )
}

fun SendMessageResponseDto.toSendMessageResponse(): SendMessageResponse {
    return SendMessageResponse(
        stored = stored
    )
}

fun CredentialEntity.toCredential(): Credential {
    return Credential(
        id = id,
        accessKey = accessKey,
        secretKey = secretKey,
        mode = mode,
        credentialId = credentialId,
        businessName = businessName,
        enabled = enabled,
        icon = icon,
        unauthorized = unauthorized
    )
}

fun Credential.toCredentialEntity(): CredentialEntity {
    return CredentialEntity(
        id = id,
        accessKey = accessKey,
        secretKey = secretKey,
        mode = mode,
        credentialId = credentialId,
        businessName = businessName,
        enabled = enabled,
        icon = icon,
        unauthorized = unauthorized
    )
}