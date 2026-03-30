package com.orcuspay.dakpion.presentation.screens.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.orcuspay.dakpion.data.local.SenderStat
import com.orcuspay.dakpion.data.local.StatusStat
import com.orcuspay.dakpion.domain.model.SMS
import com.orcuspay.dakpion.domain.repository.DakpionRepository
import com.orcuspay.dakpion.util.toSimpleDateString
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DakpionRepository
) : ViewModel() {

    val totalPayments: LiveData<Int> = repository.getTotalPaymentsCount()
    val statsBySender: LiveData<List<SenderStat>> = repository.getPaymentCountBySender()
    val statsByStatus: LiveData<List<StatusStat>> = repository.getPaymentCountByStatus()
    val allStoredSMS: LiveData<List<SMS>> = repository.getAllStoredSMS()

    fun getPaymentsByDay(smsList: List<SMS>): Map<String, Int> {
        return smsList.groupBy { it.date.toSimpleDateString() }
            .mapValues { it.value.size }
    }
}
