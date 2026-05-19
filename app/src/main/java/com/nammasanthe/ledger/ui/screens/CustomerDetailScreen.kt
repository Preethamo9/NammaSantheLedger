package com.nammasanthe.ledger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nammasanthe.ledger.R
import com.nammasanthe.ledger.data.db.TransactionType
import com.nammasanthe.ledger.ui.components.DueDateLabel
import com.nammasanthe.ledger.ui.theme.PaidGreen
import com.nammasanthe.ledger.ui.theme.PendingRed
import com.nammasanthe.ledger.util.WhatsAppHelper
import com.nammasanthe.ledger.ui.util.transactionTypeLabel
import com.nammasanthe.ledger.viewmodel.CustomerDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: Long,
    viewModel: CustomerDetailViewModel,
    onBack: () -> Unit,
    onAddPayment: () -> Unit,
    onAddCredit: () -> Unit
) {
    LaunchedEffect(customerId) { viewModel.load(customerId) }
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val showDelete = remember { mutableStateOf(false) }
    val editingPhone = remember { mutableStateOf(false) }
    val phoneInput = remember { mutableStateOf("") }
    val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.customer?.name ?: "") },
                navigationIcon = {
                    IconButton(onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(12.dp)) {
            Text(
                stringResource(R.string.balance_label, state.balance),
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                color = if (state.balance > 0) PendingRed else PaidGreen
            )
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(state.customer?.phoneNumber ?: stringResource(R.string.no_phone))
                TextButton(onClick = {
                    phoneInput.value = state.customer?.phoneNumber.orEmpty()
                    editingPhone.value = true
                }) { Text(stringResource(R.string.edit_phone)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAddCredit, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.add_credit))
                }
                OutlinedButton(onClick = onAddPayment, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.record_payment))
                }
            }
            val phone = state.customer?.phoneNumber
            if (!phone.isNullOrBlank() && state.balance > 0) {
                OutlinedButton(
                    onClick = {
                        WhatsAppHelper.openReminder(
                            context, phone, state.customer!!.name, state.balance,
                            state.vendorName, state.shopName
                        )
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) { Text(stringResource(R.string.whatsapp_reminder)) }
            }
            OutlinedButton(
                onClick = { showDelete.value = true },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.delete_customer)) }
            LazyColumn(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.transactions, key = { it.id }) { tx ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(transactionTypeLabel(tx.type))
                                Text(
                                    "₹${tx.amount}",
                                    color = if (tx.type == TransactionType.CREDIT) PendingRed else PaidGreen
                                )
                            }
                            Text(dateFmt.format(Date(tx.date)))
                            if (tx.type == TransactionType.CREDIT) DueDateLabel(tx.dueDate)
                            tx.note?.let { Text(it) }
                        }
                    }
                }
            }
        }
    }

    if (editingPhone.value) {
        AlertDialog(
            onDismissRequest = { editingPhone.value = false },
            title = { Text(stringResource(R.string.edit_phone)) },
            text = {
                OutlinedTextField(phoneInput.value, { phoneInput.value = it }, label = { Text(stringResource(R.string.phone)) })
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updatePhone(phoneInput.value)
                    editingPhone.value = false
                }) { Text(stringResource(R.string.save)) }
            }
        )
    }

    if (showDelete.value) {
        AlertDialog(
            onDismissRequest = { showDelete.value = false },
            title = { Text(stringResource(R.string.delete_customer)) },
            text = { Text(stringResource(R.string.delete_customer_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCustomer(onBack)
                    showDelete.value = false
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDelete.value = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}
