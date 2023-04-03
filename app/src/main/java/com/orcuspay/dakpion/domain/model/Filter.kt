package com.orcuspay.dakpion.domain.model

import java.util.regex.PatternSyntaxException

data class Filter(
    val id: Long = 0,
    val sender: String?,
    val value: String,
    val enabled: Boolean,
) {
    fun isValidRegex(): Boolean {
        if (!value.startsWith("/") || !value.endsWith("/"))
            return false
        return try {
            Regex(value)
            true
        } catch (e: PatternSyntaxException) {
            false
        }
    }

    fun match(value: String): Boolean {
        return if (isValidRegex()) {
            val actualRegexValue = this.value.trim('/', ' ')
            val regex =
                Regex(
                    actualRegexValue,
                    options = mutableSetOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)
                )
            regex.containsMatchIn(value)
        } else {
            value.contains(this.value)
        }
    }
}
