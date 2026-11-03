package br.com.fiap.letssign.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.fiap.letssign.ui.theme.AppShapes
import br.com.fiap.letssign.ui.theme.Dimens
import br.com.fiap.letssign.ui.theme.Spacing

/** Primary full-width button with an optional icon and loading indicator. */
@Composable
fun LetsSignButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(Dimens.buttonHeight),
        enabled = enabled && !isLoading,
        shape = AppShapes.button,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(Dimens.icon),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            ButtonContent(text, icon, bold = true)
        }
    }
}

@Composable
fun LetsSignOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(Dimens.buttonHeight),
        enabled = enabled,
        shape = AppShapes.button,
        border = BorderStroke(Dimens.borderWidth, MaterialTheme.colorScheme.outline),
    ) {
        ButtonContent(text, icon)
    }
}

@Composable
fun LetsSignTonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(Dimens.buttonHeight),
        enabled = enabled,
        shape = AppShapes.button,
    ) {
        ButtonContent(text, icon)
    }
}

@Composable
fun LetsSignTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = Spacing.xs, vertical = Spacing.xxs),
    ) {
        Text(text = text, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun RowScope.ButtonContent(text: String, icon: ImageVector?, bold: Boolean = false) {
    if (icon != null) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(Dimens.icon))
        Spacer(Modifier.size(Spacing.sm))
    }
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
    )
}
