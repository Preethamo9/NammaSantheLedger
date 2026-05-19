package com.nammasanthe.ledger.viewmodel

import androidx.lifecycle.ViewModel
import com.nammasanthe.ledger.security.SecurePrefsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsUiState(
    val vendorName: String = "",
    val shopName: String = ""
)

class SettingsViewModel(private val prefs: SecurePrefsManager) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsUiState(prefs.getVendorName(), prefs.getShopName())
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun refresh() {
        _state.value = SettingsUiState(prefs.getVendorName(), prefs.getShopName())
    }

    fun saveProfile(vendorName: String, shopName: String?) {
        prefs.updateProfile(vendorName, shopName)
        refresh()
    }

    fun changePin(current: String, newPin: String, confirm: String): PinChangeResult {
        if (newPin.length != 4 || !newPin.all { it.isDigit() }) return PinChangeResult.InvalidPin
        if (newPin != confirm) return PinChangeResult.Mismatch
        return if (prefs.changePin(current, newPin)) PinChangeResult.Success
        else PinChangeResult.WrongCurrent
    }

    fun logout() = prefs.logout()

    enum class PinChangeResult { Success, WrongCurrent, InvalidPin, Mismatch }
}
