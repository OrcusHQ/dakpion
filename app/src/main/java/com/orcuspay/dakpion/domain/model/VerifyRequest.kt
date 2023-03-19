package com.orcuspay.dakpion.domain.model

data class VerifyRequest(
    val accessKey: String,
    val secretKey: String,
    val mode: Mode
)
