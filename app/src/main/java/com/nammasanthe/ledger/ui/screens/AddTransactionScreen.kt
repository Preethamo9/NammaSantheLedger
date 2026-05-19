package com.nammasanthe.ledger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nammasanthe.ledger.R
import com.nammasanthe.ledger.data.db.TransactionType
import com.nammasanthe.ledger.ui.components.AmountDisplay
import com.nammasanthe.ledger.ui.components.CustomerPickerSheet
import com.nammasanthe.ledger.ui.components.NumericKeypad
import com.nammasanthe.ledger.ui.components.fintech.FintechFilterChips
import com.nammasanthe.ledger.ui.components.fintech.FintechScreenBackground
import com.nammasanthe.ledger.ui.theme.Dimens
import com.nammasanthe.ledger.ui.theme.PrimaryOrange
import com.nammasanthe.ledger.ui.theme.PrimaryOrangeDark
import com.nammasanthe.ledger.viewmodel.AddTransactionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel,
    preselectedCustomerId: Long?,
    initialType: TransactionType,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val allCustomers by viewModel.customers.collectAsState()
    val pickerCustomers by viewModel.pickerCustomers.collectAsState()
    val pickerSearch by viewModel.pickerSearch.collectAsState()

    var amount by rememberSaveable { mutableStateOf("") }
    var typeName by rememberSaveable { mutableStateOf(initialType.name) }
    val type = TransactionType.valueOf(typeName)

    var selectedCustomerId by rememberSaveable {
        mutableLongStateOf(preselectedCustomerId?.takeIf { it > 0 } ?: -1L)
    }
    var selectedDateMillis by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var dueDays by rememberSaveable { mutableIntStateOf(0) }
    var note by rememberSaveable { mutableStateOf("") }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showCustomerPicker by rememberSaveable {
        mutableStateOf(preselectedCustomerId == null || preselectedCustomerId <= 0)
    }

    val scope = rememberCoroutineScope()
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val selectedCustomer = allCustomers.find { it.customer.id == selectedCustomerId }

    LaunchedEffect(preselectedCustomerId) {
        if (preselectedCustomerId != null && preselectedCustomerId > 0) {
            selectedCustomerId = preselectedCustomerId
        }
    }

    fun dismissPicker() {
        showCustomerPicker = false
        viewModel.clearPickerSearch()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_transaction), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(Dimens.screenPadding)
            ) {
                val isEnabled = amount.isNotEmpty() && selectedCustomerId > 0
                val gradient = Brush.horizontalGradient(
                    colors = if (isEnabled) {
                        listOf(PrimaryOrange, PrimaryOrangeDark)
                    } else {
                        listOf(Color.LightGray, Color.Gray)
                    }
                )

                Button(
                    onClick = {
                        val amt = amount.toIntOrNull() ?: 0
                        if (selectedCustomerId <= 0) return@Button
                        scope.launch {
                            viewModel.save(
                                selectedCustomerId,
                                amt,
                                type,
                                selectedDateMillis,
                                dueDays,
                                note
                            ).onSuccess { onSaved() }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(28.dp)),
                    enabled = isEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(28.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(gradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Text(
                                stringResource(R.string.save_transaction).uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        FintechScreenBackground {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = Dimens.screenPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(8.dp))
                Card(
                    onClick = { showCustomerPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimens.cardRadius),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.select_customer),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = selectedCustomer?.customer?.name
                                ?: stringResource(R.string.choose_customer),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        selectedCustomer?.customer?.phoneNumber?.takeIf { it.isNotBlank() }?.let { phone ->
                            Text(
                                phone,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
                val typeLabels = listOf(stringResource(R.string.credit), stringResource(R.string.payment))
                val typeIndex = if (type == TransactionType.CREDIT) 0 else 1
                FintechFilterChips(
                    labels = typeLabels,
                    selectedIndex = typeIndex,
                    onSelected = { index ->
                        typeName = if (index == 0) TransactionType.CREDIT.name else TransactionType.PAYMENT.name
                    },
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { showDatePicker = true },
                        contentPadding = PaddingValues(0.dp) // Remove default button padding
                    ) {
                        Text(
                            "${stringResource(R.string.date)}: ${dateFmt.format(Date(selectedDateMillis))}",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    if (type == TransactionType.CREDIT) {
                        OutlinedTextField(
                            value = if (dueDays == 0) "" else dueDays.toString(),
                            onValueChange = { v -> dueDays = v.filter { it.isDigit() }.toIntOrNull() ?: 0 },
                            label = { Text(stringResource(R.string.return_in_days)) },
                            placeholder = { Text(stringResource(R.string.days_placeholder)) },
                            modifier = Modifier.width(140.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.note_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )
                
                Spacer(Modifier.height(16.dp))
                AmountDisplay(amount, Modifier.fillMaxWidth())
                NumericKeypad(
                    onDigit = { if (amount.length < 8) amount += it },
                    onBackspace = { if (amount.isNotEmpty()) amount = amount.dropLast(1) },
                    onClear = { amount = "" },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) { DatePicker(state) }
    }

    if (showCustomerPicker) {
        CustomerPickerSheet(
            sheetState = sheetState,
            customers = pickerCustomers,
            allCustomersEmpty = allCustomers.isEmpty(),
            searchQuery = pickerSearch,
            selectedCustomerId = selectedCustomerId,
            onSearchChange = viewModel::setPickerSearch,
            onDismiss = { dismissPicker() },
            onCustomerSelected = { id ->
                selectedCustomerId = id
                dismissPicker()
            },
            onAddCustomer = viewModel::addCustomer
        )
    }
}
