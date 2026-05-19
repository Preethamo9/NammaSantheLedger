package com.nammasanthe.ledger.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.nammasanthe.ledger.R
import com.nammasanthe.ledger.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupProfileScreen(
    viewModel: AuthViewModel,
    onComplete: () -> Unit
) {
    val vendor = remember { mutableStateOf("") }
    val shop = remember { mutableStateOf("") }
    val pin = remember { mutableStateOf("") }
    val confirm = remember { mutableStateOf("") }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.setup_profile)) }) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = vendor.value,
                onValueChange = { vendor.value = it },
                label = { Text(stringResource(R.string.vendor_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = shop.value,
                onValueChange = { shop.value = it },
                label = { Text(stringResource(R.string.shop_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = pin.value,
                onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pin.value = it },
                label = { Text(stringResource(R.string.set_pin)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = confirm.value,
                onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirm.value = it },
                label = { Text(stringResource(R.string.confirm_pin)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    when (viewModel.saveProfile(vendor.value, shop.value, pin.value, confirm.value)) {
                        AuthViewModel.ProfileResult.Success -> onComplete()
                        AuthViewModel.ProfileResult.VendorRequired ->
                            scope.launch { snackbar.showSnackbar(context.getString(R.string.vendor_name_required)) }
                        AuthViewModel.ProfileResult.InvalidPin ->
                            scope.launch { snackbar.showSnackbar(context.getString(R.string.enter_4_digit_pin)) }
                        AuthViewModel.ProfileResult.PinMismatch ->
                            scope.launch { snackbar.showSnackbar(context.getString(R.string.pin_mismatch)) }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.save_and_continue)) }
        }
    }
}
