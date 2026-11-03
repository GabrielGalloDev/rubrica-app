package br.com.fiap.letssign.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import br.com.fiap.letssign.model.UiState
import br.com.fiap.letssign.utils.text

@Composable
fun <T> UiStateContainer(
    state: UiState<T>,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    emptyContent: @Composable () -> Unit = {},
    content: @Composable (T) -> Unit,
) {
    when (state) {
        UiState.Loading -> LoadingIndicator(modifier = modifier)

        UiState.Empty -> emptyContent()

        is UiState.Error -> ErrorMessage(
            message = state.error.text(),
            modifier = modifier,
            onRetry = onRetry,
        )

        is UiState.Success -> content(state.data)
    }
}
