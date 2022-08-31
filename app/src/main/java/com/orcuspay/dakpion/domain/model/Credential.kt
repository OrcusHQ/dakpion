package com.orcuspay.dakpion.domain.model

data class Credential(
    val id: Int,
    val accessKey: String,
    val secretKey: String,
    val mode: String,
    val credentialId: String,
    val businessName: String,
)
