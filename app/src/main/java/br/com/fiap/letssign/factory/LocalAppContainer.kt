package br.com.fiap.letssign.factory

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("No AppContainer provided. Wrap the composition in a CompositionLocalProvider.")
}
