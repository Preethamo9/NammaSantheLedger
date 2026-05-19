package com.nammasanthe.ledger.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nammasanthe.ledger.R
import com.nammasanthe.ledger.data.db.TransactionType
import com.nammasanthe.ledger.ui.components.DueDateLabel
import com.nammasanthe.ledger.ui.components.ResponsiveTwoPane
import com.nammasanthe.ledger.ui.components.fintech.FintechFab
import com.nammasanthe.ledger.ui.components.fintech.FintechScreenBackground
import com.nammasanthe.ledger.ui.components.fintech.FintechTransactionRow
import com.nammasanthe.ledger.ui.components.fintech.HomeWelcomeHeader
import com.nammasanthe.ledger.ui.components.fintech.OutstandingGradientCard
import com.nammasanthe.ledger.ui.components.fintech.SectionTitle
import com.nammasanthe.ledger.ui.components.fintech.StatMiniCard
import com.nammasanthe.ledger.ui.theme.CreditPinkSoft
import com.nammasanthe.ledger.ui.theme.Dimens
import com.nammasanthe.ledger.ui.theme.PaidGreen
import com.nammasanthe.ledger.ui.theme.PaymentGreenSoft
import com.nammasanthe.ledger.ui.theme.PendingRed
import com.nammasanthe.ledger.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    widthClass: WindowWidthSizeClass,
    vendorName: String,
    shopName: String,
    onAddTransaction: () -> Unit,
    onOpenCustomer: (Long) -> Unit,
    onOpenSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val dateFmt = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val businessName = remember(vendorName, shopName) {
        when {
            shopName.isNotBlank() -> shopName
            vendorName.isNotBlank() -> vendorName
            else -> ""
        }
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        floatingActionButton = {
            FintechFab(onClick = onAddTransaction)
        }
    ) { padding ->
        FintechScreenBackground {
            ResponsiveTwoPane(widthClass, main = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(
                        horizontal = Dimens.screenPadding,
                        vertical = Dimens.screenPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.itemSpacing)
                ) {
                    item {
                        HomeWelcomeHeader(
                            welcomeText = stringResource(R.string.welcome_back),
                            businessName = businessName,
                            onSettingsClick = onOpenSettings
                        )
                    }
                    item {
                        OutstandingGradientCard(
                            label = stringResource(R.string.total_outstanding),
                            amount = "₹${state.totalOutstanding}",
                            subtitle = stringResource(R.string.outstanding_subtitle)
                        )
                    }
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatMiniCard(
                                title = stringResource(R.string.todays_sales),
                                amount = "₹${state.todaySales}",
                                icon = Icons.Default.ArrowUpward,
                                containerColor = CreditPinkSoft,
                                iconTint = PendingRed,
                                modifier = Modifier.weight(1f)
                            )
                            StatMiniCard(
                                title = stringResource(R.string.todays_payments),
                                amount = "₹${state.todayPayments}",
                                icon = Icons.Default.ArrowDownward,
                                containerColor = PaymentGreenSoft,
                                iconTint = PaidGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        Spacer(Modifier.height(8.dp))
                        SectionTitle(stringResource(R.string.recent_transactions))
                    }
                    itemsIndexed(state.recent, key = { _, tx -> tx.id }) { index, tx ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
                        ) {
                            Column {
                                FintechTransactionRow(
                                    customerName = tx.customerName,
                                    dateLabel = dateFmt.format(Date(tx.date)),
                                    amount = tx.amount,
                                    type = tx.type,
                                    onClick = { onOpenCustomer(tx.customerId) },
                                    trailing = if (tx.type == TransactionType.CREDIT) {
                                        { DueDateLabel(tx.dueDate) }
                                    } else null
                                )
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            })
        }
    }
}
