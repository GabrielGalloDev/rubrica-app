package br.com.fiap.letssign.ui.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.fiap.letssign.R
import br.com.fiap.letssign.components.CapturedSignature
import br.com.fiap.letssign.factory.AppContainer
import br.com.fiap.letssign.model.AppError
import br.com.fiap.letssign.model.Document
import br.com.fiap.letssign.model.DocumentStatus
import br.com.fiap.letssign.model.SignerStatus
import br.com.fiap.letssign.model.User
import br.com.fiap.letssign.repository.DocumentRepository
import br.com.fiap.letssign.utils.asAppError
import br.com.fiap.letssign.utils.messageRes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class SignDocumentUiState(
    val document: Document? = null,
    val pdf: File? = null,
    @param:StringRes val loadError: Int? = null,
    val captured: CapturedSignature? = null,
    /** Signature already saved (when the user reopens a document they signed). */
    val savedSignature: Bitmap? = null,
    val isPadVisible: Boolean = false,
    val isSaving: Boolean = false,
    @param:StringRes val saveError: Int? = null,
    val justSigned: Document? = null,
)

class SignDocumentViewModel(
    private val repository: DocumentRepository,
    private val user: User,
    private val documentId: String,
) : ViewModel() {
    private val state = MutableStateFlow(SignDocumentUiState())
    val uiState: StateFlow<SignDocumentUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observe(documentId).collect { document -> state.update { it.copy(document = document) } }
        }
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        runCatching { repository.refresh(documentId) }
        val doc = repository.find(documentId)
        when {
            doc == null -> state.update { it.copy(loadError = AppError.DocumentNotFound.messageRes) }
            doc.signerForUser(user.id) == null || doc.status == DocumentStatus.DRAFT ->
                state.update { it.copy(loadError = AppError.AccessDenied.messageRes) }
            else -> runCatching {
                val pdf = repository.ensureOriginalPdf(doc)
                repository.registerView(doc, user)
                state.update { it.copy(pdf = pdf, savedSignature = loadSavedSignature(doc, pdf)) }
            }.onFailure { state.update { it.copy(loadError = R.string.sign_download_error) } }
        }
    }

    private fun loadSavedSignature(doc: Document, pdf: File): Bitmap? {
        val me = doc.signerForUser(user.id) ?: return null
        if (me.status != SignerStatus.SIGNED) return null
        val file = File(pdf.parentFile, "signatures/${me.id}.png")
        return if (file.exists()) BitmapFactory.decodeFile(file.path) else null
    }

    fun openPad() = state.update { it.copy(isPadVisible = true) }

    fun dismissPad() = state.update { it.copy(isPadVisible = false) }

    fun onSignatureCaptured(signature: CapturedSignature) =
        state.update { it.copy(captured = signature, isPadVisible = false, saveError = null) }

    fun save() {
        val signature = state.value.captured ?: return
        state.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            runCatching { repository.sign(documentId, user, signature.png) }
                .onSuccess { signed ->
                    state.update { it.copy(isSaving = false, justSigned = signed, savedSignature = signature.bitmap, captured = null) }
                }
                .onFailure { throwable ->
                    val error = throwable.asAppError()
                    val message = if (error is AppError.Unexpected) R.string.sign_save_error else error.messageRes
                    state.update { it.copy(isSaving = false, saveError = message) }
                }
        }
    }

    fun dismissSignedDialog() = state.update { it.copy(justSigned = null) }

    companion object {
        fun factory(container: AppContainer, user: User, documentId: String) = viewModelFactory {
            initializer { SignDocumentViewModel(container.documentRepository, user, documentId) }
        }
    }
}
