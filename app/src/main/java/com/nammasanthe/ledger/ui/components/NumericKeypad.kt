package com.nammasanthe.ledger.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nammasanthe.ledger.R
import com.nammasanthe.ledger.ui.theme.Dimens
import com.nammasanthe.ledger.ui.theme.PrimaryOrange
import com.nammasanthe.ledger.ui.theme.SurfaceMuted
import com.nammasanthe.ledger.ui.theme.SurfaceWhite
import com.nammasanthe.ledger.ui.theme.TextPrimary

@Composable
fun NumericKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("0", "⌫", "CLR")
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.cardRadius),
        color = SurfaceWhite,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { key ->
                        val label = when (key) {
                            "⌫" -> stringResource(R.string.keypad_backspace)
                            "CLR" -> stringResource(R.string.keypad_clear)
                            else -> key
                        }
                        val onClick = when (key) {
                            "⌫" -> onBackspace
                            "CLR" -> onClear
                            else -> ({ onDigit(key) })
                        }
                        val isAction = key == "⌫" || key == "CLR"
                        if (isAction) {
                            OutlinedButton(
                                onClick = onClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) { Text(label) }
                        } else {
                            Button(
                                onClick = onClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SurfaceMuted,
                                    contentColor = TextPrimary
                                )
                            ) {
                                Text(label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AmountDisplay(amount: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.cardRadiusLarge),
        color = SurfaceWhite,
        shadowElevation = 6.dp
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (amount.isEmpty()) "₹0" else "₹$amount",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryOrange
            )
        }
    }
}
