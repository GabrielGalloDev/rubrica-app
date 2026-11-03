package br.com.fiap.letssign.utils

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Date formats follow the app language (e.g. 10/27/2026 in English, 27/10/2026 in Portuguese). */
object Formatters {
    private const val DATE_SKELETON = "ddMMyyyy"
    private const val DATE_TIME_SKELETON = "ddMMyyyyHHmm"

    fun date(millis: Long, locale: Locale): String = format(millis, locale, DATE_SKELETON)

    fun dateTime(millis: Long, locale: Locale): String = format(millis, locale, DATE_TIME_SKELETON)

    private fun format(millis: Long, locale: Locale, skeleton: String): String =
        SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, skeleton), locale).format(Date(millis))
}

@Composable
@ReadOnlyComposable
fun currentLocale(): Locale = LocalConfiguration.current.locales[0]

@Composable
@ReadOnlyComposable
fun Long.formatDate(): String = Formatters.date(this, currentLocale())

@Composable
@ReadOnlyComposable
fun Long.formatDateTime(): String = Formatters.dateTime(this, currentLocale())
