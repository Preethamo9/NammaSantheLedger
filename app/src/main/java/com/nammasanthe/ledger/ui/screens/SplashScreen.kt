package com.nammasanthe.ledger.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.nammasanthe.ledger.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigate: (String) -> Unit, resolveStart: () -> String) {
    LaunchedEffect(Unit) {
        delay(600)
        onNavigate(resolveStart())
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        CircularProgressIndicator(Modifier.align(Alignment.BottomCenter))
    }
}
