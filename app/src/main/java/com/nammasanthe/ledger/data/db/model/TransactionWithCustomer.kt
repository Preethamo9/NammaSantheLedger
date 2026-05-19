package com.nammasanthe.ledger.data.db.model

import com.nammasanthe.ledger.data.db.TransactionType
import com.nammasanthe.ledger.data.db.entity.TransactionEntity

data class TransactionWithCustomer(
    val transaction: TransactionEntity,
    val customerName: String,
    val customerPhone: String?
) {
    val id get() = transaction.id
    val amount get() = transaction.amount
    val type: TransactionType get() = transaction.type
    val date get() = transaction.date
    val dueDate get() = transaction.dueDate
    val dueDays get() = transaction.dueDays
    val note get() = transaction.note
    val customerId get() = transaction.customerId
}
