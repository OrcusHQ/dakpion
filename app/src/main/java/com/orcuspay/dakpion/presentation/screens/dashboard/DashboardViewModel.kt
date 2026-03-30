package com.orcuspay.dakpion.presentation.screens.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.orcuspay.dakpion.data.local.SenderStat
import com.orcuspay.dakpion.data.local.StatusStat
import com.orcuspay.dakpion.domain.model.SMS
import com.orcuspay.dakpion.domain.repository.DakpionRepository
import com.orcuspay.dakpion.util.toSimpleDateString
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DakpionRepository
) : ViewModel() {

    val totalPayments: LiveData<Int> = repository.getTotalPaymentsCount()
    val statsBySender: LiveData<List<SenderStat>> = repository.getPaymentCountBySender()
    val statsByStatus: LiveData<List<StatusStat>> = repository.getPaymentCountByStatus()
    val allStoredSMS: LiveData<List<SMS>> = repository.getAllStoredSMS()

    fun getTodayCount(smsList: List<SMS>): Int {
        val today = Calendar.getInstance()
        return smsList.count { sms ->
            val c = Calendar.getInstance().apply { time = sms.date }
            c.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    c.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        }
    }

    fun getThisWeekCount(smsList: List<SMS>): Int {
        val sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        return smsList.count { it.date.time >= sevenDaysAgo }
    }

    fun getTotalAmount(smsList: List<SMS>): Double {
        return smsList.sumOf { it.amount ?: 0.0 }
    }

    fun getRecentPaymentsByDay(smsList: List<SMS>): List<Pair<String, Int>> {
        val sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        return smsList
            .filter { it.date.time >= sevenDaysAgo }
            .groupBy { sms ->
                Calendar.getInstance().apply { time = sms.date }.let { c ->
                    Triple(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
                }
            }
            .entries
            .sortedByDescending { (d, _) -> d.first * 10000 + d.second * 100 + d.third }
            .map { (_, group) -> group.first().date.toSimpleDateString() to group.size }
    }

    fun getPaymentsByDay(smsList: List<SMS>): Map<String, Int> {
        return smsList.groupBy { it.date.toSimpleDateString() }
            .mapValues { it.value.size }
    }
}
