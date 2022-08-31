package com.orcuspay.dakpion.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CredentialEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val accessKey: String,
    val secretKey: String,
    val mode: String,
    val credentialId: String,
    val businessName: String,
)