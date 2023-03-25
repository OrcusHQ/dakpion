package com.orcuspay.dakpion.presentation.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orcuspay.dakpion.domain.model.Credential
import com.orcuspay.dakpion.domain.repository.DakpionRepository
import com.orcuspay.dakpion.util.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val text: String,
    private val dakpionRepository: DakpionRepository,
    private val notificationHelper: NotificationHelper,
) : ViewModel() {

    var state by mutableStateOf(HomeState())

    init {
        viewModelScope.launch {
            dakpionRepository.syncCredentials()
            val credentials = dakpionRepository.getCredentials()
            credentials.forEach {
                if (it.unauthorized) {
                    notificationHelper.showNotification(
                        notificationId = it.id,
                        title = "Invalid credentials",
                        content = "${it.businessName} has invalid credentials. We have disabled it."
                    )
                }
            }
        }
    }

    fun getCredentials(): LiveData<List<Credential>> {
        return dakpionRepository.getCredentialsLiveData()
    }

    fun deleteCredential(credential: Credential) {
        viewModelScope.launch {
            dakpionRepository.deleteCredential(credential)
        }
    }

    fun setCredentialEnabled(credential: Credential, enabled: Boolean) {
        viewModelScope.launch {
            dakpionRepository.setCredentialEnabled(credential, enabled)
        }
    }

}


data class HomeState(
    val loading: Boolean = false
)