package com.nammasanthe.ledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammasanthe.ledger.data.db.dao.TransactionDao
import com.nammasanthe.ledger.data.repository.LedgerRepository
import com.nammasanthe.ledger.util.DayBounds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class DailySummaryUiState(
    val sales: Int = 0,
    val payments: Int = 0,
    val customers: List<TransactionDao.DailyCustomerBalanceRow> = emptyList(),
    val dayMillis: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalCoroutinesApi::class)
class DailySummaryViewModel(private val repo: LedgerRepository) : ViewModel() {

    private val selectedDay = MutableStateFlow(DayBounds.today())

    val uiState: StateFlow<DailySummaryUiState> = selectedDay
        .flatMapLatest { day ->
            combine(
                repo.observeTodaySales(day),
                repo.observeTodayPayments(day),
                repo.observeDailyCustomerBalances()
            ) { sales, payments, customers ->
                DailySummaryUiState(
                    sales = sales,
                    payments = payments,
                    customers = customers,
                    dayMillis = day.start
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailySummaryUiState())

    fun setDay(millis: Long) {
        selectedDay.value = DayBounds.forDate(millis)
    }
}
