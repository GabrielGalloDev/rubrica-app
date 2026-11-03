package br.com.fiap.letssign.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** App colors outside the Material scheme, with a value for each theme. */
@Immutable
data class LetsSignExtendedColors(
    val isDark: Boolean,
    /** Strong fill of the main shortcut and of selected filters. */
    val highlight: Color,
    val onHighlight: Color,
    val onHighlightMuted: Color,
)

private val LightExtendedColors = LetsSignExtendedColors(
    isDark = false,
    highlight = Ink,
    onHighlight = Color.White,
    onHighlightMuted = Color.White.copy(alpha = 0.65f),
)

private val DarkExtendedColors = LetsSignExtendedColors(
    isDark = true,
    highlight = Indigo,
    onHighlight = Color.White,
    onHighlightMuted = Color.White.copy(alpha = 0.75f),
)

// Light gray background with white, shadowless cards: a clean, flat look.
private val LightColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFECEAFF),
    onPrimaryContainer = IndigoDark,
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5F7F1),
    onSecondaryContainer = Color(0xFF0F4F47),
    tertiary = Violet,
    tertiaryContainer = Color(0xFFFFF4DB),
    onTertiaryContainer = Color(0xFF6B4300),
    background = Color(0xFFF5F6FA),
    onBackground = Ink,
    surface = Color(0xFFF5F6FA),
    onSurface = Ink,
    surfaceVariant = Color(0xFFEEF0F6),
    onSurfaceVariant = Color(0xFF5B6275),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White,
    outline = Color(0xFFD6D9E4),
    outlineVariant = Color(0xFFE9EBF2),
    error = Color(0xFFE5484D),
    errorContainer = Color(0xFFFFECEC),
    onErrorContainer = Color(0xFF8C1D22),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA89FFF),
    onPrimary = IndigoDeep,
    primaryContainer = Color(0xFF2E2680),
    onPrimaryContainer = Color(0xFFE4E0FF),
    secondary = Color(0xFF5EEAD4),
    tertiary = Color(0xFFC4B5FD),
    tertiaryContainer = Color(0xFF3D2E0A),
    onTertiaryContainer = Color(0xFFFFE2A8),
    background = Color(0xFF0B0B14),
    onBackground = Color(0xFFECEDF5),
    surface = Color(0xFF0B0B14),
    onSurface = Color(0xFFECEDF5),
    surfaceVariant = Color(0xFF1E1F2E),
    onSurfaceVariant = Color(0xFFA3A8BC),
    surfaceContainerLowest = Color(0xFF13131F),
    surfaceContainerLow = Color(0xFF15151F),
    surfaceContainer = Color(0xFF181824),
    surfaceContainerHigh = Color(0xFF1C1C2A),
    surfaceContainerHighest = Color(0xFF181824),
    outline = Color(0xFF34364A),
    outlineVariant = Color(0xFF26283A),
)

private val LocalLetsSignExtendedColors = staticCompositionLocalOf { LightExtendedColors }

@Composable
fun LetsSignTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalLetsSignExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LetsSignTypography,
            shapes = LetsSignShapes,
            content = content,
        )
    }
}

object LetsSignTheme {
    val extendedColors: LetsSignExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalLetsSignExtendedColors.current
}
