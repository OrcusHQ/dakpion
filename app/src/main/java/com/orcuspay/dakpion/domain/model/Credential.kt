package com.orcuspay.dakpion.domain.model

enum class Mode {
    LIVE,
    TEST
}

data class Credential(
    val id: Int,
    val accessKey: String,
    val secretKey: String,
    val mode: Mode,
    val credentialId: String,
    val businessName: String,
    val enabled: Boolean,
    val icon: String?,
    val unauthorized: Boolean,
)
