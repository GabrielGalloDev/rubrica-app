package br.com.fiap.letssign.utils

import androidx.annotation.StringRes
import br.com.fiap.letssign.R

object Validators {
    const val MIN_PASSWORD_LENGTH = 6
    private const val MIN_NAME_LENGTH = 3
    private val PHONE_DIGITS = 10..13

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    /** Full name: at least two words. */
    @StringRes
    fun validateName(value: String): Int? = when {
        value.isBlank() -> R.string.error_name_required
        value.trim().length < MIN_NAME_LENGTH || !value.trim().contains(" ") -> R.string.error_name_full
        else -> null
    }

    @StringRes
    fun validateEmail(value: String): Int? = when {
        value.isBlank() -> R.string.error_email_required
        !EMAIL_REGEX.matches(value.trim()) -> R.string.error_email_invalid
        else -> null
    }

    @StringRes
    fun validatePhone(value: String): Int? = when {
        value.isBlank() -> R.string.error_phone_required
        value.count { it.isDigit() } !in PHONE_DIGITS -> R.string.error_phone_invalid
        else -> null
    }

    @StringRes
    fun validatePassword(value: String): Int? = when {
        value.isEmpty() -> R.string.error_password_required
        value.length < MIN_PASSWORD_LENGTH -> R.string.error_password_too_short
        else -> null
    }

    /** Used when editing: an empty password keeps the current one. */
    @StringRes
    fun validateOptionalPassword(value: String): Int? =
        if (value.isEmpty()) null else validatePassword(value)

    @StringRes
    fun validatePasswordConfirmation(password: String, confirmation: String): Int? = when {
        password.isEmpty() && confirmation.isEmpty() -> null
        confirmation.isEmpty() -> R.string.error_password_required
        password != confirmation -> R.string.error_password_mismatch
        else -> null
    }

    @StringRes
    fun validateRequired(value: String, @StringRes errorRes: Int): Int? =
        if (value.isBlank()) errorRes else null
}
