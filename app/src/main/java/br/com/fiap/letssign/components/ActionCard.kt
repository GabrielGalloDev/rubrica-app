package br.com.fiap.letssign.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import br.com.fiap.letssign.ui.theme.AppShapes
import br.com.fiap.letssign.ui.theme.Dimens
import br.com.fiap.letssign.ui.theme.LetsSignTheme
import br.com.fiap.letssign.ui.theme.Spacing

/** Shortcut card on the home screen; [highlighted] marks the main action. */
@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) {
    val colors = LetsSignTheme.extendedColors
    val container = if (highlighted) colors.highlight else MaterialTheme.colorScheme.surfaceContainerLowest
    val content = if (highlighted) colors.onHighlight else MaterialTheme.colorScheme.onSurface
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.cardLarge,
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
    ) {
        Column(Modifier.padding(Spacing.lg + Spacing.xxs)) {
            Box(
                Modifier.size(Dimens.iconContainer).clip(AppShapes.iconBox)
                    .background(if (highlighted) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon, contentDescription = null,
                    tint = if (highlighted) Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens.icon),
                )
            }
            Spacer(Modifier.height(Spacing.lg))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = content.copy(alpha = 0.65f))
        }
    }
}
