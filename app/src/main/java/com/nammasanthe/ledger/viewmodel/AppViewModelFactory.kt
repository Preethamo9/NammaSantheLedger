package com.nammasanthe.ledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nammasanthe.ledger.NammaSantheApplication
import com.nammasanthe.ledger.data.repository.LedgerRepository
import com.nammasanthe.ledger.locale.LocaleManager
import com.nammasanthe.ledger.security.SecurePrefsManager

class AppViewModelFactory(
    private val repository: LedgerRepository,
    private val securePrefs: SecurePrefsManager,
    private val localeManager: LocaleManager
) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return when {
      modelClass.isAssignableFrom(AuthViewModel::class.java) ->
        AuthViewModel(securePrefs) as T
      modelClass.isAssignableFrom(HomeViewModel::class.java) ->
        HomeViewModel(repository) as T
      modelClass.isAssignableFrom(CustomersViewModel::class.java) ->
        CustomersViewModel(repository) as T
      modelClass.isAssignableFrom(CustomerDetailViewModel::class.java) ->
        CustomerDetailViewModel(repository, securePrefs) as T
      modelClass.isAssignableFrom(AddTransactionViewModel::class.java) ->
        AddTransactionViewModel(repository) as T
      modelClass.isAssignableFrom(LedgerViewModel::class.java) ->
        LedgerViewModel(repository, securePrefs) as T
      modelClass.isAssignableFrom(DailySummaryViewModel::class.java) ->
        DailySummaryViewModel(repository) as T
      modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
        SettingsViewModel(securePrefs) as T
      modelClass.isAssignableFrom(LocaleViewModel::class.java) ->
        LocaleViewModel(localeManager) as T
      else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
  }

  companion object {
    fun from(app: NammaSantheApplication) =
      AppViewModelFactory(app.repository, app.securePrefs, app.localeManager)
  }
}
