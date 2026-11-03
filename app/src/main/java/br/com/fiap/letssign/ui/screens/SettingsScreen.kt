package br.com.fiap.letssign.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.letssign.R
import br.com.fiap.letssign.components.LanguageDialog
import br.com.fiap.letssign.components.LetsSignTopBar
import br.com.fiap.letssign.components.SectionTitle
import br.com.fiap.letssign.components.ThemeDialog
import br.com.fiap.letssign.components.displayName
import br.com.fiap.letssign.repository.AppLanguage
import br.com.fiap.letssign.ui.theme.AppShapes
import br.com.fiap.letssign.ui.theme.Dimens
import br.com.fiap.letssign.ui.theme.Spacing
import br.com.fiap.letssign.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onLanguageApplied: (AppLanguage) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.pendingLanguage) {
        state.pendingLanguage?.let { language ->
            viewModel.consumePendingLanguage()
            onLanguageApplied(language)
        }
    }

    Scaffold(
        topBar = { LetsSignTopBar(title = stringResource(R.string.settings_title), onNavigate = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenPadding, vertical = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            SectionTitle(text = stringResource(R.string.settings_section_preferences))
            SettingRow(
                icon = Icons.Default.Language,
                title = stringResource(R.string.settings_language),
                subtitle = state.language.displayName(),
                onClick = viewModel::openLanguageDialog,
            )
            SettingRow(
                icon = Icons.Default.DarkMode,
                title = stringResource(R.string.settings_theme),
                subtitle = state.theme.displayName(),
                onClick = viewModel::openThemeDialog,
            )
        }
    }

    if (state.isLanguageDialogVisible) {
        LanguageDialog(
            selected = state.language,
            onSelect = viewModel::selectLanguage,
            onDismiss = viewModel::dismissLanguageDialog,
        )
    }

    if (state.isThemeDialogVisible) {
        ThemeDialog(
            selected = state.theme,
            onSelect = viewModel::selectTheme,
            onDismiss = viewModel::dismissThemeDialog,
        )
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Row(Modifier.padding(Dimens.cardPadding), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(Dimens.iconContainer).clip(AppShapes.iconBox).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(Dimens.icon))
            }
            Column(Modifier.weight(1f).padding(horizontal = Spacing.md + Spacing.xxs)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
            )
        }
    }
}
