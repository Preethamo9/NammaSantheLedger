package com.nammasanthe.ledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammasanthe.ledger.data.db.model.TransactionWithCustomer
import com.nammasanthe.ledger.data.repository.LedgerRepository
import com.nammasanthe.ledger.util.DayBounds
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val totalOutstanding: Int = 0,
    val todaySales: Int = 0,
    val todayPayments: Int = 0,
    val recent: List<TransactionWithCustomer> = emptyList()
)

class HomeViewModel(private val repo: LedgerRepository) : ViewModel() {

    private val today = DayBounds.today()

    val uiState: StateFlow<HomeUiState> = combine(
        repo.observeTotalOutstanding(),
        repo.observeTodaySales(today),
        repo.observeTodayPayments(today),
        repo.observeRecentTransactions(15)
    ) { outstanding, sales, payments, recent ->
        HomeUiState(outstanding, sales, payments, recent)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
}
