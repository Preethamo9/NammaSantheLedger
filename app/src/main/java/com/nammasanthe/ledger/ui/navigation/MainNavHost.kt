package com.nammasanthe.ledger.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nammasanthe.ledger.NammaSantheApplication
import com.nammasanthe.ledger.R
import com.nammasanthe.ledger.data.db.TransactionType
import com.nammasanthe.ledger.ui.components.fintech.FintechBottomBar
import com.nammasanthe.ledger.ui.screens.AddTransactionScreen
import com.nammasanthe.ledger.ui.screens.CustomerDetailScreen
import com.nammasanthe.ledger.ui.screens.CustomersScreen
import com.nammasanthe.ledger.ui.screens.DailySummaryScreen
import com.nammasanthe.ledger.ui.screens.HomeScreen
import com.nammasanthe.ledger.ui.screens.LanguageSelectionScreen
import com.nammasanthe.ledger.ui.screens.LedgerScreen
import com.nammasanthe.ledger.ui.screens.PinEntryScreen
import com.nammasanthe.ledger.ui.screens.SettingsScreen
import com.nammasanthe.ledger.ui.screens.SetupProfileScreen
import com.nammasanthe.ledger.viewmodel.AddTransactionViewModel
import com.nammasanthe.ledger.viewmodel.AppViewModelFactory
import com.nammasanthe.ledger.viewmodel.AuthViewModel
import com.nammasanthe.ledger.viewmodel.CustomerDetailViewModel
import com.nammasanthe.ledger.viewmodel.CustomersViewModel
import com.nammasanthe.ledger.viewmodel.DailySummaryViewModel
import com.nammasanthe.ledger.viewmodel.HomeViewModel
import com.nammasanthe.ledger.viewmodel.LedgerViewModel
import com.nammasanthe.ledger.viewmodel.LocaleViewModel
import com.nammasanthe.ledger.viewmodel.SettingsViewModel
import com.nammasanthe.ledger.worker.ReminderScheduler

private val bottomTabs = listOf(
    Routes.HOME to Icons.Default.Home,
    Routes.CUSTOMERS to Icons.Default.People,
    Routes.LEDGER to Icons.Default.AccountBalance,
    Routes.DAILY_SUMMARY to Icons.Default.Summarize,
    Routes.SETTINGS to Icons.Default.Settings
)

@Composable
fun MainNavHost(
    app: NammaSantheApplication,
    widthClass: WindowWidthSizeClass,
    startRoute: String,
    pendingCustomerId: Long? = null,
    onRecreate: () -> Unit
) {
    val factory = AppViewModelFactory.from(app)
    val navController = rememberNavController()
    val authVm: AuthViewModel = viewModel(factory = factory)
    val localeVm: LocaleViewModel = viewModel(factory = factory)

    LaunchedEffect(pendingCustomerId, startRoute) {
        val id = pendingCustomerId ?: return@LaunchedEffect
        if (startRoute == Routes.HOME && app.securePrefs.isSessionUnlocked()) {
            navController.navigate(Routes.customerDetail(id)) {
                launchSingleTop = true
            }
        }
    }

    val mainTabs = setOf(Routes.HOME, Routes.CUSTOMERS, Routes.LEDGER, Routes.DAILY_SUMMARY, Routes.SETTINGS)
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route?.substringBefore("?")
        ?: startRoute.substringBefore("?")
    val showBottomBar = currentRoute != null && mainTabs.any { currentRoute.startsWith(it) }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                FintechBottomBar(
                    tabs = bottomTabs,
                    selectedRoute = currentRoute,
                    onTabSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    tabLabel = { tabLabel(it) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.LANGUAGE_SELECT) {
                LanguageSelectionScreen(
                    viewModel = localeVm,
                    showContinue = true,
                    onContinue = { language ->
                        localeVm.applyLanguage(language)
                        val next = NavRoutes.nextRouteAfterLanguage(app)
                        navController.navigate(next) {
                            popUpTo(Routes.LANGUAGE_SELECT) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.SETUP_PROFILE) {
                val context = LocalContext.current
                SetupProfileScreen(authVm) {
                    ReminderScheduler.scheduleDaily(context)
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SETUP_PROFILE) { inclusive = true }
                    }
                }
            }
            composable(Routes.PIN_ENTRY) {
                PinEntryScreen(authVm) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.PIN_ENTRY) { inclusive = true }
                    }
                }
            }
            composable(Routes.HOME) {
                val vm: HomeViewModel = viewModel(factory = factory)
                HomeScreen(
                    viewModel = vm,
                    widthClass = widthClass,
                    vendorName = app.securePrefs.getVendorName(),
                    shopName = app.securePrefs.getShopName(),
                    onAddTransaction = { navController.navigate(Routes.addTransaction()) },
                    onOpenCustomer = { id -> navController.navigate(Routes.customerDetail(id)) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) }
                )
            }
            composable(Routes.CUSTOMERS) {
                val vm: CustomersViewModel = viewModel(factory = factory)
                CustomersScreen(vm, widthClass) { id ->
                    navController.navigate(Routes.customerDetail(id))
                }
            }
            composable(
                Routes.CUSTOMER_DETAIL,
                arguments = listOf(navArgument("customerId") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("customerId") ?: return@composable
                val vm: CustomerDetailViewModel = viewModel(factory = factory)
                CustomerDetailScreen(
                    customerId = id,
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onAddPayment = { navController.navigate(Routes.addTransaction(id, "PAYMENT")) },
                    onAddCredit = { navController.navigate(Routes.addTransaction(id, "CREDIT")) }
                )
            }
            composable(
                Routes.ADD_TRANSACTION,
                arguments = listOf(
                    navArgument("customerId") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("type") { type = NavType.StringType; defaultValue = "CREDIT" }
                )
            ) { entry ->
                val cid = entry.arguments?.getLong("customerId")?.takeIf { it > 0 }
                val typeStr = entry.arguments?.getString("type") ?: "CREDIT"
                val type = TransactionType.valueOf(typeStr)
                val vm: AddTransactionViewModel = viewModel(factory = factory)
                AddTransactionScreen(
                    vm,
                    cid,
                    type,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(Routes.LEDGER) {
                val vm: LedgerViewModel = viewModel(factory = factory)
                LedgerScreen(vm, widthClass)
            }
            composable(Routes.DAILY_SUMMARY) {
                val vm: DailySummaryViewModel = viewModel(factory = factory)
                DailySummaryScreen(vm, widthClass)
            }
            composable(Routes.SETTINGS) {
                val vm: SettingsViewModel = viewModel(factory = factory)
                SettingsScreen(
                    viewModel = vm,
                    localeViewModel = localeVm,
                    onLogout = {
                        navController.navigate(Routes.PIN_ENTRY) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    onLanguageChanged = { language ->
                        localeVm.applyLanguage(language, Routes.SETTINGS)
                        onRecreate()
                    }
                )
            }
        }
    }
}

@Composable
private fun tabLabel(route: String): String = when (route) {
    Routes.HOME -> stringResource(R.string.home)
    Routes.CUSTOMERS -> stringResource(R.string.customers)
    Routes.LEDGER -> stringResource(R.string.ledger)
    Routes.DAILY_SUMMARY -> stringResource(R.string.daily_summary)
    Routes.SETTINGS -> stringResource(R.string.settings)
    else -> route
}
