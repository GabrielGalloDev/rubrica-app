package br.com.fiap.letssign.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import br.com.fiap.letssign.model.DocumentStatus
import br.com.fiap.letssign.ui.theme.Dimens
import br.com.fiap.letssign.ui.theme.LetsSignTheme
import br.com.fiap.letssign.ui.theme.Spacing
import br.com.fiap.letssign.ui.theme.statusColor
import br.com.fiap.letssign.utils.labelRes

@Composable
fun StatusChip(status: DocumentStatus, modifier: Modifier = Modifier) {
    Pill(stringResource(status.labelRes), statusColor(status), modifier)
}

/** Small colored label with a dot. */
@Composable
fun Pill(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(color = color.copy(alpha = 0.14f), shape = RoundedCornerShape(50), modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Spacing.sm + Spacing.xxs, vertical = Spacing.xs),
        ) {
            Box(Modifier.size(Dimens.statusDot).background(color, CircleShape))
            Spacer(Modifier.size(Spacing.xs + Spacing.xxs))
            Text(text, color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Pill-shaped toggle used as a filter or tab. */
@Composable
fun SegmentPill(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LetsSignTheme.extendedColors
    val background = if (selected) colors.highlight else MaterialTheme.colorScheme.surfaceContainerLowest
    Box(
        modifier
            .clip(CircleShape)
            .background(background)
            .border(Dimens.borderWidth, if (selected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm + Spacing.xxs),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) colors.onHighlight else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
