package br.com.fiap.letssign.ui.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.fiap.letssign.R
import br.com.fiap.letssign.factory.AppContainer
import br.com.fiap.letssign.model.User
import br.com.fiap.letssign.repository.AuthRepository
import br.com.fiap.letssign.utils.Validators
import br.com.fiap.letssign.utils.toMessageRes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    @param:StringRes val emailError: Int? = null,
    @param:StringRes val passwordError: Int? = null,
    @param:StringRes val formError: Int? = null,
    @param:StringRes val successMessage: Int? = null,
    val isSubmitting: Boolean = false,
    val isRemoteEnabled: Boolean = false,
)

data class RegisterUiState(
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
    val isSubmitting: Boolean = false,
)

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val loginState = MutableStateFlow(LoginUiState(isRemoteEnabled = authRepository.isRemoteEnabled))
    val login: StateFlow<LoginUiState> = loginState.asStateFlow()

    private val registerState = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = registerState.asStateFlow()

    // ---------------- Login ----------------

    fun onLoginEmailChange(value: String) = loginState.update {
        it.copy(email = value, emailError = null, formError = null, successMessage = null)
    }

    fun onLoginPasswordChange(value: String) = loginState.update {
        it.copy(password = value, passwordError = null, formError = null)
    }

    fun toggleLoginPasswordVisibility() = loginState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun signIn(onSuccess: (User) -> Unit) {
        val current = loginState.value
        val emailError = Validators.validateEmail(current.email)
        val passwordError = Validators.validateRequired(current.password, R.string.error_password_required)
        if (emailError != null || passwordError != null) {
            loginState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        loginState.update { it.copy(isSubmitting = true, formError = null, successMessage = null) }
        viewModelScope.launch {
            authRepository.login(current.email, current.password)
                .onSuccess { user ->
                    loginState.value = LoginUiState(isRemoteEnabled = current.isRemoteEnabled)
                    onSuccess(user)
                }
                .onFailure { throwable ->
                    loginState.update { it.copy(isSubmitting = false, formError = throwable.toMessageRes()) }
                }
        }
    }

    fun sendPasswordReset() {
        val current = loginState.value
        val emailError = Validators.validateEmail(current.email)
        if (emailError != null) {
            loginState.update { it.copy(emailError = R.string.error_reset_email_required) }
            return
        }

        loginState.update { it.copy(isSubmitting = true, formError = null, successMessage = null) }
        viewModelScope.launch {
            authRepository.sendPasswordReset(current.email)
                .onSuccess { loginState.update { it.copy(isSubmitting = false, successMessage = R.string.login_reset_sent) } }
                .onFailure { throwable ->
                    loginState.update { it.copy(isSubmitting = false, formError = throwable.toMessageRes()) }
                }
        }
    }

    // ---------------- Admin registration ----------------

    fun onRegisterNameChange(value: String) = registerState.update { it.copy(name = value, nameError = null, formError = null) }

    fun onRegisterEmailChange(value: String) = registerState.update { it.copy(email = value, emailError = null, formError = null) }

    fun onRegisterPhoneChange(value: String) = registerState.update { it.copy(phone = value, phoneError = null, formError = null) }

    fun onRegisterPasswordChange(value: String) = registerState.update {
        it.copy(password = value, passwordError = null, formError = null)
    }

    fun onRegisterConfirmPasswordChange(value: String) = registerState.update {
        it.copy(confirmPassword = value, confirmPasswordError = null, formError = null)
    }

    fun toggleRegisterPasswordVisibility() = registerState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun toggleRegisterConfirmPasswordVisibility() = registerState.update {
        it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible)
    }

    fun signUp(onSuccess: (User) -> Unit) {
        val current = registerState.value
        val nameError = Validators.validateName(current.name)
        val emailError = Validators.validateEmail(current.email)
        val phoneError = Validators.validatePhone(current.phone)
        val passwordError = Validators.validatePassword(current.password)
        val confirmError = Validators.validatePasswordConfirmation(current.password, current.confirmPassword)
            ?: if (current.confirmPassword.isEmpty()) R.string.error_password_required else null

        if (listOfNotNull(nameError, emailError, phoneError, passwordError, confirmError).isNotEmpty()) {
            registerState.update {
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

        registerState.update { it.copy(isSubmitting = true, formError = null) }
        viewModelScope.launch {
            authRepository.registerAdmin(current.name, current.email, current.phone, current.password)
                .onSuccess { user ->
                    registerState.value = RegisterUiState()
                    onSuccess(user)
                }
                .onFailure { throwable ->
                    registerState.update { it.copy(isSubmitting = false, formError = throwable.toMessageRes()) }
                }
        }
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { AuthViewModel(container.authRepository) }
        }
    }
}
