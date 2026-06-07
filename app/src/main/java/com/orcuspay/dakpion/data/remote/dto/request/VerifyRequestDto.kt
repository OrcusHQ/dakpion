package com.orcuspay.dakpion.data.remote.dto.request

data class VerifyRequestDto(
    val accessKey: String,
    val secretKey: String,
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
