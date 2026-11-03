package br.com.fiap.letssign.ui.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.fiap.letssign.factory.AppContainer
import br.com.fiap.letssign.model.Document
import br.com.fiap.letssign.model.User
import br.com.fiap.letssign.repository.DocumentRepository
import br.com.fiap.letssign.utils.toMessageRes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DocumentDetailUiState(
    val document: Document? = null,
    val isLoaded: Boolean = false,
    val isBusy: Boolean = false,
    val isDeleteDialogVisible: Boolean = false,
    @param:StringRes val error: Int? = null,
)

class DocumentDetailViewModel(
    private val repository: DocumentRepository,
    private val admin: User,
    private val documentId: String,
) : ViewModel() {
    private val state = MutableStateFlow(DocumentDetailUiState())
    val uiState: StateFlow<DocumentDetailUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observe(documentId).collect { document ->
                // One user's data can't be accessed by another.
                state.update { it.copy(document = document?.takeIf { doc -> doc.ownerId == admin.id }) }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            runCatching { repository.refresh(documentId) }
            state.update { it.copy(isLoaded = true) }
        }
    }

    fun createNewVersion(onCreated: (String) -> Unit) {
        state.update { it.copy(isBusy = true, error = null) }
        viewModelScope.launch {
            runCatching { repository.createNewVersion(documentId, admin) }
                .onSuccess { onCreated(it.id) }
                .onFailure { throwable -> state.update { it.copy(error = throwable.toMessageRes()) } }
            state.update { it.copy(isBusy = false) }
        }
    }

    fun openDeleteDialog() = state.update { it.copy(isDeleteDialogVisible = true) }

    fun dismissDeleteDialog() = state.update { it.copy(isDeleteDialogVisible = false) }

    fun deleteDraft(onDeleted: () -> Unit) {
        state.update { it.copy(isDeleteDialogVisible = false) }
        viewModelScope.launch {
            runCatching { repository.deleteDraft(documentId) }
                .onSuccess { onDeleted() }
                .onFailure { throwable -> state.update { it.copy(error = throwable.toMessageRes()) } }
        }
    }

    companion object {
        fun factory(container: AppContainer, admin: User, documentId: String) = viewModelFactory {
            initializer { DocumentDetailViewModel(container.documentRepository, admin, documentId) }
        }
    }
}
