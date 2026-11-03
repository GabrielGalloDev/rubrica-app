package br.com.fiap.letssign.model

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>

    data class Success<T>(val data: T) : UiState<T>

    data object Empty : UiState<Nothing>

    data class Error(val error: AppError) : UiState<Nothing>
}

fun <T> Result<T>.toUiState(isEmpty: (T) -> Boolean = { false }): UiState<T> = fold(
    onSuccess = { value -> if (isEmpty(value)) UiState.Empty else UiState.Success(value) },
    onFailure = { throwable ->
        UiState.Error(throwable as? AppError ?: AppError.Unexpected(throwable))
    },
)
