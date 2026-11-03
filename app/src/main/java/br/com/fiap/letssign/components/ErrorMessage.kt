package br.com.fiap.letssign.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import br.com.fiap.letssign.R
import br.com.fiap.letssign.ui.theme.AppShapes
import br.com.fiap.letssign.ui.theme.Dimens
import br.com.fiap.letssign.ui.theme.Emerald
import br.com.fiap.letssign.ui.theme.Spacing

@Composable
fun InlineErrorMessage(message: String, modifier: Modifier = Modifier) {
    MessageBanner(
        message, Icons.Default.ErrorOutline,
        MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, modifier,
    )
}

@Composable
fun InlineSuccessMessage(message: String, modifier: Modifier = Modifier) {
    MessageBanner(message, Icons.Default.CheckCircle, Emerald.copy(alpha = 0.12f), MaterialTheme.colorScheme.onSurface, modifier)
}

@Composable
fun SyncWarning(modifier: Modifier = Modifier) {
    MessageBanner(
        stringResource(R.string.sync_offline), Icons.Default.CloudOff,
        MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer, modifier,
    )
}

/** Full-screen error with an optional retry action. */
@Composable
fun ErrorMessage(message: String, modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null) {
    EmptyState(
        icon = Icons.Default.ErrorOutline,
        title = stringResource(R.string.error_title),
        message = message,
        modifier = modifier,
        action = onRetry?.let { retry ->
            { LetsSignTextButton(stringResource(R.string.action_retry), onClick = retry) }
        },
    )
}

@Composable
private fun MessageBanner(
    message: String,
    icon: ImageVector,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().clip(AppShapes.message).background(container).padding(Spacing.md + Spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(Dimens.iconSmall))
        Spacer(Modifier.width(Spacing.sm + Spacing.xxs))
        Text(message, color = content, style = MaterialTheme.typography.bodyMedium)
    }
}
