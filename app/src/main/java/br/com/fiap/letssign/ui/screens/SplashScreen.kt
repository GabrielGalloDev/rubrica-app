package br.com.fiap.letssign.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import br.com.fiap.letssign.components.LoadingIndicator

/** Shown while the saved session is restored. */
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    LoadingIndicator(modifier)
}
