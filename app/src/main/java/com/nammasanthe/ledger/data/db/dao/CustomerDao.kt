package com.nammasanthe.ledger.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nammasanthe.ledger.data.db.entity.CustomerEntity
import com.nammasanthe.ledger.data.db.model.CustomerWithBalance
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(customer: CustomerEntity): Long

    @Update
    suspend fun update(customer: CustomerEntity)

    @Delete
    suspend fun delete(customer: CustomerEntity)

    @Query("SELECT * FROM customers WHERE id = :id")
    fun observeById(id: Long): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: Long): CustomerEntity?

    @Query(
        """
        SELECT c.*,
            COALESCE(SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE 0 END), 0)
            - COALESCE(SUM(CASE WHEN t.type = 'PAYMENT' THEN t.amount ELSE 0 END), 0) AS balance
        FROM customers c
        LEFT JOIN transactions t ON t.customerId = c.id
        GROUP BY c.id
        ORDER BY c.name COLLATE NOCASE ASC
        """
    )
    fun observeAllWithBalance(): Flow<List<CustomerWithBalanceRow>>

    @Query(
        """
        SELECT c.*,
            COALESCE(SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE 0 END), 0)
            - COALESCE(SUM(CASE WHEN t.type = 'PAYMENT' THEN t.amount ELSE 0 END), 0) AS balance
        FROM customers c
        LEFT JOIN transactions t ON t.customerId = c.id
        WHERE c.name LIKE '%' || :query || '%'
        GROUP BY c.id
        ORDER BY c.name COLLATE NOCASE ASC
        """
    )
    fun searchWithBalance(query: String): Flow<List<CustomerWithBalanceRow>>

    @Query(
        """
        SELECT COALESCE(SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE 0 END), 0)
            - COALESCE(SUM(CASE WHEN t.type = 'PAYMENT' THEN t.amount ELSE 0 END), 0)
        FROM transactions t WHERE t.customerId = :customerId
        """
    )
    fun observeBalance(customerId: Long): Flow<Int?>

    data class CustomerWithBalanceRow(
        val id: Long,
        val name: String,
        val phoneNumber: String?,
        val createdAt: Long,
        val balance: Int
    ) {
        fun toModel() = CustomerWithBalance(
            customer = CustomerEntity(id, name, phoneNumber, createdAt),
            balance = balance
        )
    }
}
