package br.com.fiap.letssign.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.fiap.letssign.factory.AppContainer
import br.com.fiap.letssign.model.Document
import br.com.fiap.letssign.model.DocumentStatus
import br.com.fiap.letssign.model.SignerStatus
import br.com.fiap.letssign.model.User
import br.com.fiap.letssign.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AdminFilter(val matches: (Document) -> Boolean) {
    ALL({ true }),
    DRAFT({ it.status == DocumentStatus.DRAFT }),
    SENT({ it.status == DocumentStatus.SENT }),
    PARTIAL({ it.status == DocumentStatus.PARTIALLY_SIGNED }),
    COMPLETED({ it.status == DocumentStatus.COMPLETED }),
}

enum class SignerTab { PENDING, SIGNED }

data class HomeUiState(
    val documents: List<Document> = emptyList(),
    val isLoaded: Boolean = false,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val adminFilter: AdminFilter = AdminFilter.ALL,
    val signerTab: SignerTab = SignerTab.PENDING,
) {
    fun count(status: DocumentStatus) = documents.count { it.status == status }

    fun count(filter: AdminFilter) = documents.count(filter.matches)

    val filteredDocuments: List<Document> get() = documents.filter(adminFilter.matches)
}

/** Home of both profiles: the admin sees the documents they own, the signer the received ones. */
class HomeViewModel(
    private val repository: DocumentRepository,
    private val user: User,
    private val asAdmin: Boolean,
) : ViewModel() {
    private val state = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            val source = if (asAdmin) repository.observeOwnedBy(user.id) else repository.observeReceivedBy(user.id)
            source.collect { documents -> state.update { it.copy(documents = documents, isLoaded = true) } }
        }
        refresh()
    }

    fun refresh() {
        state.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            val result = runCatching {
                if (asAdmin) repository.refreshOwnedBy(user.id) else repository.refreshReceivedBy(user.id)
            }
            result.exceptionOrNull()?.let { Log.w(TAG, "Failed to sync documents", it) }
            state.update { it.copy(isRefreshing = false, isOffline = result.isFailure) }
        }
    }

    fun selectFilter(filter: AdminFilter) = state.update { it.copy(adminFilter = filter) }

    fun selectTab(tab: SignerTab) = state.update { it.copy(signerTab = tab) }

    fun pendingFor(documents: List<Document>) = documents.filter { it.signerForUser(user.id)?.status == SignerStatus.PENDING }

    fun signedFor(documents: List<Document>) = documents.filter { it.signerForUser(user.id)?.status == SignerStatus.SIGNED }

    companion object {
        private const val TAG = "LetsSign"

        fun factory(container: AppContainer, user: User, asAdmin: Boolean) = viewModelFactory {
            initializer { HomeViewModel(container.documentRepository, user, asAdmin) }
        }
    }
}
