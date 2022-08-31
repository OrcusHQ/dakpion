package com.orcuspay.dakpion.data.remote.dto.request

data class SendMessageRequestDto(
    val accessKey: String,
    val secretKey: String,
    val mode: String,
    val senderId: String,
    val body: String
)