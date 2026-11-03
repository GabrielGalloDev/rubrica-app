package br.com.fiap.letssign.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.letssign.R
import br.com.fiap.letssign.components.InlineErrorMessage
import br.com.fiap.letssign.components.InlineSuccessMessage
import br.com.fiap.letssign.components.LetsSignButton
import br.com.fiap.letssign.components.LetsSignPasswordField
import br.com.fiap.letssign.components.LetsSignTextButton
import br.com.fiap.letssign.components.LetsSignTextField
import br.com.fiap.letssign.components.LetsSignTopBar
import br.com.fiap.letssign.ui.theme.Dimens
import br.com.fiap.letssign.ui.theme.Spacing
import br.com.fiap.letssign.ui.viewmodel.SignerFormViewModel

@Composable
fun SignerFormScreen(viewModel: SignerFormViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSaved) { if (state.isSaved) onBack() }

    Scaffold(
        topBar = {
            LetsSignTopBar(
                title = stringResource(if (state.isEditing) R.string.signer_form_edit_title else R.string.signer_form_new_title),
                onNavigate = onBack,
            )
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.md + Spacing.xxs),
        ) {
            LetsSignTextField(
                label = stringResource(R.string.field_full_name),
                value = state.name,
                onValueChange = viewModel::onNameChange,
                icon = Icons.Default.Person,
                errorRes = state.nameError,
            )
            LetsSignTextField(
                label = stringResource(R.string.field_email_login),
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                icon = Icons.Default.Email,
                errorRes = state.emailError,
                enabled = !state.isEditing,
                keyboardType = KeyboardType.Email,
            )
            LetsSignTextField(
                label = stringResource(R.string.field_phone),
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                icon = Icons.Default.Phone,
                errorRes = state.phoneError,
                keyboardType = KeyboardType.Phone,
            )
            if (state.canChangePassword) {
                LetsSignPasswordField(
                    label = stringResource(if (state.isEditing) R.string.field_new_password_optional else R.string.field_password),
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    isVisible = state.isPasswordVisible,
                    onToggleVisibility = viewModel::togglePasswordVisibility,
                    errorRes = state.passwordError,
                )
                LetsSignPasswordField(
                    label = stringResource(R.string.field_confirm_password),
                    value = state.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    isVisible = state.isConfirmPasswordVisible,
                    onToggleVisibility = viewModel::toggleConfirmPasswordVisibility,
                    errorRes = state.confirmPasswordError,
                    imeAction = ImeAction.Done,
                )
            } else {
                Text(
                    stringResource(R.string.signer_form_password_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LetsSignTextButton(
                    text = stringResource(R.string.signer_form_send_reset),
                    onClick = viewModel::sendPasswordReset,
                    enabled = !state.isSubmitting,
                )
            }
            state.formError?.let { InlineErrorMessage(stringResource(it)) }
            state.resetSentTo?.let { InlineSuccessMessage(stringResource(R.string.signer_form_reset_sent, it)) }
            Spacer(Modifier.height(Spacing.xs))
            LetsSignButton(
                text = stringResource(if (state.isEditing) R.string.action_save_changes else R.string.signer_form_submit),
                onClick = viewModel::save,
                isLoading = state.isSubmitting,
            )
        }
    }
}
