package com.nammasanthe.ledger.data.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.nammasanthe.ledger.data.db.AppDatabase
import com.nammasanthe.ledger.data.db.TransactionType
import com.nammasanthe.ledger.data.db.entity.CustomerEntity
import com.nammasanthe.ledger.data.db.entity.ReminderLogEntity
import com.nammasanthe.ledger.data.db.entity.TransactionEntity
import com.nammasanthe.ledger.data.db.model.CustomerWithBalance
import com.nammasanthe.ledger.data.db.model.TransactionWithCustomer
import com.nammasanthe.ledger.util.DayBounds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class LedgerRepository(private val db: AppDatabase) {

    private val customerDao = db.customerDao()
    private val transactionDao = db.transactionDao()
    private val reminderLogDao = db.reminderLogDao()
    private val firestore = Firebase.firestore

    fun observeCustomers(search: String = ""): Flow<List<CustomerWithBalance>> =
        if (search.isBlank()) {
            customerDao.observeAllWithBalance()
        } else {
            customerDao.searchWithBalance(search.trim())
        }.map { rows -> rows.map { it.toModel() } }

    fun observeCustomer(id: Long) = customerDao.observeById(id)

    fun observeBalance(customerId: Long): Flow<Int> =
        customerDao.observeBalance(customerId).map { it ?: 0 }

    suspend fun insertCustomer(name: String, phone: String? = null): Long {
        val id = customerDao.insert(CustomerEntity(name = name.trim(), phoneNumber = phone?.trim()))
        syncCustomerToFirestore(id)
        return id
    }

    suspend fun updateCustomer(customer: CustomerEntity) {
        customerDao.update(customer)
        syncCustomerToFirestore(customer.id)
    }

    suspend fun deleteCustomer(customer: CustomerEntity) {
        customerDao.delete(customer)
        firestore.collection("customers").document(customer.id.toString()).delete().await()
    }

    private suspend fun syncCustomerToFirestore(customerId: Long) {
        val customer = customerDao.getById(customerId) ?: return
        firestore.collection("customers").document(customer.id.toString())
            .set(customer)
            .await()
    }

    fun observeTransactionsForCustomer(customerId: Long): Flow<List<TransactionWithCustomer>> =
        transactionDao.observeForCustomer(customerId)
            .map { rows -> rows.map { it.toModel() } }

    fun observeRecentTransactions(limit: Int = 20): Flow<List<TransactionWithCustomer>> =
        transactionDao.observeRecent(limit).map { rows -> rows.map { it.toModel() } }

    fun observeLedgerSearch(query: String): Flow<List<TransactionWithCustomer>> =
        transactionDao.searchLedger(query.trim()).map { rows -> rows.map { it.toModel() } }

    fun observeTotalOutstanding(): Flow<Int> =
        transactionDao.observeTotalOutstanding().map { it ?: 0 }

    fun observeTodaySales(day: DayBounds): Flow<Int> =
        transactionDao.observeDayTotal(TransactionType.CREDIT, day.start, day.end)
            .map { it ?: 0 }

    fun observeTodayPayments(day: DayBounds): Flow<Int> =
        transactionDao.observeDayTotal(TransactionType.PAYMENT, day.start, day.end)
            .map { it ?: 0 }

    fun observeDailyCustomerBalances() =
        transactionDao.observeCustomersWithNonZeroBalance()

    suspend fun getAllTransactionsForExport(): List<TransactionWithCustomer> =
        transactionDao.getAllForExport().map { it.toModel() }

    suspend fun addTransaction(
        customerId: Long,
        amount: Int,
        type: TransactionType,
        date: Long,
        dueDays: Int = 0,
        note: String? = null
    ): Long {
        val dueDate = if (type == TransactionType.CREDIT && dueDays > 0) {
            date + dueDays * 86_400_000L
        } else null
        val id = transactionDao.insert(
            TransactionEntity(
                customerId = customerId,
                amount = amount,
                type = type,
                date = date,
                dueDays = if (type == TransactionType.CREDIT) dueDays else 0,
                dueDate = dueDate,
                note = note?.trim()?.takeIf { it.isNotEmpty() }
            )
        )
        syncTransactionToFirestore(id)
        return id
    }

    private suspend fun syncTransactionToFirestore(transactionId: Long) {
        val transaction = transactionDao.getById(transactionId) ?: return
        firestore.collection("transactions").document(transaction.id.toString())
            .set(transaction)
            .await()
    }

    suspend fun getOverdueForReminders(now: Long = System.currentTimeMillis()) =
        reminderLogDao.getOverdueCustomersWithPhone(now)

    suspend fun getReminderLog(customerId: Long) =
        reminderLogDao.getForCustomer(customerId)

    suspend fun recordReminderSent(customerId: Long, at: Long = System.currentTimeMillis()) {
        reminderLogDao.upsert(ReminderLogEntity(customerId, at))
    }
}
