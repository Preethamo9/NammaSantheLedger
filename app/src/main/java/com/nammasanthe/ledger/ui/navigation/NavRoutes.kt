package com.nammasanthe.ledger.ui.navigation

import com.nammasanthe.ledger.NammaSantheApplication

object NavRoutes {

    private val topLevelRoutes = setOf(
        Routes.LANGUAGE_SELECT,
        Routes.SETUP_PROFILE,
        Routes.PIN_ENTRY,
        Routes.HOME,
        Routes.CUSTOMERS,
        Routes.LEDGER,
        Routes.DAILY_SUMMARY,
        Routes.SETTINGS
    )

    data class Startup(
        val startRoute: String,
        val pendingCustomerId: Long? = null
    )

    fun computeStartup(app: NammaSantheApplication, startCustomerId: Long?): Startup {
        val restore = app.securePrefs.peekRestoreRoute()
        if (restore != null && restore in topLevelRoutes) {
            app.securePrefs.consumeRestoreRoute()
            return Startup(startRoute = restore)
        }
        if (restore != null) {
            app.securePrefs.clearRestoreRoute()
        }

        val pendingId = startCustomerId?.takeIf { it > 0 }
        val startRoute = when {
            !app.securePrefs.isLanguageSelected() -> Routes.LANGUAGE_SELECT
            !app.securePrefs.isProfileSetup() -> Routes.SETUP_PROFILE
            !app.securePrefs.isSessionUnlocked() -> Routes.PIN_ENTRY
            else -> Routes.HOME
        }
        return Startup(
            startRoute = startRoute,
            pendingCustomerId = if (startRoute == Routes.HOME) pendingId else null
        )
    }

    fun nextRouteAfterLanguage(app: NammaSantheApplication): String = when {
        !app.securePrefs.isProfileSetup() -> Routes.SETUP_PROFILE
        !app.securePrefs.isSessionUnlocked() -> Routes.PIN_ENTRY
        else -> Routes.HOME
    }

    @Deprecated("Use computeStartup", ReplaceWith("computeStartup(app, startCustomerId).startRoute"))
    fun computeStartRoute(app: NammaSantheApplication, startCustomerId: Long?): String =
        computeStartup(app, startCustomerId).startRoute
}
