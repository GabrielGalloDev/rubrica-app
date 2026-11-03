package br.com.fiap.letssign.utils

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import br.com.fiap.letssign.R
import br.com.fiap.letssign.model.AppError
import br.com.fiap.letssign.model.ValidationIssue

@get:StringRes
val AppError.messageRes: Int
    get() = when (this) {
        AppError.RemoteUnavailable -> R.string.error_remote_unavailable
        AppError.Network -> R.string.error_network
        AppError.InvalidCredentials -> R.string.error_invalid_credentials
        AppError.EmailAlreadyInUse -> R.string.error_email_already_used
        AppError.InvalidEmail -> R.string.error_email_invalid
        AppError.WeakPassword -> R.string.error_password_too_short
        AppError.UserNotFound -> R.string.error_user_not_found
        AppError.ProfileNotFound -> R.string.error_profile_not_found
        AppError.TooManyRequests -> R.string.error_too_many_requests
        AppError.AuthProviderDisabled -> R.string.error_auth_provider_disabled
        AppError.PermissionDenied -> R.string.error_permission_denied
        AppError.DocumentNotFound -> R.string.error_document_not_found
        AppError.AccessDenied -> R.string.error_access_denied
        AppError.DocumentLocked -> R.string.error_document_locked
        AppError.PdfEmpty -> R.string.error_pdf_empty
        AppError.PdfTooLarge -> R.string.error_pdf_too_large
        AppError.PdfInvalidFormat -> R.string.error_pdf_invalid_format
        AppError.PdfNoPages -> R.string.error_pdf_no_pages
        AppError.PdfPasswordProtected -> R.string.error_pdf_password_protected
        AppError.PdfCorrupted -> R.string.error_pdf_corrupted
        is AppError.Validation -> R.string.error_document_invalid
        is AppError.Unexpected -> R.string.error_generic
    }

fun Throwable.asAppError(): AppError = this as? AppError ?: AppError.Unexpected(this)

@StringRes
fun Throwable.toMessageRes(): Int = asAppError().messageRes

@Composable
fun ValidationIssue.text(): String = stringResource(messageRes, *args.toTypedArray())

/** Full message of an error, listing each issue of a validation failure. */
@Composable
fun AppError.text(): String = when (this) {
    is AppError.Validation -> issues.map { "• ${it.text()}" }.joinToString("\n")
    else -> stringResource(messageRes)
}
