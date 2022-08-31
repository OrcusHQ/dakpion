package com.orcuspay.dakpion.data.remote.dto.response


data class VerifyResponseDto(
    val business: BusinessDto,
    val mode: String
)

data class BusinessDto(
    val id: String,
    val name: String,
    val userId: String
)
