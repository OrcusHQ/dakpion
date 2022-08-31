package com.orcuspay.dakpion.domain.model

data class VerifyResponse(
    val id: String,
    val mode: String,
    val name: String,
    val userId: String
)
