package com.orcuspay.dakpion.data.repository

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.Telephony
import android.util.Log
import com.orcuspay.dakpion.data.local.DakpionDatabase
import com.orcuspay.dakpion.data.mapper.toCredential
import com.orcuspay.dakpion.data.mapper.toSMSEntity
import com.orcuspay.dakpion.domain.model.SMS
import com.orcuspay.dakpion.domain.model.SMSStatus
import com.orcuspay.dakpion.domain.repository.FilterRepository
import com.orcuspay.dakpion.domain.repository.SmsRepository
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SmsRepositoryImp @Inject constructor(
    application: Application,
    db: DakpionDatabase,
    private val filterRepository: FilterRepository,
) : SmsRepository {

    private val dao = db.dao
    private val context: Context = application.applicationContext

    override suspend fun loadSMSAfter(after: Date) {
        val credentials = dao.getCredentials().map { it.toCredential() }
        val filters = filterRepository.getEnabledFilters()
        val contentResolver: ContentResolver = context.contentResolver

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
                Telephony.Sms.Inbox.BODY
            ),
            selection,
            selectionArgs,
            sortOrder,
        ) ?: return

        if (!cursor.moveToFirst()) {
            cursor.close()
            return
        }

        while (!cursor.isAfterLast) {
            val id = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.Inbox._ID))
            val creator = cursor.getString(cursor.getColumnIndex(Telephony.Sms.Inbox.ADDRESS))
            val date = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.Inbox.DATE))
            val body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.Inbox.BODY))

            credentials.forEach { credential ->
                var sms = SMS(
                    smsId = id,
                    credentialId = credential.id,
                    sender = creator,
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
                )

                if (
                    supportedSenders.any { f ->
                        sms.sender.lowercase().contains(f.lowercase())
                    } && credential.enabled
                ) {

                    if (sms.sender.lowercase().contains("ibbl")) {
                        if (!sms.body.lowercase().contains("cellfin")) {
                            return
                        }
                    }

                    if (filters.filter { filter ->
                            filter.sender == null || filter.sender.lowercase() == sms.sender.lowercase()
                        }.any { filter ->
                            filter.match(sms.body)
                        }) {
                        sms = sms.copy(status = SMSStatus.FILTERED)
                    }

                    val smsEntity = sms.toSMSEntity()
                    Log.d("kraken", "Created $smsEntity")
                    dao.createSMS(smsEntity)
                }
            }

            cursor.moveToNext()
        }


        cursor.close()
    }

    override suspend fun updateSMS(sms: SMS) {
        dao.updateSMS(sms.toSMSEntity())
    }
}
