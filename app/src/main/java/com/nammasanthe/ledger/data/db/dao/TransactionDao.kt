package com.nammasanthe.ledger.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nammasanthe.ledger.data.db.TransactionType
import com.nammasanthe.ledger.data.db.entity.TransactionEntity
import com.nammasanthe.ledger.data.db.model.TransactionWithCustomer
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query(
        """
        SELECT t.*, c.name AS customerName, c.phoneNumber AS customerPhone
        FROM transactions t
        INNER JOIN customers c ON c.id = t.customerId
        WHERE t.customerId = :customerId
        ORDER BY t.date DESC, t.id DESC
        """
    )
    fun observeForCustomer(customerId: Long): Flow<List<TransactionWithCustomerRow>>

    @Query(
        """
        SELECT t.*, c.name AS customerName, c.phoneNumber AS customerPhone
        FROM transactions t
        INNER JOIN customers c ON c.id = t.customerId
        ORDER BY t.date DESC, t.id DESC
        LIMIT :limit
        """
    )
    fun observeRecent(limit: Int = 20): Flow<List<TransactionWithCustomerRow>>

    @Query(
        """
        SELECT t.*, c.name AS customerName, c.phoneNumber AS customerPhone
        FROM transactions t
        INNER JOIN customers c ON c.id = t.customerId
        WHERE c.name LIKE '%' || :query || '%'
        ORDER BY t.date DESC, t.id DESC
        """
    )
    fun searchLedger(query: String): Flow<List<TransactionWithCustomerRow>>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = :type AND date >= :dayStart AND date < :dayEnd
        """
    )
    fun observeDayTotal(type: TransactionType, dayStart: Long, dayEnd: Long): Flow<Int?>

    @Query(
        """
        SELECT COALESCE(SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END), 0)
        FROM transactions
        """
    )
    fun observeTotalOutstanding(): Flow<Int?>

    @Query(
        """
        SELECT c.id AS customerId, c.name AS customerName,
            COALESCE(SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE 0 END), 0)
            - COALESCE(SUM(CASE WHEN t.type = 'PAYMENT' THEN t.amount ELSE 0 END), 0) AS balance
        FROM customers c
        LEFT JOIN transactions t ON t.customerId = c.id
        GROUP BY c.id
        HAVING balance != 0
        ORDER BY balance DESC
        """
    )
    fun observeCustomersWithNonZeroBalance(): Flow<List<DailyCustomerBalanceRow>>

    @Query(
        """
        SELECT t.*, c.name AS customerName, c.phoneNumber AS customerPhone
        FROM transactions t
        INNER JOIN customers c ON c.id = t.customerId
        ORDER BY t.date DESC, t.id DESC
        """
    )
    suspend fun getAllForExport(): List<TransactionWithCustomerRow>

    data class TransactionWithCustomerRow(
        val id: Long,
        val customerId: Long,
        val amount: Int,
        val type: TransactionType,
        val date: Long,
        val dueDays: Int,
        val dueDate: Long?,
        val note: String?,
        val customerName: String,
        val customerPhone: String?
    ) {
        fun toModel() = TransactionWithCustomer(
            transaction = TransactionEntity(
                id, customerId, amount, type, date, dueDays, dueDate, note
            ),
            customerName = customerName,
            customerPhone = customerPhone
        )
    }

    data class DailyCustomerBalanceRow(
        val customerId: Long,
        val customerName: String,
        val balance: Int
    )
}
