package com.orcuspay.dakpion.domain.repository

import androidx.lifecycle.LiveData
import com.orcuspay.dakpion.domain.model.Filter


interface FilterRepository {
    suspend fun getFilters(): List<Filter>
    fun getFiltersLiveData(): LiveData<List<Filter>>
    suspend fun createFilter(filter: Filter)
    suspend fun deleteFilter(filter: Filter)
    suspend fun updateFilter(filter: Filter)
    suspend fun getEnabledFilters(): List<Filter>
}