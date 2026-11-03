package br.com.fiap.letssign.firebase

import br.com.fiap.letssign.model.AppError
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import java.io.IOException

private const val ERROR_INVALID_EMAIL = "ERROR_INVALID_EMAIL"

internal fun Throwable.toAppError(): AppError = when (this) {
    is AppError -> this
    is FirebaseAuthWeakPasswordException -> AppError.WeakPassword
    is FirebaseAuthUserCollisionException -> AppError.EmailAlreadyInUse
    is FirebaseAuthInvalidUserException -> AppError.UserNotFound
    is FirebaseAuthInvalidCredentialsException ->
        if (errorCode == ERROR_INVALID_EMAIL) AppError.InvalidEmail else AppError.InvalidCredentials
    is FirebaseTooManyRequestsException -> AppError.TooManyRequests
    is FirebaseNetworkException -> AppError.Network
    is IOException -> AppError.Network
    is DatabaseException -> if (message.orEmpty().contains("Permission denied", ignoreCase = true)) {
        AppError.PermissionDenied
    } else {
        AppError.Unexpected(this)
    }
    // Project without the e-mail/password provider enabled in the console.
    is FirebaseException -> when {
        message.orEmpty().contains("CONFIGURATION_NOT_FOUND") ||
            message.orEmpty().contains("OPERATION_NOT_ALLOWED") -> AppError.AuthProviderDisabled
        else -> AppError.Unexpected(this)
    }
    else -> AppError.Unexpected(this)
}

internal fun DatabaseError.toAppError(): AppError = when (code) {
    DatabaseError.PERMISSION_DENIED -> AppError.PermissionDenied
    DatabaseError.EXPIRED_TOKEN,
    DatabaseError.INVALID_TOKEN,
        -> AppError.InvalidCredentials

    DatabaseError.DISCONNECTED,
    DatabaseError.NETWORK_ERROR,
    DatabaseError.UNAVAILABLE,
        -> AppError.Network

    else -> AppError.Unexpected(toException())
}

/** Runs a Firebase call converting any failure into an [AppError]. */
internal suspend fun <T> firebaseCall(block: suspend () -> T): T = try {
    block()
} catch (cancellation: kotlinx.coroutines.CancellationException) {
    throw cancellation
} catch (throwable: Throwable) {
    throw throwable.toAppError()
}
