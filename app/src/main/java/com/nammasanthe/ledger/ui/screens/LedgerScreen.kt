package com.nammasanthe.ledger.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nammasanthe.ledger.R
import com.nammasanthe.ledger.data.db.TransactionType
import com.nammasanthe.ledger.ui.components.DueDateLabel
import com.nammasanthe.ledger.ui.components.ResponsiveTwoPane
import com.nammasanthe.ledger.ui.components.fintech.EmptyStateMessage
import com.nammasanthe.ledger.ui.components.fintech.FintechScreenBackground
import com.nammasanthe.ledger.ui.components.fintech.FintechSearchField
import com.nammasanthe.ledger.ui.components.fintech.FintechTransactionRow
import com.nammasanthe.ledger.ui.components.fintech.SectionTitle
import com.nammasanthe.ledger.ui.theme.Dimens
import com.nammasanthe.ledger.util.PdfReportGenerator
import com.nammasanthe.ledger.viewmodel.LedgerViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(viewModel: LedgerViewModel, widthClass: WindowWidthSizeClass) {
    val txs by viewModel.transactions.collectAsState()
    val search by viewModel.search.collectAsState()
    val isGenerating by viewModel.isGeneratingPdf.collectAsState()
    val pdfFile by viewModel.pdfFile.collectAsState()
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showExportSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    
    var exportStartDate by remember { mutableLongStateOf(0L) }
    var exportEndDate by remember { mutableLongStateOf(0L) }
    var pickingStartDate by remember { mutableStateOf(false) }
    var pickingEndDate by remember { mutableStateOf(false) }
    
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    LaunchedEffect(exportStartDate, exportEndDate) {
        viewModel.setPdfFile(null)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ledger), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showExportSheet = true }) {
                        Icon(Icons.Default.PictureAsPdf, stringResource(R.string.export_pdf))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
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
                    item { SectionTitle(stringResource(R.string.ledger)) }
                    item {
                        FintechSearchField(
                            value = search,
                            onValueChange = viewModel::setSearch,
                            placeholder = stringResource(R.string.search_ledger)
                        )
                    }
                    if (txs.isEmpty()) {
                        item { EmptyStateMessage(stringResource(R.string.no_customer_matches)) }
                    } else {
                        items(txs, key = { it.id }) { tx ->
                            FintechTransactionRow(
                                customerName = tx.customerName,
                                dateLabel = dateFmt.format(Date(tx.date)),
                                amount = tx.amount,
                                type = tx.type,
                                onClick = { },
                                trailing = if (tx.type == TransactionType.CREDIT) {
                                    { DueDateLabel(tx.dueDate) }
                                } else null
                            )
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            })
        }
    }

    if (showExportSheet) {
        ModalBottomSheet(
            onDismissRequest = { showExportSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.export_options),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.download_report),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))

                // Date Range Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.start_date), style = MaterialTheme.typography.labelSmall)
                        OutlinedButton(
                            onClick = { pickingStartDate = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(if (exportStartDate == 0L) "--/--/--" else dateFmt.format(Date(exportStartDate)))
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.end_date), style = MaterialTheme.typography.labelSmall)
                        OutlinedButton(
                            onClick = { pickingEndDate = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(if (exportEndDate == 0L) "--/--/--" else dateFmt.format(Date(exportEndDate)))
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.generating_pdf))
                } else if (pdfFile != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                pdfFile?.let { file ->
                                    val uri = PdfReportGenerator.getFileUri(context, file)
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "application/pdf")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.open_report)))
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.open_report))
                        }
                        Button(
                            onClick = {
                                pdfFile?.let { file ->
                                    val uri = PdfReportGenerator.getFileUri(context, file)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_report)))
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.share_report))
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.setPdfFile(null) // Clear old file immediately on click
                                viewModel.setGeneratingPdf(true)
                                val data = viewModel.getExportData(
                                    startDate = if (exportStartDate == 0L) null else exportStartDate,
                                    endDate = if (exportEndDate == 0L) null else exportEndDate
                                )
                                
                                if (data.transactions.isEmpty()) {
                                    viewModel.setGeneratingPdf(false)
                                    Toast.makeText(context, "No transactions found to export", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }

                                PdfReportGenerator.generateReport(
                                    context = context,
                                    vendorName = data.vendorName,
                                    shopName = data.shopName,
                                    transactions = data.transactions,
                                    totalCredit = data.totalCredit,
                                    totalPayment = data.totalPayment,
                                    onComplete = { file ->
                                        scope.launch {
                                            viewModel.setGeneratingPdf(false)
                                            if (file != null) {
                                                viewModel.setPdfFile(file)
                                                Toast.makeText(context, context.getString(R.string.pdf_generated_success), Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, context.getString(R.string.pdf_generation_failed), Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.generate_pdf), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (pickingStartDate) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = if (exportStartDate == 0L) System.currentTimeMillis() else exportStartDate)
        DatePickerDialog(
            onDismissRequest = { pickingStartDate = false },
            confirmButton = {
                TextButton(onClick = {
                    exportStartDate = datePickerState.selectedDateMillis ?: 0L
                    pickingStartDate = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { pickingStartDate = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (pickingEndDate) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = if (exportEndDate == 0L) System.currentTimeMillis() else exportEndDate)
        DatePickerDialog(
            onDismissRequest = { pickingEndDate = false },
            confirmButton = {
                TextButton(onClick = {
                    exportEndDate = datePickerState.selectedDateMillis ?: 0L
                    pickingEndDate = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { pickingEndDate = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
