package com.orcuspay.dakpion.data.remote.dto.request

data class SendMessageRequestDto(
    val accessKey: String,
    val secretKey: String,
    val mode: String,
    val senderId: String,
    val body: String,
    val amount: Double? = null,
    val balance: Double? = null,
    val deviceId: String? = null,
    val deviceName: String? = null,
    val manufacturer: String? = null,
    val model: String? = null,
    val sdk: String? = null,
    val appVersion: String? = null,
    val batteryLevel: Int? = null,
    val network: String? = null,
    val status: String? = null,
)
