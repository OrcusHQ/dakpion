package com.orcuspay.dakpion.presentation.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orcuspay.dakpion.domain.model.VerifyRequest
import com.orcuspay.dakpion.domain.repository.DakpionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val text: String,
    private val dakpionRepository: DakpionRepository
) : ViewModel() {

    var state by mutableStateOf(HomeState())

    fun test() {
        viewModelScope.launch {
            state = state.copy(loading = true)
            dakpionRepository.verify(
                VerifyRequest(
                    accessKey = "string",
                    secretKey = "string",
                    mode = "test"
                )
            )
            state = state.copy(loading = false)
        }
    }

}


data class HomeState(
    val loading: Boolean = false
)