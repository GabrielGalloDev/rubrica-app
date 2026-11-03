package br.com.fiap.letssign.ui.viewmodel

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.fiap.letssign.R
import br.com.fiap.letssign.factory.AppContainer
import br.com.fiap.letssign.model.AppError
import br.com.fiap.letssign.model.Document
import br.com.fiap.letssign.model.DocumentEvent
import br.com.fiap.letssign.model.DocumentSigner
import br.com.fiap.letssign.model.DocumentStatus
import br.com.fiap.letssign.model.EventType
import br.com.fiap.letssign.model.SignatureField
import br.com.fiap.letssign.model.SignerRole
import br.com.fiap.letssign.model.User
import br.com.fiap.letssign.model.ValidationIssue
import br.com.fiap.letssign.repository.DocumentRepository
import br.com.fiap.letssign.repository.SignerRepository
import br.com.fiap.letssign.utils.DocumentValidator
import br.com.fiap.letssign.utils.asAppError
import br.com.fiap.letssign.utils.messageRes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

sealed interface SendState {
    data object Idle : SendState
    data object Sending : SendState
    data class Success(val document: Document) : SendState
    data class Failure(val error: AppError) : SendState
}

enum class WizardStep(@param:StringRes val titleRes: Int) {
    INFO(R.string.wizard_step_document),
    SIGNERS(R.string.wizard_step_signers),
    FIELDS(R.string.wizard_step_fields),
    REVIEW(R.string.wizard_step_review),
}

data class NewDocumentUiState(
    val step: WizardStep = WizardStep.INFO,
    val isLoading: Boolean = false,
    // Step 1: information and file
    val title: String = "",
    val message: String = "",
    val fileName: String = "",
    val pageCount: Int = 0,
    val pdfFile: File? = null,
    val isImporting: Boolean = false,
    @param:StringRes val pdfError: Int? = null,
    // Step 2: signers
    val signers: List<DocumentSigner> = emptyList(),
    // Step 3: fields
    val fields: List<SignatureField> = emptyList(),
    val currentPage: Int = 0,
    val activeSignerId: String? = null,
    val selectedFieldId: String? = null,
    // Navigation and sending
    val stepError: ValidationIssue? = null,
    val sendState: SendState = SendState.Idle,
    val version: Int = 1,
    val parentId: String? = null,
) {
    val isNewVersion: Boolean get() = parentId != null

    fun signerIndex(signerId: String): Int = signers.indexOfFirst { it.id == signerId }.coerceAtLeast(0)

    fun signerName(signerId: String): String = signers.firstOrNull { it.id == signerId }?.name.orEmpty()
}

class NewDocumentViewModel(
    private val documents: DocumentRepository,
    signersRepository: SignerRepository,
    private val admin: User,
    draftId: String?,
    defaultMessage: String,
) : ViewModel() {

    val documentId: String = draftId ?: UUID.randomUUID().toString()

    private val state = MutableStateFlow(NewDocumentUiState(isLoading = draftId != null, message = defaultMessage))
    val uiState: StateFlow<NewDocumentUiState> = state.asStateFlow()

    val availableSigners: StateFlow<List<User>> = signersRepository.observeSigners(admin.id)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var createdAt = System.currentTimeMillis()
    private var events: List<DocumentEvent> = emptyList()

    init {
        viewModelScope.launch { runCatching { signersRepository.refresh(admin.id) } }
        if (draftId != null) viewModelScope.launch {
            documents.find(draftId)?.let { doc ->
                createdAt = doc.createdAt
                events = doc.events
                state.update {
                    it.copy(
                        title = doc.title,
                        message = doc.message,
                        fileName = doc.fileName,
                        pageCount = doc.pageCount,
                        signers = doc.signers,
                        fields = doc.fields,
                        version = doc.version,
                        parentId = doc.parentId,
                        pdfFile = documents.originalFile(doc.id).takeIf { file -> file.exists() },
                        activeSignerId = doc.signers.firstOrNull()?.id,
                    )
                }
            }
            state.update { it.copy(isLoading = false) }
        }
    }

    // ---------------- File ----------------

    fun onTitleChange(value: String) = state.update { it.copy(title = value, stepError = null) }

    fun onMessageChange(value: String) = state.update { it.copy(message = value) }

    fun onPdfPicked(uri: Uri, displayName: String?) {
        state.update { it.copy(isImporting = true, pdfError = null) }
        viewModelScope.launch {
            runCatching { documents.importPdf(documentId, uri) }
                .onSuccess { pages ->
                    val name = displayName ?: DEFAULT_FILE_NAME
                    state.update {
                        it.copy(
                            pageCount = pages,
                            fileName = name,
                            pdfFile = documents.originalFile(documentId),
                            fields = it.fields.filter { field -> field.page < pages },
                            currentPage = 0,
                            stepError = null,
                            title = it.title.ifBlank { name.removeSuffix(".pdf").removeSuffix(".PDF") },
                        )
                    }
                }
                .onFailure { throwable -> state.update { it.copy(pdfError = throwable.asAppError().messageRes) } }
            state.update { it.copy(isImporting = false) }
        }
    }

    // ---------------- Signers ----------------

    fun addSigner(user: User) = state.update { current ->
        if (current.signers.any { it.userId == user.id }) return@update current
        val signer = DocumentSigner(
            id = UUID.randomUUID().toString(), userId = user.id, name = user.name, email = user.email, phone = user.phone,
            role = if (current.signers.isEmpty()) SignerRole.CONTRACTOR else SignerRole.CONTRACTED,
            order = current.signers.size,
        )
        current.copy(
            signers = current.signers + signer,
            activeSignerId = current.activeSignerId ?: signer.id,
            stepError = null,
        )
    }

    /** Replaces the person while keeping the same participant, so the placed fields are preserved. */
    fun replaceSigner(signerId: String, user: User) = state.update { current ->
        if (current.signers.any { it.userId == user.id }) return@update current
        current.copy(signers = current.signers.map {
            if (it.id == signerId) it.copy(userId = user.id, name = user.name, email = user.email, phone = user.phone) else it
        })
    }

    fun removeSigner(signerId: String) = state.update { current ->
        val signers = current.signers.filterNot { it.id == signerId }.mapIndexed { i, s -> s.copy(order = i) }
        current.copy(
            signers = signers,
            fields = current.fields.filterNot { it.signerId == signerId },
            activeSignerId = if (current.activeSignerId == signerId) signers.firstOrNull()?.id else current.activeSignerId,
        )
    }

    fun setRole(signerId: String, role: SignerRole) = updateSigner(signerId) { it.copy(role = role) }

    fun setRequired(signerId: String, required: Boolean) = updateSigner(signerId) { it.copy(required = required) }

    private fun updateSigner(signerId: String, change: (DocumentSigner) -> DocumentSigner) = state.update { current ->
        current.copy(signers = current.signers.map { if (it.id == signerId) change(it) else it })
    }

    // ---------------- Fields ----------------

    fun selectSigner(signerId: String) = state.update { it.copy(activeSignerId = signerId) }

    fun selectField(field: SignatureField?) = state.update {
        it.copy(selectedFieldId = field?.id, activeSignerId = field?.signerId ?: it.activeSignerId)
    }

    fun addField() = state.update { current ->
        val signerId = current.activeSignerId ?: current.signers.firstOrNull()?.id ?: return@update current
        val sameSignerOnPage = current.fields.count { it.page == current.currentPage && it.signerId == signerId }
        val field = SignatureField(
            id = UUID.randomUUID().toString(), signerId = signerId, page = current.currentPage,
            x = 0.5f - DEFAULT_FIELD_WIDTH / 2,
            y = (FIRST_FIELD_Y + sameSignerOnPage * FIELD_CASCADE).coerceAtMost(1f - DEFAULT_FIELD_HEIGHT),
            width = DEFAULT_FIELD_WIDTH, height = DEFAULT_FIELD_HEIGHT,
        )
        current.copy(fields = current.fields + field, selectedFieldId = field.id, stepError = null)
    }

    /** Moves the field keeping it inside the page bounds (relative values 0..1). */
    fun moveField(id: String, dx: Float, dy: Float) = updateField(id) {
        it.copy(x = (it.x + dx).coerceIn(0f, 1f - it.width), y = (it.y + dy).coerceIn(0f, 1f - it.height))
    }

    fun resizeField(id: String, dw: Float, dh: Float) = updateField(id) {
        it.copy(
            width = (it.width + dw).coerceIn(MIN_FIELD_WIDTH, 1f - it.x),
            height = (it.height + dh).coerceIn(MIN_FIELD_HEIGHT, 1f - it.y),
        )
    }

    private fun updateField(id: String, change: (SignatureField) -> SignatureField) = state.update { current ->
        current.copy(fields = current.fields.map { if (it.id == id) change(it) else it })
    }

    fun deleteField(id: String) = state.update { current ->
        current.copy(
            fields = current.fields.filterNot { it.id == id },
            selectedFieldId = current.selectedFieldId.takeIf { it != id },
        )
    }

    fun goToPage(page: Int) = state.update {
        it.copy(currentPage = page.coerceIn(0, (it.pageCount - 1).coerceAtLeast(0)), selectedFieldId = null)
    }

    // ---------------- Navigation ----------------

    fun next() {
        val current = state.value
        val error = validateStep(current)
        state.update { it.copy(stepError = error) }
        if (error != null) return
        saveDraft()
        state.update { it.copy(step = WizardStep.entries[(it.step.ordinal + 1).coerceAtMost(WizardStep.entries.lastIndex)]) }
    }

    /** Returns false when already on the first step (the screen should then exit). */
    fun back(): Boolean {
        val current = state.value
        if (current.step == WizardStep.INFO) return false
        state.update { it.copy(step = WizardStep.entries[it.step.ordinal - 1], stepError = null) }
        return true
    }

    fun goTo(target: WizardStep) = state.update {
        if (target.ordinal < it.step.ordinal) it.copy(step = target, stepError = null) else it
    }

    private fun validateStep(current: NewDocumentUiState): ValidationIssue? = when (current.step) {
        WizardStep.INFO -> when {
            current.title.isBlank() -> ValidationIssue(R.string.error_document_title_required)
            current.pdfFile == null || current.pageCount == 0 -> ValidationIssue(R.string.error_document_pdf_required)
            else -> null
        }
        WizardStep.SIGNERS -> when {
            current.signers.isEmpty() -> ValidationIssue(R.string.error_document_signer_required)
            current.signers.none { it.required } -> ValidationIssue(R.string.error_document_required_signer)
            else -> null
        }
        WizardStep.FIELDS -> current.signers.firstOrNull { s -> current.fields.none { it.signerId == s.id } }
            ?.let { ValidationIssue(R.string.error_document_signer_without_field, listOf(it.name)) }
        WizardStep.REVIEW -> null
    }

    private fun buildDocument(current: NewDocumentUiState = state.value): Document = Document(
        id = documentId, ownerId = admin.id, ownerName = admin.name,
        title = current.title.trim(), message = current.message.trim(),
        version = current.version, parentId = current.parentId, status = DocumentStatus.DRAFT,
        fileName = current.fileName, pageCount = current.pageCount,
        localPdfPath = current.pdfFile?.path, finalPdfPath = null,
        createdAt = createdAt, sentAt = null, completedAt = null,
        signers = current.signers, fields = current.fields,
        events = events.ifEmpty { listOf(DocumentEvent("$documentId-created", EventType.CREATED, admin.name, createdAt)) },
    )

    fun reviewIssues(current: NewDocumentUiState): List<ValidationIssue> =
        DocumentValidator.validate(buildDocument(current), current.pdfFile?.exists() == true)

    fun saveDraft() {
        if (state.value.pdfFile == null) return
        val document = buildDocument()
        viewModelScope.launch { runCatching { documents.saveDraft(document) } }
    }

    // ---------------- Sending ----------------

    fun send() {
        val current = state.value
        if (current.sendState == SendState.Sending) return
        val issues = reviewIssues(current)
        if (issues.isNotEmpty()) {
            state.update { it.copy(sendState = SendState.Failure(AppError.Validation(issues))) }
            return
        }
        state.update { it.copy(sendState = SendState.Sending) }
        viewModelScope.launch {
            val result = runCatching {
                documents.saveDraft(buildDocument(current))
                documents.send(documentId, admin)
            }
            state.update {
                it.copy(sendState = result.fold({ sent -> SendState.Success(sent) }, { error -> SendState.Failure(error.asAppError()) }))
            }
        }
    }

    fun dismissFailure() = state.update { if (it.sendState is SendState.Failure) it.copy(sendState = SendState.Idle) else it }

    companion object {
        const val DEFAULT_FIELD_WIDTH = 0.32f
        const val DEFAULT_FIELD_HEIGHT = 0.07f
        const val MIN_FIELD_WIDTH = 0.1f
        const val MIN_FIELD_HEIGHT = 0.035f
        private const val FIRST_FIELD_Y = 0.45f
        private const val FIELD_CASCADE = 0.06f
        private const val DEFAULT_FILE_NAME = "document.pdf"

        fun factory(container: AppContainer, admin: User, draftId: String?, defaultMessage: String) = viewModelFactory {
            initializer {
                NewDocumentViewModel(container.documentRepository, container.signerRepository, admin, draftId, defaultMessage)
            }
        }
    }
}
