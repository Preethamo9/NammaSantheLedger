package com.nammasanthe.ledger.data.db.model

import com.nammasanthe.ledger.data.db.entity.CustomerEntity

data class CustomerWithBalance(
    val customer: CustomerEntity,
    val balance: Int
)
