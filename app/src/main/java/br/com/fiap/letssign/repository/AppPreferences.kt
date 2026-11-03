package br.com.fiap.letssign.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val tag: String?) {
    SYSTEM(null),
    ENGLISH("en"),
    PORTUGUESE_BR("pt-BR");

    companion object {
        fun fromTag(tag: String?): AppLanguage =
            entries.firstOrNull { it.tag == tag } ?: SYSTEM
    }
}

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromName(name: String?): AppTheme =
            entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}

class AppPreferences(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    private val languageState = MutableStateFlow(
        AppLanguage.fromTag(preferences.getString(KEY_LANGUAGE, null))
    )
    val language: StateFlow<AppLanguage> = languageState.asStateFlow()

    private val themeState = MutableStateFlow(
        AppTheme.fromName(preferences.getString(KEY_THEME, null))
    )
    val theme: StateFlow<AppTheme> = themeState.asStateFlow()

    /** Id of the signed-in user, kept between app launches. */
    var currentUserId: String?
        get() = preferences.getString(KEY_USER_ID, null)
        set(value) = preferences.edit { putString(KEY_USER_ID, value) }

    fun setLanguage(language: AppLanguage) {
        preferences.edit { putString(KEY_LANGUAGE, language.tag) }
        languageState.value = language
    }

    fun setTheme(theme: AppTheme) {
        preferences.edit { putString(KEY_THEME, theme.name) }
        themeState.value = theme
    }

    private companion object {
        const val FILE_NAME = "letssign_preferences"
        const val KEY_LANGUAGE = "language_tag"
        const val KEY_THEME = "theme"
        const val KEY_USER_ID = "user_id"
    }
}
