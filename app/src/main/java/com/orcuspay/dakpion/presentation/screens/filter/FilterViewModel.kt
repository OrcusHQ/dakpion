package com.orcuspay.dakpion.presentation.screens.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orcuspay.dakpion.domain.model.Filter
import com.orcuspay.dakpion.domain.repository.FilterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilterViewModel @Inject constructor(
    private val filterRepository: FilterRepository
) : ViewModel() {

    val filtersLiveData = filterRepository.getFiltersLiveData()

    fun deleteFilter(filter: Filter) {
        viewModelScope.launch {
            filterRepository.deleteFilter(filter)
        }
    }

    fun setFilterEnabled(filter: Filter, enabled: Boolean) {
        viewModelScope.launch {
            filterRepository.updateFilter(filter.copy(enabled = enabled))
        }
    }
}