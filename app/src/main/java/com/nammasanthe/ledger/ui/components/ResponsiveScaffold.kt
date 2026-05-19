package com.nammasanthe.ledger.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ResponsiveTwoPane(
    widthClass: WindowWidthSizeClass,
    main: @Composable () -> Unit,
    side: (@Composable () -> Unit)? = null
) {
    if (widthClass >= WindowWidthSizeClass.Medium && side != null) {
        Row(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).padding(8.dp)) { main() }
            Box(Modifier.weight(1f).padding(8.dp)) { side() }
        }
    } else {
        Box(
            Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .padding(8.dp)
        ) { main() }
    }
}
