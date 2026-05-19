package com.nammasanthe.ledger.viewmodel

import androidx.lifecycle.ViewModel
import com.nammasanthe.ledger.security.SecurePrefsManager

class AuthViewModel(private val prefs: SecurePrefsManager) : ViewModel() {

    fun isProfileSetup(): Boolean = prefs.isProfileSetup()

    fun isSessionUnlocked(): Boolean = prefs.isSessionUnlocked()

    fun saveProfile(vendorName: String, shopName: String?, pin: String, confirmPin: String): ProfileResult {
        if (vendorName.isBlank()) return ProfileResult.VendorRequired
        if (pin.length != 4 || !pin.all { it.isDigit() }) return ProfileResult.InvalidPin
        if (pin != confirmPin) return ProfileResult.PinMismatch
        prefs.saveProfile(vendorName, shopName, pin)
        prefs.setSessionUnlocked(true)
        return ProfileResult.Success
    }

    fun verifyPin(pin: String): Boolean {
        val ok = prefs.verifyPin(pin)
        if (ok) prefs.setSessionUnlocked(true)
        return ok
    }

    fun logout() = prefs.logout()

    enum class ProfileResult { Success, VendorRequired, InvalidPin, PinMismatch }
}
