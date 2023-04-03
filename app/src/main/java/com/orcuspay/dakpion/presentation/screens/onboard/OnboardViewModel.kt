package com.orcuspay.dakpion.presentation.screens.onboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orcuspay.dakpion.domain.model.Filter
import com.orcuspay.dakpion.domain.repository.FilterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardViewModel @Inject constructor(
    private val filterRepository: FilterRepository,
) : ViewModel() {
    fun addDefaultFilters() {
        viewModelScope.launch {
            if (filterRepository.getFilters().isEmpty()) {
                filterRepository.createFilter(Filter(sender = null, value = "/verification code|otp|pin/", enabled = true))
            }
        }
    }
}