package com.nammasanthe.ledger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nammasanthe.ledger.R
import com.nammasanthe.ledger.ui.components.ResponsiveTwoPane
import com.nammasanthe.ledger.ui.components.fintech.CustomerListCard
import com.nammasanthe.ledger.ui.components.fintech.EmptyStateMessage
import com.nammasanthe.ledger.ui.components.fintech.FintechFab
import com.nammasanthe.ledger.ui.components.fintech.FintechFilterChips
import com.nammasanthe.ledger.ui.components.fintech.FintechScreenBackground
import com.nammasanthe.ledger.ui.components.fintech.FintechSearchField
import com.nammasanthe.ledger.ui.components.fintech.SectionTitle
import com.nammasanthe.ledger.ui.theme.Dimens
import com.nammasanthe.ledger.viewmodel.CustomerFilter
import com.nammasanthe.ledger.viewmodel.CustomersViewModel
import kotlinx.coroutines.launch

@Composable
fun CustomersScreen(
    viewModel: CustomersViewModel,
    widthClass: WindowWidthSizeClass,
    onCustomerClick: (Long) -> Unit
) {
    val customers by viewModel.displayCustomers.collectAsState()
    val search by viewModel.search.collectAsState()
    val filter by viewModel.currentFilter.collectAsState()
    val showAdd = remember { mutableStateOf(false) }
    val newName = remember { mutableStateOf("") }
    val newPhone = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val filterIndex = when (filter) {
        CustomerFilter.ALL -> 0
        CustomerFilter.PENDING -> 1
        CustomerFilter.PAID -> 2
    }
    val filterLabels = listOf(
        stringResource(R.string.filter_all),
        stringResource(R.string.filter_pending),
        stringResource(R.string.filter_paid)
    )

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        floatingActionButton = { FintechFab(onClick = { showAdd.value = true }) }
    ) { padding ->
        FintechScreenBackground {
            ResponsiveTwoPane(widthClass, main = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(Dimens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimens.itemSpacing)
                ) {
                    item {
                        SectionTitle(stringResource(R.string.customers))
                    }
                    item {
                        FintechSearchField(
                            value = search,
                            onValueChange = viewModel::setSearch,
                            placeholder = stringResource(R.string.search_customers)
                        )
                    }
                    item {
                        FintechFilterChips(
                            labels = filterLabels,
                            selectedIndex = filterIndex,
                            onSelected = { index ->
                                viewModel.setFilter(
                                    when (index) {
                                        1 -> CustomerFilter.PENDING
                                        2 -> CustomerFilter.PAID
                                        else -> CustomerFilter.ALL
                                    }
                                )
                            }
                        )
                    }
                    if (customers.isEmpty()) {
                        item {
                            EmptyStateMessage(stringResource(R.string.no_customers_yet))
                        }
                    } else {
                        items(customers, key = { it.customer.id }) { item ->
                            CustomerListCard(
                                name = item.customer.name,
                                balance = item.balance,
                                hasPhone = !item.customer.phoneNumber.isNullOrBlank(),
                                onClick = { onCustomerClick(item.customer.id) }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(88.dp)) }
                }
            })
        }
    }

    if (showAdd.value) {
        AlertDialog(
            onDismissRequest = { showAdd.value = false },
            title = { Text(stringResource(R.string.add_customer)) },
            text = {
                Column {
                    OutlinedTextField(
                        newName.value,
                        { newName.value = it },
                        label = { Text(stringResource(R.string.customer_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        newPhone.value,
                        { newPhone.value = it },
                        label = { Text(stringResource(R.string.phone_optional)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        viewModel.addCustomer(newName.value, newPhone.value.ifBlank { null })
                        showAdd.value = false
                        newName.value = ""
                        newPhone.value = ""
                    }
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showAdd.value = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}
