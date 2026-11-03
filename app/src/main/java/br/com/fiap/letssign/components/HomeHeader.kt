package br.com.fiap.letssign.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.fiap.letssign.R
import br.com.fiap.letssign.ui.theme.AppShapes
import br.com.fiap.letssign.ui.theme.BrandGradient
import br.com.fiap.letssign.ui.theme.Dimens
import br.com.fiap.letssign.ui.theme.Spacing
import java.util.Calendar

/** Gradient header of the home screens: greeting, actions and a slot for statistics. */
@Composable
fun HomeHeader(
    name: String,
    subtitle: String,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    content: @Composable () -> Unit,
) {
    LightStatusBarIcons()
    Column(
        Modifier
            .fillMaxWidth()
            .clip(AppShapes.header)
            .background(BrandGradient)
            .drawBehind {
                // Translucent decorative shapes in the header background.
                drawCircle(Color.White.copy(alpha = 0.07f), radius = size.width * 0.45f, center = Offset(size.width * 0.95f, 0f))
                drawCircle(Color.White.copy(alpha = 0.05f), radius = size.width * 0.3f, center = Offset(size.width * 0.1f, size.height))
            }
            .statusBarsPadding()
            .padding(horizontal = Dimens.screenPadding, vertical = Spacing.lg),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(Dimens.avatar).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) { Text(initials(name), color = Color.White, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.home_greeting, stringResource(greetingRes())),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f),
                )
                Text(
                    name.substringBefore(" "),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            HeaderIconButton(Icons.Default.Settings, stringResource(R.string.cd_settings), onOpenSettings)
            Spacer(Modifier.width(Spacing.sm))
            HeaderIconButton(Icons.Default.Refresh, stringResource(R.string.cd_refresh), onRefresh)
            Spacer(Modifier.width(Spacing.sm))
            HeaderIconButton(Icons.AutoMirrored.Filled.Logout, stringResource(R.string.cd_logout), onLogout)
        }
        Spacer(Modifier.height(Spacing.xl))
        Text(subtitle, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
        Spacer(Modifier.height(Spacing.sm + Spacing.xxs))
        content()
        Spacer(Modifier.height(Spacing.sm))
        if (refreshing) {
            LinearProgressIndicator(
                Modifier.fillMaxWidth().height(2.dp).clip(CircleShape),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.15f),
            )
        } else {
            Spacer(Modifier.height(2.dp))
        }
    }
}

@Composable
fun HeaderIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        Modifier.size(Dimens.headerButton).clip(CircleShape).background(Color.White.copy(alpha = 0.14f)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, description, tint = Color.White, modifier = Modifier.size(20.dp)) }
}

@StringRes
private fun greetingRes(): Int = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> R.string.home_greeting_morning
    in 12..17 -> R.string.home_greeting_afternoon
    else -> R.string.home_greeting_evening
}
