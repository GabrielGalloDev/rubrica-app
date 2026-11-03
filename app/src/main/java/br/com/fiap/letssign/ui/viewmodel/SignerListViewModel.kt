package br.com.fiap.letssign.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.fiap.letssign.factory.AppContainer
import br.com.fiap.letssign.model.User
import br.com.fiap.letssign.repository.SignerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SignerListViewModel(private val repository: SignerRepository, private val admin: User) : ViewModel() {
    val signers: StateFlow<List<User>> = repository.observeSigners(admin.id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { runCatching { repository.refresh(admin.id) } }
    }

    companion object {
        fun factory(container: AppContainer, admin: User) = viewModelFactory {
            initializer { SignerListViewModel(container.signerRepository, admin) }
        }
    }
}
