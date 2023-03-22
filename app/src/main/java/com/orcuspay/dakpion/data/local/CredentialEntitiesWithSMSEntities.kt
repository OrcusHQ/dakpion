package com.orcuspay.dakpion.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class CredentialEntitiesWithSMSEntities(
    @Embedded val credential: CredentialEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "credentialId",
    )
    val smsList: List<SMSEntity>,
)
