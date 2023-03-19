package com.orcuspay.dakpion.domain.model

data class SendMessageRequest(
    val accessKey: String,
    val secretKey: String,
    val mode: Mode,
    val senderId: String,
    val body: String
)
