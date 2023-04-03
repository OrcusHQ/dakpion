package com.orcuspay.dakpion.presentation.screens.business

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orcuspay.dakpion.data.remote.ApiResult
import com.orcuspay.dakpion.domain.model.Mode
import com.orcuspay.dakpion.domain.model.VerifyRequest
import com.orcuspay.dakpion.domain.repository.DakpionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNewBusinessViewModel @Inject constructor(
    val dakpionRepository: DakpionRepository,
) : ViewModel() {

    var state by mutableStateOf(AddNewBusinessState())

    fun verify(
        accessKey: String,
        secretKey: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            val result = dakpionRepository.verify(
                VerifyRequest(
                    accessKey = accessKey,
                    secretKey = secretKey,
                )
            )
            when (result) {
                is ApiResult.Error -> {
                    state = state.copy(loading = false, error = result.message)
                }
                is ApiResult.Success -> {
                    state = state.copy(loading = false)
                    onSuccess()
                }
            }
        }
    }
}

data class AddNewBusinessState(
    val loading: Boolean = false,
    val error: String? = null,
)