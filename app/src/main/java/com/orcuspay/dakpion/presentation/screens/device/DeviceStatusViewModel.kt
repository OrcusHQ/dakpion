package com.orcuspay.dakpion.presentation.screens.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orcuspay.dakpion.data.remote.ApiResult
import com.orcuspay.dakpion.domain.model.DeviceInfo
import com.orcuspay.dakpion.domain.model.SMS
import com.orcuspay.dakpion.domain.model.SMSStatus
import com.orcuspay.dakpion.domain.repository.DakpionRepository
import com.orcuspay.dakpion.domain.repository.SmsRepository
import com.orcuspay.dakpion.util.DakpionPreference
import com.orcuspay.dakpion.util.DeviceInfoProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class DeviceStatusViewModel @Inject constructor(
    private val deviceInfoProvider: DeviceInfoProvider,
    private val dakpionPreference: DakpionPreference,
    private val dakpionRepository: DakpionRepository,
    private val smsRepository: SmsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        DeviceStatusState(
            deviceInfo = deviceInfoProvider.getDeviceInfo(),
            lastSyncTime = dakpionPreference.getLastSyncTime(),
        )
    )
    val state: StateFlow<DeviceStatusState> = _state.asStateFlow()

    fun refreshDeviceInfo() {
        _state.update {
            it.copy(
                deviceInfo = deviceInfoProvider.getDeviceInfo(),
                lastSyncTime = dakpionPreference.getLastSyncTime(),
            )
        }
    }

    fun syncNow() {
        if (_state.value.syncing) return

        viewModelScope.launch {
            _state.update { it.copy(syncing = true, error = null, message = null) }
            try {
                dakpionRepository.syncCredentials()

                val recoveryStartTime = Date(System.currentTimeMillis() - 24L * 60L * 60L * 1000L)
                smsRepository.loadSMSAfter(recoveryStartTime)
                dakpionPreference.setLastSyncTime(Date())

                var attempted = 0
                var synced = 0
                var failed = 0

                dakpionRepository.getCredentialWithSMS().forEach { credentialWithSMS ->
                    val credential = credentialWithSMS.credential
                    if (!credential.enabled) return@forEach

                    credentialWithSMS.smsList
                        .filter { it.shouldSync() }
                        .forEach { sms ->
                            attempted++
                            when (dakpionRepository.send(credential, sms)) {
                                is ApiResult.Success -> synced++
                                is ApiResult.Error -> failed++
                            }
                        }
                }

                val message = if (attempted == 0) {
                    "No recent pending SMS to sync"
                } else {
                    "Synced $synced of $attempted recent pending SMS"
                }

                _state.update {
                    it.copy(
                        syncing = false,
                        deviceInfo = deviceInfoProvider.getDeviceInfo(),
                        lastSyncTime = dakpionPreference.getLastSyncTime(),
                        message = message,
                        error = if (failed > 0) "$failed SMS need another retry" else null,
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        syncing = false,
                        error = e.message ?: "Manual sync failed",
                    )
                }
            }
        }
    }

    private fun SMS.shouldSync(): Boolean {
        return status == SMSStatus.PROCESSING ||
            status == SMSStatus.ERROR ||
            status == SMSStatus.SUSPICIOUS ||
            status == SMSStatus.NOT_STORED
    }
}

data class DeviceStatusState(
    val deviceInfo: DeviceInfo,
    val lastSyncTime: Date?,
    val syncing: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)
