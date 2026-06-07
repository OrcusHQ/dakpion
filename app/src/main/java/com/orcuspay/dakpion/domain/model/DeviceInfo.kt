package com.orcuspay.dakpion.domain.model

data class DeviceInfo(
    val deviceId: String,
    val deviceName: String,
    val manufacturer: String,
    val model: String,
    val sdk: String,
    val appVersion: String,
    val batteryLevel: Int?,
    val network: String?,
    val status: String = "online",
)
