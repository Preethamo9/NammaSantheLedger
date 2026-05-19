package com.nammasanthe.ledger.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nammasanthe.ledger.data.db.entity.ReminderLogEntity

@Dao
interface ReminderLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: ReminderLogEntity)

    @Query("SELECT * FROM reminder_logs WHERE customerId = :customerId")
    suspend fun getForCustomer(customerId: Long): ReminderLogEntity?

    @Query(
        """
        SELECT c.id, c.name, c.phoneNumber,
            COALESCE(SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE 0 END), 0)
            - COALESCE(SUM(CASE WHEN t.type = 'PAYMENT' THEN t.amount ELSE 0 END), 0) AS balance
        FROM customers c
        INNER JOIN transactions t ON t.customerId = c.id
        WHERE c.phoneNumber IS NOT NULL AND c.phoneNumber != ''
        GROUP BY c.id
        HAVING balance > 0
        AND EXISTS (
            SELECT 1 FROM transactions tx
            WHERE tx.customerId = c.id
            AND tx.type = 'CREDIT'
            AND tx.dueDate IS NOT NULL
            AND tx.dueDate < :now
        )
        """
    )
    suspend fun getOverdueCustomersWithPhone(now: Long): List<OverdueCustomerRow>

    data class OverdueCustomerRow(
        val id: Long,
        val name: String,
        val phoneNumber: String,
        val balance: Int
    )
}
