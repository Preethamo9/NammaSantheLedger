package com.nammasanthe.ledger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.nammasanthe.ledger.R
import com.nammasanthe.ledger.locale.AppLanguage
import com.nammasanthe.ledger.viewmodel.LocaleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionScreen(
    viewModel: LocaleViewModel,
    showContinue: Boolean,
    onContinue: (AppLanguage) -> Unit,
    onLanguageSelected: ((AppLanguage) -> Unit)? = null
) {
    val selected by viewModel.selectedLanguage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.choose_language)) })
        }
    ) { padding ->
        LanguageSelectionContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            selected = selected,
            showContinue = showContinue,
            onSelect = { language ->
                viewModel.select(language)
                onLanguageSelected?.invoke(language)
            },
            onContinue = { onContinue(selected) }
        )
    }
}

@Composable
fun LanguageSelectionContent(
    modifier: Modifier = Modifier,
    selected: AppLanguage,
    showContinue: Boolean,
    onSelect: (AppLanguage) -> Unit,
    onContinue: () -> Unit
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Text(
            stringResource(R.string.choose_language_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        AppLanguage.entries.forEach { language ->
            LanguageOptionCard(
                language = language,
                selected = selected == language,
                onSelect = { onSelect(language) }
            )
        }
        if (showContinue) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text(stringResource(R.string.continue_label))
            }
        }
    }
}

@Composable
fun LanguageOptionCard(
    language: AppLanguage,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 1.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selected, onClick = null)
                Text(
                    text = stringResource(language.nativeLabelRes),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
