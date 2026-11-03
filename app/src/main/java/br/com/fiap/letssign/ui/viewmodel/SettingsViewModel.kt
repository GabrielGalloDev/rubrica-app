package br.com.fiap.letssign.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.fiap.letssign.factory.AppContainer
import br.com.fiap.letssign.repository.AppLanguage
import br.com.fiap.letssign.repository.AppPreferences
import br.com.fiap.letssign.repository.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val language: AppLanguage = AppLanguage.SYSTEM,
    val theme: AppTheme = AppTheme.SYSTEM,
    val isLanguageDialogVisible: Boolean = false,
    val isThemeDialogVisible: Boolean = false,
    /** Language picked but not applied yet (applying it may recreate the screen). */
    val pendingLanguage: AppLanguage? = null,
)

class SettingsViewModel(private val preferences: AppPreferences) : ViewModel() {
    private val dialogs = MutableStateFlow(SettingsUiState())

    val uiState: StateFlow<SettingsUiState> =
        combine(preferences.language, preferences.theme, dialogs) { language, theme, current ->
            current.copy(language = language, theme = theme)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            SettingsUiState(language = preferences.language.value, theme = preferences.theme.value),
        )

    fun openLanguageDialog() = dialogs.update { it.copy(isLanguageDialogVisible = true) }

    fun dismissLanguageDialog() = dialogs.update { it.copy(isLanguageDialogVisible = false) }

    fun selectLanguage(language: AppLanguage) = dialogs.update {
        it.copy(
            isLanguageDialogVisible = false,
            pendingLanguage = language.takeIf { selected -> selected != preferences.language.value },
        )
    }

    fun consumePendingLanguage() = dialogs.update { it.copy(pendingLanguage = null) }

    fun openThemeDialog() = dialogs.update { it.copy(isThemeDialogVisible = true) }

    fun dismissThemeDialog() = dialogs.update { it.copy(isThemeDialogVisible = false) }

    /** The theme applies right away: the activity observes the preference. */
    fun selectTheme(theme: AppTheme) {
        preferences.setTheme(theme)
        dialogs.update { it.copy(isThemeDialogVisible = false) }
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { SettingsViewModel(container.preferences) }
        }
    }
}
