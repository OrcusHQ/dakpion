package com.orcuspay.dakpion.domain.repository

import com.orcuspay.dakpion.domain.model.SMS
import java.util.*

interface SmsRepository {
    suspend fun loadSMSAfter(after: Date)
    suspend fun updateSMS(sms: SMS)
}