package com.orcuspay.dakpion.presentation.screens.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orcuspay.dakpion.domain.model.Filter
import com.orcuspay.dakpion.domain.repository.FilterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNewFilterViewModel @Inject constructor(
    private val filterRepository: FilterRepository
) : ViewModel() {
    fun createFilter(
        sender: String,
        value: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            filterRepository.createFilter(
                Filter(
                    sender = sender.ifBlank { null },
                    value = value,
                    enabled = true,
                )
            )
            onSuccess()
        }
    }
}