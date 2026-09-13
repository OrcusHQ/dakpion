package com.orcuspay.dakpion.data.remote.dto.response

data class FilterRulesResponseDto(
    val version: Int = 0,
    val checkIntervalHours: Int = 6,
    val allowedSenders: List<String> = emptyList(),
    val blockedSenders: List<String> = emptyList(),
    val negativeKeywords: List<String> = emptyList(),
    val positiveKeywords: List<String> = emptyList(),
)
