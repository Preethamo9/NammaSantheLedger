package com.nammasanthe.ledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammasanthe.ledger.data.db.model.CustomerWithBalance
import com.nammasanthe.ledger.data.repository.LedgerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class CustomerFilter { ALL, PENDING, PAID }

class CustomersViewModel(private val repo: LedgerRepository) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val filter = MutableStateFlow(CustomerFilter.ALL)

    val search: StateFlow<String> = searchQuery
    val currentFilter: StateFlow<CustomerFilter> = filter

    val displayCustomers: StateFlow<List<CustomerWithBalance>> = combine(
        repo.observeCustomers(""),
        searchQuery,
        filter
    ) { list, query, f ->
        val searched = if (query.isBlank()) list else list.filter {
            it.customer.name.contains(query, ignoreCase = true)
        }
        when (f) {
            CustomerFilter.ALL -> searched
            CustomerFilter.PENDING -> searched.filter { it.balance > 0 }
            CustomerFilter.PAID -> searched.filter { it.balance == 0 }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearch(q: String) { searchQuery.value = q }
    fun setFilter(f: CustomerFilter) { filter.value = f }

    suspend fun addCustomer(name: String, phone: String?): Result<Long> = try {
        Result.success(repo.insertCustomer(name, phone))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
