package com.nammasanthe.ledger

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.nammasanthe.ledger.ui.navigation.MainNavHost
import com.nammasanthe.ledger.ui.navigation.NavRoutes
import com.nammasanthe.ledger.ui.theme.NammaSantheTheme
import com.nammasanthe.ledger.worker.ReminderScheduler

class MainActivity : AppCompatActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as NammaSantheApplication
        val startup = NavRoutes.computeStartup(
            app,
            intent.getLongExtra("navigate_customer_id", -1L).takeIf { it > 0 }
        )
        if (app.securePrefs.isProfileSetup()) {
            ReminderScheduler.scheduleDaily(this)
        }
        setContent {
            val widthClass = calculateWindowSizeClass(this).widthSizeClass
            NammaSantheTheme {
                MainNavHost(
                    app = app,
                    widthClass = widthClass,
                    startRoute = startup.startRoute,
                    pendingCustomerId = startup.pendingCustomerId,
                    onRecreate = { recreate() }
                )
            }
        }
    }
}
