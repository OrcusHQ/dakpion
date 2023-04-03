package com.orcuspay.dakpion.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.orcuspay.dakpion.data.local.DakpionDatabase
import com.orcuspay.dakpion.data.mapper.toFilter
import com.orcuspay.dakpion.data.mapper.toFilterEntity
import com.orcuspay.dakpion.domain.model.Filter
import com.orcuspay.dakpion.domain.repository.FilterRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterRepositoryImp @Inject constructor(
    private val db: DakpionDatabase
) : FilterRepository {

    private val dao = db.dao

    override suspend fun getFilters(): List<Filter> {
        return dao.getFilters().map {
            it.toFilter()
        }
    }

    override suspend fun getEnabledFilters(): List<Filter> {
        return dao.getEnabledFilters().map {
            it.toFilter()
        }
    }

    override fun getFiltersLiveData(): LiveData<List<Filter>> {
        return Transformations.map(dao.getFiltersLiveData()) {
            it.map { fe ->
                fe.toFilter()
            }
        }
    }

    override suspend fun createFilter(filter: Filter) {
        dao.createFilter(filter.toFilterEntity())
    }

    override suspend fun deleteFilter(filter: Filter) {
        dao.deleteFilter(filter.toFilterEntity())
    }

    override suspend fun updateFilter(filter: Filter) {
        dao.updateFilter(filter.toFilterEntity())
    }
}