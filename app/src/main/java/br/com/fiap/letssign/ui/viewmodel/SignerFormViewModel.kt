package br.com.fiap.letssign.ui.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.fiap.letssign.factory.AppContainer
import br.com.fiap.letssign.model.User
import br.com.fiap.letssign.repository.SignerRepository
import br.com.fiap.letssign.utils.Validators
import br.com.fiap.letssign.utils.toMessageRes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignerFormUiState(
    val isEditing: Boolean = false,
    val canChangePassword: Boolean = true,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    @param:StringRes val nameError: Int? = null,
    @param:StringRes val emailError: Int? = null,
    @param:StringRes val phoneError: Int? = null,
    @param:StringRes val passwordError: Int? = null,
    @param:StringRes val confirmPasswordError: Int? = null,
    @param:StringRes val formError: Int? = null,
    val resetSentTo: String? = null,
    val isSubmitting: Boolean = false,
    val isSaved: Boolean = false,
)

class SignerFormViewModel(
    private val repository: SignerRepository,
    private val admin: User,
    private val signerId: String?,
) : ViewModel() {
    private val state = MutableStateFlow(
        SignerFormUiState(
            isEditing = signerId != null,
            // When editing in Firebase mode the password is changed through a reset link.
            canChangePassword = signerId == null || repository.canChangePassword,
        )
    )
    val uiState: StateFlow<SignerFormUiState> = state.asStateFlow()

    private var original: User? = null

    init {
        if (signerId != null) viewModelScope.launch {
            repository.find(signerId)?.let { user ->
                original = user
                state.update { it.copy(name = user.name, email = user.email, phone = user.phone) }
            }
        }
    }

    fun onNameChange(value: String) = state.update { it.copy(name = value, nameError = null, formError = null) }

    fun onEmailChange(value: String) = state.update { it.copy(email = value, emailError = null, formError = null) }

    fun onPhoneChange(value: String) = state.update { it.copy(phone = value, phoneError = null, formError = null) }

    fun onPasswordChange(value: String) = state.update { it.copy(password = value, passwordError = null, formError = null) }

    fun onConfirmPasswordChange(value: String) = state.update {
        it.copy(confirmPassword = value, confirmPasswordError = null, formError = null)
    }

    fun togglePasswordVisibility() = state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun toggleConfirmPasswordVisibility() = state.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }

    fun save() {
        val current = state.value
        val nameError = Validators.validateName(current.name)
        val emailError = if (current.isEditing) null else Validators.validateEmail(current.email)
        val phoneError = Validators.validatePhone(current.phone)
        val passwordError = when {
            !current.canChangePassword -> null
            current.isEditing -> Validators.validateOptionalPassword(current.password)
            else -> Validators.validatePassword(current.password)
        }
        val confirmError = if (current.canChangePassword) {
            Validators.validatePasswordConfirmation(current.password, current.confirmPassword)
        } else {
            null
        }

        if (listOfNotNull(nameError, emailError, phoneError, passwordError, confirmError).isNotEmpty()) {
            state.update {
                it.copy(
                    nameError = nameError,
                    emailError = emailError,
                    phoneError = phoneError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError,
                )
            }
            return
        }

        state.update { it.copy(isSubmitting = true, formError = null) }
        viewModelScope.launch {
            val existing = original
            val result = if (existing != null) {
                repository.update(existing.copy(name = current.name, phone = current.phone), current.password.ifBlank { null })
            } else {
                repository.create(admin, current.name, current.email, current.phone, current.password)
            }
            result
                .onSuccess { state.update { it.copy(isSubmitting = false, isSaved = true) } }
                .onFailure { throwable -> state.update { it.copy(isSubmitting = false, formError = throwable.toMessageRes()) } }
        }
    }

    fun sendPasswordReset() {
        val email = state.value.email
        state.update { it.copy(isSubmitting = true, formError = null, resetSentTo = null) }
        viewModelScope.launch {
            repository.sendPasswordReset(email)
                .onSuccess { state.update { it.copy(isSubmitting = false, resetSentTo = email) } }
                .onFailure { throwable ->
                    state.update { it.copy(isSubmitting = false, formError = throwable.toMessageRes()) }
                }
        }
    }

    companion object {
        fun factory(container: AppContainer, admin: User, signerId: String?) = viewModelFactory {
            initializer { SignerFormViewModel(container.signerRepository, admin, signerId) }
        }
    }
}
