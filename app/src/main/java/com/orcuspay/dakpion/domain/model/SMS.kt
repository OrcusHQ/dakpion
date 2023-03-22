package com.orcuspay.dakpion.domain.model

import java.util.*

data class SMS(
    val id: Int = 0,
    val credentialId: Int,
    val smsId: Int,
    val sender: String,
    val date: Date,
    val body: String,
    val status: SMSStatus
)
