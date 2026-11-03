package br.com.fiap.letssign.model

sealed class AppError(cause: Throwable? = null) : Exception(cause) {
    data object RemoteUnavailable : AppError() {
        private fun readResolve(): Any = RemoteUnavailable
    }

    data object Network : AppError() {
        private fun readResolve(): Any = Network
    }

    data object InvalidCredentials : AppError() {
        private fun readResolve(): Any = InvalidCredentials
    }

    data object EmailAlreadyInUse : AppError() {
        private fun readResolve(): Any = EmailAlreadyInUse
    }

    data object InvalidEmail : AppError() {
        private fun readResolve(): Any = InvalidEmail
    }

    data object WeakPassword : AppError() {
        private fun readResolve(): Any = WeakPassword
    }

    data object UserNotFound : AppError() {
        private fun readResolve(): Any = UserNotFound
    }

    data object ProfileNotFound : AppError() {
        private fun readResolve(): Any = ProfileNotFound
    }

    data object TooManyRequests : AppError() {
        private fun readResolve(): Any = TooManyRequests
    }

    /** E-mail/password provider disabled in the Firebase console. */
    data object AuthProviderDisabled : AppError() {
        private fun readResolve(): Any = AuthProviderDisabled
    }

    data object PermissionDenied : AppError() {
        private fun readResolve(): Any = PermissionDenied
    }

    data object DocumentNotFound : AppError() {
        private fun readResolve(): Any = DocumentNotFound
    }

    data object AccessDenied : AppError() {
        private fun readResolve(): Any = AccessDenied
    }

    /** Sent documents are preserved; changes must go to a new version. */
    data object DocumentLocked : AppError() {
        private fun readResolve(): Any = DocumentLocked
    }

    data object PdfEmpty : AppError() {
        private fun readResolve(): Any = PdfEmpty
    }

    data object PdfTooLarge : AppError() {
        private fun readResolve(): Any = PdfTooLarge
    }

    data object PdfInvalidFormat : AppError() {
        private fun readResolve(): Any = PdfInvalidFormat
    }

    data object PdfNoPages : AppError() {
        private fun readResolve(): Any = PdfNoPages
    }

    data object PdfPasswordProtected : AppError() {
        private fun readResolve(): Any = PdfPasswordProtected
    }

    data object PdfCorrupted : AppError() {
        private fun readResolve(): Any = PdfCorrupted
    }

    /** Business rules that failed before sending a document. */
    class Validation(val issues: List<ValidationIssue>) : AppError()

    class Unexpected(cause: Throwable? = null) : AppError(cause)
}
