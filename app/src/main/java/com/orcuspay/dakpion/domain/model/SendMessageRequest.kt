package com.orcuspay.dakpion.domain.model

data class SendMessageRequest(
    val accessKey: String,
    val secretKey: String,
    val mode: String,
    val senderId: String,
    val body: String
)
