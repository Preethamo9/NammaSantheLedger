package com.nammasanthe.ledger.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nammasanthe.ledger.R
import com.nammasanthe.ledger.data.db.TransactionType

@Composable
fun transactionTypeLabel(type: TransactionType): String = when (type) {
    TransactionType.CREDIT -> stringResource(R.string.credit)
    TransactionType.PAYMENT -> stringResource(R.string.payment)
}
