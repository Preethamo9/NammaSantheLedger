package com.nammasanthe.ledger.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammasanthe.ledger.data.db.TransactionType
import com.nammasanthe.ledger.data.db.model.TransactionWithCustomer
import com.nammasanthe.ledger.data.repository.LedgerRepository
import com.nammasanthe.ledger.security.SecurePrefsManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.io.File
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class LedgerViewModel(
    private val repo: LedgerRepository,
    private val securePrefs: SecurePrefsManager
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    val search: StateFlow<String> = searchQuery

    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf

    private val _pdfFile = MutableStateFlow<File?>(null)
    val pdfFile: StateFlow<File?> = _pdfFile

    val transactions: StateFlow<List<TransactionWithCustomer>> = searchQuery
        .flatMapLatest { q ->
            if (q.isBlank()) repo.observeRecentTransactions(200)
            else repo.observeLedgerSearch(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearch(q: String) {
        searchQuery.value = q
    }

    suspend fun getExportData(
        startDate: Long? = null,
        endDate: Long? = null
    ): ExportData {
        val query = searchQuery.value.trim()
        var allTx = if (query.isBlank()) {
            repo.getAllTransactionsForExport()
        } else {
            repo.getAllTransactionsForExport().filter { 
                it.customerName.contains(query, ignoreCase = true) 
            }
        }

        // Apply Date Filtering with proper Day Boundary handling
        if (startDate != null || endDate != null) {
            val startTs = startDate?.let {
                Calendar.getInstance().apply {
                    timeInMillis = it
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            } ?: 0L

            val endTs = endDate?.let {
                Calendar.getInstance().apply {
                    timeInMillis = it
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
            } ?: Long.MAX_VALUE

            Log.d("LedgerViewModel", "Filtering transactions between $startTs and $endTs")
            allTx = allTx.filter { it.date in startTs..endTs }
        }

        Log.d("LedgerViewModel", "Exporting ${allTx.size} transactions")

        val totalCredit = allTx.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount }
        val totalPayment = allTx.filter { it.type == TransactionType.PAYMENT }.sumOf { it.amount }
        
        return ExportData(
            vendorName = securePrefs.getVendorName(),
            shopName = securePrefs.getShopName(),
            transactions = allTx,
            totalCredit = totalCredit,
            totalPayment = totalPayment
        )
    }

    fun setGeneratingPdf(generating: Boolean) {
        _isGeneratingPdf.value = generating
    }

    fun setPdfFile(file: File?) {
        _pdfFile.value = file
    }

    data class ExportData(
        val vendorName: String,
        val shopName: String,
        val transactions: List<TransactionWithCustomer>,
        val totalCredit: Int,
        val totalPayment: Int
    )
}
