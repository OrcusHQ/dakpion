package com.orcuspay.dakpion.domain.model

data class VerifyResponse(
    val id: String,
    val mode: Mode,
    val name: String,
    val userId: String,
    val icon: String?,
)
