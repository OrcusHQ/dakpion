package com.orcuspay.dakpion.domain.model

data class CredentialWithSMS(
    val credential: Credential,
    val smsList: List<SMS>
)
