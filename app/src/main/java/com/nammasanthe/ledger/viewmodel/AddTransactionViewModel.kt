package com.nammasanthe.ledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammasanthe.ledger.data.db.TransactionType
import com.nammasanthe.ledger.data.db.model.CustomerWithBalance
import com.nammasanthe.ledger.data.repository.LedgerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AddTransactionViewModel(private val repo: LedgerRepository) : ViewModel() {

    private val pickerSearchQuery = MutableStateFlow("")

    val customers: StateFlow<List<CustomerWithBalance>> =
        repo.observeCustomers("")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pickerCustomers: StateFlow<List<CustomerWithBalance>> = combine(
        customers,
        pickerSearchQuery
    ) { list, query ->
        if (query.isBlank()) list
        else list.filter { it.customer.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pickerSearch: StateFlow<String> = pickerSearchQuery

    fun setPickerSearch(query: String) {
        pickerSearchQuery.value = query
    }

    fun clearPickerSearch() {
        pickerSearchQuery.value = ""
    }

    suspend fun addCustomer(name: String, phone: String?): Result<Long> = try {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            Result.failure(IllegalArgumentException("Name required"))
        } else {
            Result.success(repo.insertCustomer(trimmed, phone?.trim()?.takeIf { it.isNotEmpty() }))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun save(
        customerId: Long,
        amount: Int,
        type: TransactionType,
        dateMillis: Long,
        dueDays: Int,
        note: String?
    ): Result<Unit> {
        return try {
            if (amount <= 0) {
                Result.failure(IllegalArgumentException("Invalid amount"))
            } else {
                repo.addTransaction(customerId, amount, type, dateMillis, dueDays, note)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
