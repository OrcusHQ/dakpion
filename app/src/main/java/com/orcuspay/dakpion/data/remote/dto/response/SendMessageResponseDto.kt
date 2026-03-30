package com.orcuspay.dakpion.data.remote.dto.response

data class SendMessageResponseDto(
    val stored: Boolean,
    val suspicious: Boolean = false,
)
