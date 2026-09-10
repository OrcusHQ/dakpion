package com.orcuspay.dakpion.data.repository

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.Telephony
import android.util.Log
import com.orcuspay.dakpion.data.local.DakpionDatabase
import com.orcuspay.dakpion.data.mapper.toCredential
import com.orcuspay.dakpion.data.mapper.toSMS
import com.orcuspay.dakpion.data.mapper.toSMSEntity
import com.orcuspay.dakpion.domain.model.SMS
import com.orcuspay.dakpion.domain.model.SMSStatus
import com.orcuspay.dakpion.domain.repository.FilterRepository
import com.orcuspay.dakpion.domain.repository.SmsRepository
import com.orcuspay.dakpion.util.DakpionPreference
import com.orcuspay.dakpion.util.SimInfoProvider
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SmsRepositoryImp @Inject constructor(
    application: Application,
    db: DakpionDatabase,
    private val filterRepository: FilterRepository,
    private val dakpionPreference: DakpionPreference,
    private val simInfoProvider: SimInfoProvider,
) : SmsRepository {

    private val dao = db.dao
    private val context: Context = application.applicationContext

    override suspend fun loadSMSAfter(after: Date) {
        val credentials = dao.getCredentials().map { it.toCredential() }
        val filters = filterRepository.getEnabledFilters()
        val contentResolver: ContentResolver = context.contentResolver

        // SIM filtering: when a specific slot is selected, only keep SMS that
        // arrived on that SIM. If we can't resolve an SMS's slot (permission
        // missing, or the device doesn't report it) we keep it, so we never
        // drop a real payment SMS just because the SIM was unknown.
        val simSlotFilter = dakpionPreference.getSimSlotFilter()
        val subIdToSlot: Map<Int, Int> =
            if (simSlotFilter != SimInfoProvider.SLOT_BOTH) {
                simInfoProvider.getSubscriptionIdToSlot()
            } else {
                emptyMap()
            }

        val selection = "${Telephony.Sms.Inbox.DATE} >= ?"
        val selectionArgs = arrayOf(
            "${after.time}"
        )
        val sortOrder = "${Telephony.Sms.Inbox.DATE} DESC"

        val cursor: Cursor = contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(
                Telephony.Sms.Inbox._ID,
                Telephony.Sms.Inbox.ADDRESS,
                Telephony.Sms.Inbox.DATE,
                Telephony.Sms.Inbox.BODY,
                Telephony.Sms.Inbox.SUBSCRIPTION_ID
            ),
            selection,
            selectionArgs,
            sortOrder,
        ) ?: return

        if (!cursor.moveToFirst()) {
            cursor.close()
            return
        }

        val subIdColumn = cursor.getColumnIndex(Telephony.Sms.Inbox.SUBSCRIPTION_ID)

        while (!cursor.isAfterLast) {
            val id = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.Inbox._ID))
            val creator = cursor.getString(cursor.getColumnIndex(Telephony.Sms.Inbox.ADDRESS))
            val date = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.Inbox.DATE))
            val body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.Inbox.BODY))

            // Skip SMS from a non-selected SIM (only when we can resolve its slot).
            if (simSlotFilter != SimInfoProvider.SLOT_BOTH && subIdColumn >= 0) {
                val subId = cursor.getInt(subIdColumn)
                val slot = subIdToSlot[subId]
                if (slot != null && slot != simSlotFilter) {
                    cursor.moveToNext()
                    continue
                }
            }

            credentials.forEach { credential ->
                var sms = SMS(
                    smsId = id,
                    credentialId = credential.id,
                    sender = creator ?: "Unknown",
                    date = Date(date),
                    body = body,
                    status = SMSStatus.PROCESSING,
                )

                val supportedSenders = listOf(
                    "bKash",
                    "nagad",
                    "upay",
                    "16216",
                    "IBBL",
                    "01847-348685",
                    "16259",
                    "pathaopay",
                    "telecash",
                    "ipay",
                    "tap",
                )

                if (
                    supportedSenders.any { f ->
                        sms.sender.lowercase().contains(f.lowercase())
                    } && credential.enabled
                ) {

                    if (sms.sender.lowercase().contains("ibbl")) {
                        if (!sms.body.lowercase().contains("cellfin")) {
                            return@forEach
                        }
                    }

                    // Extract amount and balance
                    val amount = extractAmount(body)
                    val currentBalance = extractBalance(body)
                    
                    sms = sms.copy(amount = amount, balance = currentBalance)

                    // Balance Verification
                    val lastSms = dao.getLastStoredSMS(credential.id, sms.sender)?.toSMS()
                    if (lastSms != null && amount != null && currentBalance != null && lastSms.balance != null) {
                        val expectedBalance = lastSms.balance + amount
                        // allow small difference for charges if any, but usually payments are additions
                        if (Math.abs(expectedBalance - currentBalance) > 0.01) {
                            sms = sms.copy(status = SMSStatus.SUSPICIOUS)
                        }
                    }

                    if (sms.status != SMSStatus.SUSPICIOUS) {
                        if (filters.filter { filter ->
                                filter.sender == null || filter.sender.lowercase() == sms.sender.lowercase()
                            }.any { filter ->
                                filter.match(sms.body)
                            }) {
                            sms = sms.copy(status = SMSStatus.FILTERED)
                        }
                    }

                    val smsEntity = sms.toSMSEntity()
                    Log.d("kraken", "Created $smsEntity")
                    try {
                        dao.createSMS(smsEntity)
                    } catch (e: Exception) {
                        // Likely duplicate, ignore
                    }
                }
            }

            cursor.moveToNext()
        }


        cursor.close()
    }

    private fun extractAmount(body: String): Double? {
        val patterns = listOf(
            Regex("Tk\\s?([0-9,]+\\.[0-9]{2})"),
            Regex("Amount:\\s?Tk\\s?([0-9,]+\\.[0-9]{2})"),
            Regex("received\\s?Tk\\s?([0-9,]+\\.[0-9]{2})")
        )
        for (pattern in patterns) {
            val match = pattern.find(body)
            if (match != null) {
                return match.groupValues[1].replace(",", "").toDoubleOrNull()
            }
        }
        return null
    }

    private fun extractBalance(body: String): Double? {
        val patterns = listOf(
            Regex("Balance:\\s?Tk\\s?([0-9,]+\\.[0-9]{2})"),
            Regex("Bal:\\s?Tk\\s?([0-9,]+\\.[0-9]{2})"),
            Regex("Current Balance\\s?Tk\\s?([0-9,]+\\.[0-9]{2})")
        )
        for (pattern in patterns) {
            val match = pattern.find(body)
            if (match != null) {
                return match.groupValues[1].replace(",", "").toDoubleOrNull()
            }
        }
        return null
    }

    override suspend fun updateSMS(sms: SMS) {
        dao.updateSMS(sms.toSMSEntity())
    }
}
