package com.orcuspay.dakpion.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.orcuspay.dakpion.domain.model.Mode

@Entity
data class CredentialEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val accessKey: String,
    val secretKey: String,
    val mode: Mode,
    val credentialId: String,
    val businessName: String,
)