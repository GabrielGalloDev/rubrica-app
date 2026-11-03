package br.com.fiap.letssign

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.letssign.factory.LocalAppContainer
import br.com.fiap.letssign.navigation.LetsSignApp
import br.com.fiap.letssign.repository.AppLanguage
import br.com.fiap.letssign.repository.AppTheme
import br.com.fiap.letssign.ui.theme.LetsSignTheme
import br.com.fiap.letssign.utils.LocaleManager

class MainActivity : ComponentActivity() {
    private val container get() = (application as LetsSignApplication).container

    override fun attachBaseContext(newBase: Context) {
        val preferences = (newBase.applicationContext as? LetsSignApplication)?.container?.preferences
        val language = preferences?.language?.value ?: AppLanguage.SYSTEM
        super.attachBaseContext(LocaleManager.wrap(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme by container.preferences.theme.collectAsStateWithLifecycle()
            val darkTheme = when (theme) {
                AppTheme.SYSTEM -> isSystemInDarkTheme()
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }
            // System bar icons follow the app theme, not only the system one.
            DisposableEffect(darkTheme) {
                applySystemBars(darkTheme)
                onDispose { }
            }

            CompositionLocalProvider(LocalAppContainer provides container) {
                LetsSignTheme(darkTheme = darkTheme) {
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        LetsSignApp(onLanguageApplied = ::applyLanguage)
                    }
                }
            }
        }
    }

    private fun applySystemBars(darkTheme: Boolean) {
        val style = if (darkTheme) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }

    private fun applyLanguage(language: AppLanguage) {
        container.preferences.setLanguage(language)
        LocaleManager.apply(this, language)
        if (LocaleManager.requiresRecreation()) recreate()
    }
}
