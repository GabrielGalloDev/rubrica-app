package br.com.fiap.letssign.utils

import android.app.LocaleManager as PlatformLocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import br.com.fiap.letssign.repository.AppLanguage
import java.util.Locale

/** Per-app language: native API on Android 13+, configuration wrapping on older versions. */
object LocaleManager {
    fun apply(context: Context, language: AppLanguage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val service = context.getSystemService(PlatformLocaleManager::class.java)
            service?.applicationLocales = language.tag
                ?.let { LocaleList.forLanguageTags(it) }
                ?: LocaleList.getEmptyLocaleList()
        }
    }

    fun wrap(context: Context, language: AppLanguage): Context {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return context
        val tag = language.tag ?: return context

        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocales(LocaleList(locale))
        return context.createConfigurationContext(configuration)
    }

    fun requiresRecreation(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
}
