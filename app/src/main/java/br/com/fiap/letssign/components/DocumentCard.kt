package br.com.fiap.letssign.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.fiap.letssign.R
import br.com.fiap.letssign.model.Document
import br.com.fiap.letssign.model.DocumentStatus
import br.com.fiap.letssign.ui.theme.AppShapes
import br.com.fiap.letssign.ui.theme.Dimens
import br.com.fiap.letssign.ui.theme.Spacing
import br.com.fiap.letssign.ui.theme.statusColor

@Composable
fun DocumentCard(
    document: Document,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: (@Composable () -> Unit)? = null,
) {
    val accent = statusColor(document.status)
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Column(Modifier.padding(Dimens.cardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(Dimens.avatar).clip(AppShapes.iconBox).background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Default.Description, contentDescription = null, tint = accent, modifier = Modifier.size(Dimens.icon)) }
                Spacer(Modifier.width(Spacing.md + Spacing.xxs))
                Column(Modifier.weight(1f)) {
                    Text(document.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(Spacing.xxs))
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
            }
            Spacer(Modifier.height(Spacing.md + Spacing.xxs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (badge != null) badge() else StatusChip(document.status)
                Spacer(Modifier.weight(1f))
                if (document.signers.isNotEmpty()) {
                    Text(
                        stringResource(R.string.document_signatures_progress, document.signedCount, document.signers.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (document.status != DocumentStatus.DRAFT && document.signers.isNotEmpty()) {
                Spacer(Modifier.height(Spacing.sm + Spacing.xxs))
                LinearProgressIndicator(
                    progress = { document.signedCount.toFloat() / document.signers.size },
                    modifier = Modifier.fillMaxWidth().height(Dimens.progressHeight),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.12f),
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                )
            }
        }
    }
}
