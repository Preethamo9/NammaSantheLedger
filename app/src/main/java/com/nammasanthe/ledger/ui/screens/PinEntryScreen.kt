package com.nammasanthe.ledger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nammasanthe.ledger.R
import com.nammasanthe.ledger.ui.components.NumericKeypad
import com.nammasanthe.ledger.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinEntryScreen(viewModel: AuthViewModel, onUnlocked: () -> Unit) {
    val pin = remember { mutableStateOf("") }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun tryUnlock() {
        if (pin.value.length == 4) {
            if (viewModel.verifyPin(pin.value)) onUnlocked()
            else {
                pin.value = ""
                scope.launch { snackbar.showSnackbar(context.getString(R.string.wrong_pin)) }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.enter_pin)) }) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.enter_pin), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(4) { i ->
                    Text(
                        if (i < pin.value.length) "●" else "○",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            NumericKeypad(
                onDigit = {
                    if (pin.value.length < 4) {
                        pin.value += it
                        tryUnlock()
                    }
                },
                onBackspace = { if (pin.value.isNotEmpty()) pin.value = pin.value.dropLast(1) },
                onClear = { pin.value = "" },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
