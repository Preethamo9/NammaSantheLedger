package com.nammasanthe.ledger.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.nammasanthe.ledger.util.PinHasher

class SecurePrefsManager(context: Context) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = createPrefs(appContext)

    private fun createPrefs(context: Context): SharedPreferences {
        return try {
            EncryptedSharedPreferences.create(
                context,
                PREFS_FILE,
                MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.w(TAG, "Encrypted prefs unavailable, using standard prefs", e)
            context.getSharedPreferences(PREFS_FILE_FALLBACK, Context.MODE_PRIVATE)
        }
    }

    fun isProfileSetup(): Boolean = prefs.getBoolean(KEY_PROFILE_SETUP, false)

    fun saveProfile(vendorName: String, shopName: String?, pin: String) {
        prefs.edit()
            .putBoolean(KEY_PROFILE_SETUP, true)
            .putString(KEY_VENDOR_NAME, vendorName.trim())
            .putString(KEY_SHOP_NAME, shopName?.trim().orEmpty())
            .putString(KEY_PIN_HASH, PinHasher.hash(pin))
            .apply()
    }

    fun updateProfile(vendorName: String, shopName: String?) {
        prefs.edit()
            .putString(KEY_VENDOR_NAME, vendorName.trim())
            .putString(KEY_SHOP_NAME, shopName?.trim().orEmpty())
            .apply()
    }

    fun verifyPin(pin: String): Boolean {
        val stored = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return stored == PinHasher.hash(pin)
    }

    fun changePin(currentPin: String, newPin: String): Boolean {
        if (!verifyPin(currentPin)) return false
        prefs.edit().putString(KEY_PIN_HASH, PinHasher.hash(newPin)).apply()
        return true
    }

    fun getVendorName(): String = prefs.getString(KEY_VENDOR_NAME, "").orEmpty()

    fun getShopName(): String = prefs.getString(KEY_SHOP_NAME, "").orEmpty()

    fun isSessionUnlocked(): Boolean = prefs.getBoolean(KEY_SESSION_UNLOCKED, false)

    fun setSessionUnlocked(unlocked: Boolean) {
        prefs.edit().putBoolean(KEY_SESSION_UNLOCKED, unlocked).apply()
    }

    fun logout() {
        setSessionUnlocked(false)
    }

    fun isLanguageSelected(): Boolean = prefs.getBoolean(KEY_LANGUAGE_SELECTED, false)

    fun setLanguageSelected(selected: Boolean) {
        prefs.edit().putBoolean(KEY_LANGUAGE_SELECTED, selected).apply()
    }

    fun getAppLanguageTag(): String =
        prefs.getString(KEY_APP_LANGUAGE, AppLanguageDefault) ?: AppLanguageDefault

    fun setAppLanguageTag(tag: String) {
        prefs.edit().putString(KEY_APP_LANGUAGE, tag).apply()
    }

    fun setRestoreRoute(route: String) {
        prefs.edit().putString(KEY_RESTORE_ROUTE, route).apply()
    }

    fun peekRestoreRoute(): String? = prefs.getString(KEY_RESTORE_ROUTE, null)

    fun consumeRestoreRoute(): String? {
        val route = prefs.getString(KEY_RESTORE_ROUTE, null) ?: return null
        prefs.edit().remove(KEY_RESTORE_ROUTE).apply()
        return route
    }

    fun clearRestoreRoute() {
        prefs.edit().remove(KEY_RESTORE_ROUTE).apply()
    }

    companion object {
        private const val TAG = "SecurePrefsManager"
        private const val PREFS_FILE = "namma_santhe_secure_prefs"
        private const val PREFS_FILE_FALLBACK = "namma_santhe_prefs"
        private const val KEY_PROFILE_SETUP = "profile_setup"
        private const val KEY_VENDOR_NAME = "vendor_name"
        private const val KEY_SHOP_NAME = "shop_name"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_SESSION_UNLOCKED = "session_unlocked"
        private const val KEY_LANGUAGE_SELECTED = "language_selected"
        private const val KEY_APP_LANGUAGE = "app_language"
        private const val KEY_RESTORE_ROUTE = "restore_route"
        private const val AppLanguageDefault = "en"
    }
}
