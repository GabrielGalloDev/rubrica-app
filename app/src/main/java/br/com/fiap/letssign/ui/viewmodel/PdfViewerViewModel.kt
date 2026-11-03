package br.com.fiap.letssign.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.fiap.letssign.factory.AppContainer
import br.com.fiap.letssign.model.AppError
import br.com.fiap.letssign.model.DocumentStatus
import br.com.fiap.letssign.model.UiState
import br.com.fiap.letssign.model.User
import br.com.fiap.letssign.model.toUiState
import br.com.fiap.letssign.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class PdfViewerContent(
    val file: File,
    val title: String,
    /** True when showing the final PDF of a completed document. */
    val isFinal: Boolean,
)

class PdfViewerViewModel(
    private val repository: DocumentRepository,
    private val user: User,
    private val documentId: String,
    private val signed: Boolean,
) : ViewModel() {
    private val state = MutableStateFlow<UiState<PdfViewerContent>>(UiState.Loading)
    val uiState: StateFlow<UiState<PdfViewerContent>> = state.asStateFlow()

    init {
        load()
    }

    fun load() {
        state.value = UiState.Loading
        viewModelScope.launch {
            state.value = runCatching {
                val doc = repository.find(documentId) ?: throw AppError.DocumentNotFound
                // Only the owner or a participant can view the document.
                if (doc.ownerId != user.id && doc.signerForUser(user.id) == null) throw AppError.AccessDenied
                val completed = doc.status == DocumentStatus.COMPLETED
                val file = when {
                    !signed -> repository.ensureOriginalPdf(doc)
                    completed && doc.finalPdfPath?.let { File(it).exists() } == true -> File(doc.finalPdfPath)
                    else -> repository.buildSignedPdf(doc.id)
                }
                PdfViewerContent(file, doc.title, isFinal = completed && signed)
            }.toUiState()
        }
    }

    companion object {
        fun factory(container: AppContainer, user: User, documentId: String, signed: Boolean) = viewModelFactory {
            initializer { PdfViewerViewModel(container.documentRepository, user, documentId, signed) }
        }
    }
}
