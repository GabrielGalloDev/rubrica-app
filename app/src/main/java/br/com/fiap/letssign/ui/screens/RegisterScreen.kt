package br.com.fiap.letssign.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.letssign.R
import br.com.fiap.letssign.components.InlineErrorMessage
import br.com.fiap.letssign.components.LetsSignButton
import br.com.fiap.letssign.components.LetsSignPasswordField
import br.com.fiap.letssign.components.LetsSignTextField
import br.com.fiap.letssign.model.User
import br.com.fiap.letssign.ui.theme.Dimens
import br.com.fiap.letssign.ui.theme.Spacing
import br.com.fiap.letssign.ui.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onRegistered: (User) -> Unit,
) {
    val state by viewModel.register.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = Dimens.screenPadding, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md + Spacing.xxs),
    ) {
        Box(
            Modifier.size(Dimens.iconContainer).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerLowest),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_back)) }
        }
        Spacer(Modifier.height(Spacing.xs))
        Text(stringResource(R.string.register_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(R.string.register_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Spacing.xs))
        LetsSignTextField(
            label = stringResource(R.string.field_full_name),
            value = state.name,
            onValueChange = viewModel::onRegisterNameChange,
            icon = Icons.Default.Person,
            errorRes = state.nameError,
            enabled = !state.isSubmitting,
        )
        LetsSignTextField(
            label = stringResource(R.string.field_email),
            value = state.email,
            onValueChange = viewModel::onRegisterEmailChange,
            icon = Icons.Default.Email,
            errorRes = state.emailError,
            enabled = !state.isSubmitting,
            keyboardType = KeyboardType.Email,
        )
        LetsSignTextField(
            label = stringResource(R.string.field_phone),
            value = state.phone,
            onValueChange = viewModel::onRegisterPhoneChange,
            icon = Icons.Default.Phone,
            errorRes = state.phoneError,
            enabled = !state.isSubmitting,
            keyboardType = KeyboardType.Phone,
        )
        LetsSignPasswordField(
            label = stringResource(R.string.field_password),
            value = state.password,
            onValueChange = viewModel::onRegisterPasswordChange,
            isVisible = state.isPasswordVisible,
            onToggleVisibility = viewModel::toggleRegisterPasswordVisibility,
            errorRes = state.passwordError,
            enabled = !state.isSubmitting,
        )
        LetsSignPasswordField(
            label = stringResource(R.string.field_confirm_password),
            value = state.confirmPassword,
            onValueChange = viewModel::onRegisterConfirmPasswordChange,
            isVisible = state.isConfirmPasswordVisible,
            onToggleVisibility = viewModel::toggleRegisterConfirmPasswordVisibility,
            errorRes = state.confirmPasswordError,
            enabled = !state.isSubmitting,
            imeAction = ImeAction.Done,
        )
        state.formError?.let { InlineErrorMessage(stringResource(it)) }
        LetsSignButton(
            text = stringResource(R.string.register_submit),
            onClick = { viewModel.signUp(onRegistered) },
            isLoading = state.isSubmitting,
        )
    }
}
