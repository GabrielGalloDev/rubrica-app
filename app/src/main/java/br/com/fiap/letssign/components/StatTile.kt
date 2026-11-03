package br.com.fiap.letssign.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import br.com.fiap.letssign.ui.theme.AppShapes
import br.com.fiap.letssign.ui.theme.Dimens
import br.com.fiap.letssign.ui.theme.Spacing

/** Translucent statistic tile placed over the gradient header. */
@Composable
fun StatTile(label: String, value: Int, icon: ImageVector, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(AppShapes.tile)
            .background(Color.White.copy(alpha = 0.12f))
            .border(Dimens.borderWidth, Color.White.copy(alpha = 0.16f), AppShapes.tile)
            .padding(Spacing.md + Spacing.xxs),
    ) {
        Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(Dimens.iconSmall))
        Spacer(Modifier.height(Spacing.sm + Spacing.xxs))
        Text("$value", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.ExtraBold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
    }
}
