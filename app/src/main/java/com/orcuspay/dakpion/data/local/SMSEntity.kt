package com.orcuspay.dakpion.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.orcuspay.dakpion.domain.model.SMSStatus

@Entity(
    indices = [Index(value = ["credentialId", "smsId"], unique = true)]
)
data class SMSEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val credentialId: Int,
    val smsId: Int,
    val sender: String,
    val date: Long,
    val body: String,
    val status: SMSStatus,
)
