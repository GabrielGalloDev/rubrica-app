package br.com.fiap.letssign.components

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import br.com.fiap.letssign.ui.theme.LetsSignTheme

// How many screens with a dark header are composed (they can coexist during a transition).
private var darkHeaders = 0

/** Makes the status bar icons light while the screen shows a dark header. */
@Composable
fun LightStatusBarIcons() {
    val view = LocalView.current
    if (view.isInEditMode) return
    val isDarkTheme = LetsSignTheme.extendedColors.isDark
    DisposableEffect(isDarkTheme) {
        val controller = WindowCompat.getInsetsController((view.context as Activity).window, view)
        darkHeaders++
        controller.isAppearanceLightStatusBars = false
        onDispose {
            darkHeaders--
            // Back to the theme default: dark icons only on the light theme.
            if (darkHeaders == 0) controller.isAppearanceLightStatusBars = !isDarkTheme
        }
    }
}
