package com.nammasanthe.ledger.viewmodel

import androidx.lifecycle.ViewModel
import com.nammasanthe.ledger.locale.AppLanguage
import com.nammasanthe.ledger.locale.LocaleManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocaleViewModel(private val localeManager: LocaleManager) : ViewModel() {

    private val _selected = MutableStateFlow(localeManager.getCurrentLanguage())
    val selectedLanguage: StateFlow<AppLanguage> = _selected.asStateFlow()

    fun select(language: AppLanguage) {
        _selected.value = language
    }

    fun getCurrentLanguage(): AppLanguage = localeManager.getCurrentLanguage()

    fun applyLanguage(language: AppLanguage, restoreRoute: String? = null) {
        localeManager.setLanguage(language, restoreRoute)
        _selected.value = language
    }
}
