package com.orcuspay.dakpion.data.mapper

import com.orcuspay.dakpion.data.local.CredentialEntitiesWithSMSEntities
import com.orcuspay.dakpion.data.local.SMSEntity
import com.orcuspay.dakpion.domain.model.CredentialWithSMS
import com.orcuspay.dakpion.domain.model.SMS
import java.util.*

fun SMS.toSMSEntity(): SMSEntity {
    return SMSEntity(
        id = id,
        credentialId = credentialId,
        smsId = smsId,
        sender = sender,
        date = date.time,
        body = body,
        status = status
    )
}


fun SMSEntity.toSMS(): SMS {
    return SMS(
        id = id,
        credentialId = credentialId,
        smsId = smsId,
        sender = sender,
        date = Date(date),
        body = body,
        status = status,
    )
}


fun CredentialEntitiesWithSMSEntities.toCredentialWithSMS(): CredentialWithSMS {
    return CredentialWithSMS(
        credential = credential.toCredential(),
        smsList = smsList.map { it.toSMS() }
    )
}