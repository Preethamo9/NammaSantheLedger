package com.nammasanthe.ledger.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.nammasanthe.ledger.security.SecurePrefsManager

class LocaleManager(private val prefs: SecurePrefsManager) {

    fun applyStoredLocale() {
        if (!prefs.isLanguageSelected()) return
        applyLocale(prefs.getAppLanguageTag())
    }

    fun setLanguage(language: AppLanguage, restoreRoute: String? = null) {
        prefs.setAppLanguageTag(language.tag)
        prefs.setLanguageSelected(true)
        restoreRoute?.let { prefs.setRestoreRoute(it) }
        applyLocale(language.tag)
    }

    fun getCurrentLanguage(): AppLanguage = AppLanguage.fromTag(prefs.getAppLanguageTag())

    private fun applyLocale(tag: String) {
        try {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        } catch (_: Exception) {
            // Ignore on devices where app locale API is unavailable
        }
    }
}
