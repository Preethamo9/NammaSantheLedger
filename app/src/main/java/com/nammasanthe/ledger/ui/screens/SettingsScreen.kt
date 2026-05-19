package com.nammasanthe.ledger.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.nammasanthe.ledger.R
import com.nammasanthe.ledger.locale.AppLanguage
import com.nammasanthe.ledger.ui.components.fintech.FintechScreenBackground
import com.nammasanthe.ledger.ui.components.fintech.SectionTitle
import com.nammasanthe.ledger.ui.components.fintech.SettingsCard
import com.nammasanthe.ledger.ui.components.fintech.SettingsRow
import com.nammasanthe.ledger.ui.theme.Dimens
import com.nammasanthe.ledger.ui.theme.PendingRed
import com.nammasanthe.ledger.ui.theme.PrimaryOrange
import com.nammasanthe.ledger.viewmodel.LocaleViewModel
import com.nammasanthe.ledger.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    localeViewModel: LocaleViewModel,
    onLogout: () -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val currentLanguage by localeViewModel.selectedLanguage.collectAsState()
    var vendor by remember(state.vendorName) { mutableStateOf(state.vendorName) }
    var shop by remember(state.shopName) { mutableStateOf(state.shopName) }
    var showPin by remember { mutableStateOf(false) }
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var showLanguageSheet by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val languageSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val smsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        scope.launch {
            snackbar.showSnackbar(
                context.getString(
                    if (granted) R.string.sms_permission_granted else R.string.sms_permission_denied
                )
            )
        }
    }
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        FintechScreenBackground {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(Dimens.screenPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                SectionTitle(stringResource(R.string.settings))
                SettingsCard {
                    SettingsRow(
                        title = stringResource(R.string.language),
                        subtitle = stringResource(
                            R.string.language_current,
                            stringResource(currentLanguage.nativeLabelRes)
                        ),
                        icon = Icons.Default.Language,
                        onClick = { showLanguageSheet = true }
                    )
                    SettingsRow(
                        title = stringResource(R.string.change_pin),
                        icon = Icons.Default.Lock,
                        onClick = { showPin = true }
                    )
                    SettingsRow(
                        title = stringResource(R.string.request_sms_permission),
                        icon = Icons.Default.Sms,
                        onClick = {
                            smsLauncher.launch(Manifest.permission.SEND_SMS)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    )
                }
                Spacer(Modifier.height(20.dp))
                SettingsCard {
                    OutlinedTextField(
                        value = vendor,
                        onValueChange = { vendor = it },
                        label = { Text(stringResource(R.string.vendor_name)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    )
                    OutlinedTextField(
                        value = shop,
                        onValueChange = { shop = it },
                        label = { Text(stringResource(R.string.shop_name)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    )
                    Button(
                        onClick = { viewModel.saveProfile(vendor, shop) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                    ) { Text(stringResource(R.string.save_profile)) }
                }
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PendingRed)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text(stringResource(R.string.logout))
                }
            }
        }
    }

    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            sheetState = languageSheetState
        ) {
            LanguageSelectionContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
                selected = currentLanguage,
                showContinue = false,
                onSelect = { language ->
                    showLanguageSheet = false
                    if (language != currentLanguage) {
                        onLanguageChanged(language)
                    }
                },
                onContinue = { }
            )
        }
    }

    if (showPin) {
        AlertDialog(
            onDismissRequest = { showPin = false },
            title = { Text(stringResource(R.string.change_pin)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = currentPin,
                        onValueChange = { if (it.length <= 4) currentPin = it },
                        label = { Text(stringResource(R.string.current_pin)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 4) newPin = it },
                        label = { Text(stringResource(R.string.new_pin)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { if (it.length <= 4) confirmPin = it },
                        label = { Text(stringResource(R.string.confirm_pin)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    when (viewModel.changePin(currentPin, newPin, confirmPin)) {
                        SettingsViewModel.PinChangeResult.Success -> {
                            showPin = false
                            scope.launch { snackbar.showSnackbar(context.getString(R.string.pin_changed)) }
                        }
                        SettingsViewModel.PinChangeResult.WrongCurrent ->
                            scope.launch { snackbar.showSnackbar(context.getString(R.string.wrong_pin)) }
                        SettingsViewModel.PinChangeResult.Mismatch ->
                            scope.launch { snackbar.showSnackbar(context.getString(R.string.pin_mismatch)) }
                        SettingsViewModel.PinChangeResult.InvalidPin ->
                            scope.launch { snackbar.showSnackbar(context.getString(R.string.invalid_pin)) }
                    }
                }) { Text(stringResource(R.string.save)) }
            }
        )
    }
}
