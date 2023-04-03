package com.orcuspay.dakpion.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FilterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String?,
    val value: String,
    val enabled: Boolean,
)
