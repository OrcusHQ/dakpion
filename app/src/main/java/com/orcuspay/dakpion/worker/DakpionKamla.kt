package com.orcuspay.dakpion.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.orcuspay.dakpion.data.remote.ApiResult
import com.orcuspay.dakpion.domain.model.SMSStatus
import com.orcuspay.dakpion.domain.repository.DakpionRepository
import com.orcuspay.dakpion.domain.repository.SmsRepository
import com.orcuspay.dakpion.util.DakpionPreference
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.*

@HiltWorker
class DakpionKamla @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    private val dakpionRepository: DakpionRepository,
    private val smsRepository: SmsRepository,
    private val dakpionPreference: DakpionPreference,
) : CoroutineWorker(
    appContext = context,
    params = workerParams
) {

    companion object {
        const val TAG = "dakpionkamla"
    }

    override suspend fun doWork(): Result {
        try {
            val lastSyncTime = dakpionPreference.getLastSyncTime() ?: return Result.retry()
            smsRepository.loadSMSAfter(after = lastSyncTime)
            dakpionPreference.setLastSyncTime(Date())

            val credentialWithSMSList = dakpionRepository.getCredentialWithSMS()

            var hasError = false
            credentialWithSMSList.forEach { cs ->
                val credential = cs.credential
                val smsList = cs.smsList

                if (credential.enabled) {
                    smsList
                        .filter {
                            (it.status == SMSStatus.PROCESSING ||
                                    it.status == SMSStatus.ERROR) && it.status != SMSStatus.DUPLICATE
                        }
                        .forEach { sms ->
                            val result = dakpionRepository.send(
                                credential = credential,
                                sms = sms
                            )
                            when (result) {
                                is ApiResult.Error -> {
                                    hasError = true
                                }
                                is ApiResult.Success -> {

                                }
                            }
                        }
                }
            }

            if (hasError) {
                return Result.retry()
            }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("kraken", "Exception in DakpionKamla")
            return Result.retry()
        }
    }
}