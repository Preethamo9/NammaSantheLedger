package com.nammasanthe.ledger.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nammasanthe.ledger.R
import com.nammasanthe.ledger.data.db.model.CustomerWithBalance
import com.nammasanthe.ledger.ui.theme.PaidGreen
import com.nammasanthe.ledger.ui.theme.PendingRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerPickerSheet(
    sheetState: SheetState,
    customers: List<CustomerWithBalance>,
    allCustomersEmpty: Boolean,
    searchQuery: String,
    selectedCustomerId: Long,
    onSearchChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onCustomerSelected: (Long) -> Unit,
    onAddCustomer: suspend (name: String, phone: String?) -> Result<Long>
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showNewCustomer by remember { mutableStateOf(false) }
    val newName = remember { mutableStateOf("") }
    val newPhone = remember { mutableStateOf("") }
    var addError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                stringResource(R.string.select_customer),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                label = { Text(stringResource(R.string.customer_picker_search)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedButton(
                onClick = { showNewCustomer = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(
                    stringResource(R.string.new_customer),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            if (allCustomersEmpty) {
                Text(
                    stringResource(R.string.no_customers_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Button(
                    onClick = { showNewCustomer = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.add_customer)) }
            } else if (customers.isEmpty()) {
                Text(
                    stringResource(R.string.no_customer_matches),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(customers, key = { it.customer.id }) { item ->
                        val selected = item.customer.id == selectedCustomerId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCustomerSelected(item.customer.id)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (selected) 4.dp else 1.dp
                            )
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        item.customer.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (!item.customer.phoneNumber.isNullOrBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Phone,
                                                contentDescription = null,
                                                modifier = Modifier.padding(end = 4.dp),
                                                tint = PaidGreen
                                            )
                                            Text(
                                                item.customer.phoneNumber,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                                Text(
                                    stringResource(R.string.customer_balance_short, item.balance),
                                    color = if (item.balance > 0) PendingRed else PaidGreen,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewCustomer) {
        AlertDialog(
            onDismissRequest = {
                showNewCustomer = false
                addError = null
            },
            title = { Text(stringResource(R.string.new_customer)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName.value,
                        onValueChange = { newName.value = it },
                        label = { Text(stringResource(R.string.customer_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPhone.value,
                        onValueChange = { newPhone.value = it },
                        label = { Text(stringResource(R.string.phone_optional)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        singleLine = true
                    )
                    addError?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        onAddCustomer(newName.value, newPhone.value.ifBlank { null })
                            .onSuccess { id ->
                                onCustomerSelected(id)
                                newName.value = ""
                                newPhone.value = ""
                                showNewCustomer = false
                                addError = null
                            }
                            .onFailure { e ->
                                addError = e.message
                                    ?: context.getString(R.string.could_not_add_customer)
                            }
                    }
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNewCustomer = false
                    addError = null
                }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}
