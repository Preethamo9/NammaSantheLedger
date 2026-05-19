package com.nammasanthe.ledger.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val LANGUAGE_SELECT = "language_select"
    const val SETUP_PROFILE = "setup_profile"
    const val PIN_ENTRY = "pin_entry"
    const val HOME = "home"
    const val CUSTOMERS = "customers"
    const val CUSTOMER_DETAIL = "customer_detail/{customerId}"
    const val ADD_TRANSACTION = "add_transaction?customerId={customerId}&type={type}"
    const val LEDGER = "ledger"
    const val DAILY_SUMMARY = "daily_summary"
    const val SETTINGS = "settings"

    fun customerDetail(id: Long) = "customer_detail/$id"
    fun addTransaction(customerId: Long? = null, type: String = "CREDIT") =
        "add_transaction?customerId=${customerId ?: -1}&type=$type"
}
