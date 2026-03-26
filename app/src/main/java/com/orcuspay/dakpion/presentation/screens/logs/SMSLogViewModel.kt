package com.orcuspay.dakpion.presentation.screens.logs

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orcuspay.dakpion.domain.model.Credential
import com.orcuspay.dakpion.domain.model.CredentialWithSMS
import com.orcuspay.dakpion.domain.model.SMS
import com.orcuspay.dakpion.domain.repository.DakpionRepository
import com.orcuspay.dakpion.util.toSimpleDateString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class SMSLogViewModel @Inject constructor(
    private val dakpionRepository: DakpionRepository,
) : ViewModel() {

    fun getCredentialsWithSMS(): LiveData<List<CredentialWithSMS>> {
        return dakpionRepository.getCredentialWithSMSLiveData()
    }

    fun groupSMSByDate(smsList: List<SMS>): Map<String, List<SMS>> {
        return smsList
            .sortedByDescending { it.date }
            .groupBy { it.date.toSimpleDateString() }
    }

    fun retry(credential: Credential, sms: SMS) {
        viewModelScope.launch {
            Log.d("kraken", "Retry Sending $sms")
            dakpionRepository.send(
                credential = credential,
                sms = sms
            )
        }
    }

    fun exportToCsv(context: Context, credentialName: String, smsList: List<SMS>) {
        viewModelScope.launch {
            val csvHeader = "ID,Sender,Date,Body,Status\n"
            val csvContent = StringBuilder(csvHeader)
            
            smsList.forEach { sms ->
                csvContent.append("${sms.smsId},${sms.sender},${sms.date},\"${sms.body.replace("\"", "\"\"")}\",${sms.status}\n")
            }

            try {
                val filename = "dakpion_logs_${credentialName.lowercase().replace(" ", "_")}.csv"
                val file = File(context.cacheDir, filename)
                FileOutputStream(file).use {
                    it.write(csvContent.toString().toByteArray())
                }

                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Export Logs"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}