package br.com.fiap.letssign.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import br.com.fiap.letssign.R
import br.com.fiap.letssign.repository.AppLanguage
import br.com.fiap.letssign.repository.AppTheme
import br.com.fiap.letssign.ui.theme.Spacing

@Composable
fun LanguageDialog(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    ChoiceDialog(
        title = stringResource(R.string.language_title),
        options = AppLanguage.entries,
        selected = selected,
        label = { it.displayName() },
        onSelect = onSelect,
        onDismiss = onDismiss,
    )
}

@Composable
fun ThemeDialog(
    selected: AppTheme,
    onSelect: (AppTheme) -> Unit,
    onDismiss: () -> Unit,
) {
    ChoiceDialog(
        title = stringResource(R.string.theme_title),
        options = AppTheme.entries,
        selected = selected,
        label = { it.displayName() },
        onSelect = onSelect,
        onDismiss = onDismiss,
    )
}

/** Dialog with one radio button per option. */
@Composable
private fun <T> ChoiceDialog(
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = selected == option, onClick = { onSelect(option) })
                        Text(text = label(option), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
        },
    )
}

@Composable
fun AppLanguage.displayName(): String = when (this) {
    AppLanguage.SYSTEM -> stringResource(R.string.language_system)
    AppLanguage.ENGLISH -> stringResource(R.string.language_english)
    AppLanguage.PORTUGUESE_BR -> stringResource(R.string.language_portuguese)
}

@Composable
fun AppTheme.displayName(): String = when (this) {
    AppTheme.SYSTEM -> stringResource(R.string.theme_system)
    AppTheme.LIGHT -> stringResource(R.string.theme_light)
    AppTheme.DARK -> stringResource(R.string.theme_dark)
}
