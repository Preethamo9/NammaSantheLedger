package com.nammasanthe.ledger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nammasanthe.ledger.R
import com.nammasanthe.ledger.ui.components.ResponsiveTwoPane
import com.nammasanthe.ledger.ui.components.fintech.CustomerListCard
import com.nammasanthe.ledger.ui.components.fintech.FintechScreenBackground
import com.nammasanthe.ledger.ui.components.fintech.SectionTitle
import com.nammasanthe.ledger.ui.components.fintech.SettingsRow
import com.nammasanthe.ledger.ui.components.fintech.StatMiniCard
import com.nammasanthe.ledger.ui.theme.CreditPinkSoft
import com.nammasanthe.ledger.ui.theme.Dimens
import com.nammasanthe.ledger.ui.theme.PaidGreen
import com.nammasanthe.ledger.ui.theme.PaymentGreenSoft
import com.nammasanthe.ledger.ui.theme.PendingRed
import com.nammasanthe.ledger.viewmodel.DailySummaryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailySummaryScreen(viewModel: DailySummaryViewModel, widthClass: WindowWidthSizeClass) {
    val state by viewModel.uiState.collectAsState()
    var showPicker by remember { mutableStateOf(false) }
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent) { padding ->
        FintechScreenBackground {
            ResponsiveTwoPane(widthClass, main = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(Dimens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimens.itemSpacing)
                ) {
                    item { SectionTitle(stringResource(R.string.daily_summary)) }
                    item {
                        SettingsRow(
                            title = stringResource(R.string.date),
                            subtitle = dateFmt.format(Date(state.dayMillis)),
                            icon = androidx.compose.material.icons.Icons.Default.CalendarMonth,
                            onClick = { showPicker = true }
                        )
                    }
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatMiniCard(
                                title = stringResource(R.string.todays_sales),
                                amount = "₹${state.sales}",
                                icon = androidx.compose.material.icons.Icons.Default.ArrowUpward,
                                containerColor = CreditPinkSoft,
                                iconTint = PendingRed,
                                modifier = Modifier.weight(1f)
                            )
                            StatMiniCard(
                                title = stringResource(R.string.todays_payments),
                                amount = "₹${state.payments}",
                                icon = androidx.compose.material.icons.Icons.Default.ArrowDownward,
                                containerColor = PaymentGreenSoft,
                                iconTint = PaidGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        SectionTitle(stringResource(R.string.customer_balances))
                    }
                    items(state.customers, key = { it.customerId }) { row ->
                        CustomerListCard(
                            name = row.customerName,
                            balance = row.balance,
                            hasPhone = false,
                            onClick = { }
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            })
        }
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.dayMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { viewModel.setDay(it) }
                    showPicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) { DatePicker(pickerState) }
    }
}
