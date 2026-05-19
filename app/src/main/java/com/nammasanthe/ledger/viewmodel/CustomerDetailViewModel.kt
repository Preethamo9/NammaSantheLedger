package com.nammasanthe.ledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammasanthe.ledger.data.db.entity.CustomerEntity
import com.nammasanthe.ledger.data.db.model.TransactionWithCustomer
import com.nammasanthe.ledger.data.repository.LedgerRepository
import com.nammasanthe.ledger.security.SecurePrefsManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CustomerDetailUiState(
    val customer: CustomerEntity? = null,
    val balance: Int = 0,
    val transactions: List<TransactionWithCustomer> = emptyList(),
    val vendorName: String = "",
    val shopName: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
class CustomerDetailViewModel(
    private val repo: LedgerRepository,
    private val prefs: SecurePrefsManager
) : ViewModel() {

    private val customerId = MutableStateFlow(0L)

    fun load(id: Long) {
        customerId.value = id
    }

    val uiState: StateFlow<CustomerDetailUiState> = customerId
        .flatMapLatest { id ->
            if (id <= 0) return@flatMapLatest flowOf(CustomerDetailUiState())
            kotlinx.coroutines.flow.combine(
                repo.observeCustomer(id),
                repo.observeBalance(id),
                repo.observeTransactionsForCustomer(id)
            ) { customer, balance, txs ->
                CustomerDetailUiState(
                    customer = customer,
                    balance = balance,
                    transactions = txs,
                    vendorName = prefs.getVendorName(),
                    shopName = prefs.getShopName()
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CustomerDetailUiState())

    fun updatePhone(phone: String?) {
        val c = uiState.value.customer ?: return
        viewModelScope.launch {
            repo.updateCustomer(c.copy(phoneNumber = phone?.trim()?.takeIf { it.isNotEmpty() }))
        }
    }

    fun deleteCustomer(onDone: () -> Unit) {
        val c = uiState.value.customer ?: return
        viewModelScope.launch {
            repo.deleteCustomer(c)
            onDone()
        }
    }
}
