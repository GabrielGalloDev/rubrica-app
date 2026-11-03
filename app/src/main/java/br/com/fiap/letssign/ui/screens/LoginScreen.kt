package br.com.fiap.letssign.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.letssign.R
import br.com.fiap.letssign.components.HeaderIconButton
import br.com.fiap.letssign.components.InlineErrorMessage
import br.com.fiap.letssign.components.InlineSuccessMessage
import br.com.fiap.letssign.components.LetsSignButton
import br.com.fiap.letssign.components.LetsSignOutlinedButton
import br.com.fiap.letssign.components.LetsSignPasswordField
import br.com.fiap.letssign.components.LetsSignTextButton
import br.com.fiap.letssign.components.LetsSignTextField
import br.com.fiap.letssign.components.LightStatusBarIcons
import br.com.fiap.letssign.model.User
import br.com.fiap.letssign.repository.AuthRepository
import br.com.fiap.letssign.ui.theme.AppShapes
import br.com.fiap.letssign.ui.theme.BrandGradient
import br.com.fiap.letssign.ui.theme.Dimens
import br.com.fiap.letssign.ui.theme.Spacing
import br.com.fiap.letssign.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoggedIn: (User) -> Unit,
    onNavigateToRegister: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val state by viewModel.login.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
    ) {
        BrandHeader(onOpenSettings)
        // Form card overlapping the header.
        Card(
            modifier = Modifier.padding(horizontal = Dimens.screenPadding).offset(y = (-36).dp),
            shape = AppShapes.sheet,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(Modifier.padding(Spacing.xl + Spacing.xxs), verticalArrangement = Arrangement.spacedBy(Spacing.md + Spacing.xxs)) {
                Text(stringResource(R.string.login_title), style = MaterialTheme.typography.headlineSmall)
                Text(
                    stringResource(R.string.login_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LetsSignTextField(
                    label = stringResource(R.string.field_email),
                    value = state.email,
                    onValueChange = viewModel::onLoginEmailChange,
                    icon = Icons.Default.Email,
                    errorRes = state.emailError,
                    enabled = !state.isSubmitting,
                    keyboardType = KeyboardType.Email,
                )
                LetsSignPasswordField(
                    label = stringResource(R.string.field_password),
                    value = state.password,
                    onValueChange = viewModel::onLoginPasswordChange,
                    isVisible = state.isPasswordVisible,
                    onToggleVisibility = viewModel::toggleLoginPasswordVisibility,
                    errorRes = state.passwordError,
                    enabled = !state.isSubmitting,
                    imeAction = ImeAction.Done,
                )
                if (state.isRemoteEnabled) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        LetsSignTextButton(
                            text = stringResource(R.string.login_forgot_password),
                            onClick = viewModel::sendPasswordReset,
                            enabled = !state.isSubmitting,
                        )
                    }
                }
                state.formError?.let { InlineErrorMessage(stringResource(it)) }
                state.successMessage?.let { InlineSuccessMessage(stringResource(it, state.email.trim())) }
                LetsSignButton(
                    text = stringResource(R.string.login_submit),
                    onClick = { viewModel.signIn(onLoggedIn) },
                    isLoading = state.isSubmitting,
                )
            }
        }
        Column(
            Modifier.fillMaxWidth().padding(horizontal = Dimens.screenPadding).offset(y = (-16).dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.md + Spacing.xxs),
        ) {
            AuthDivider(stringResource(R.string.login_no_account))
            LetsSignOutlinedButton(text = stringResource(R.string.login_create_admin), onClick = onNavigateToRegister)
            if (!state.isRemoteEnabled) {
                InlineSuccessMessage(
                    stringResource(R.string.login_demo_hint, AuthRepository.DEMO_ADMIN_EMAIL, AuthRepository.DEMO_ADMIN_PASSWORD)
                )
            }
        }
    }
}

@Composable
private fun BrandHeader(onOpenSettings: () -> Unit) {
    LightStatusBarIcons()
    Box(
        Modifier
            .fillMaxWidth()
            .clip(AppShapes.headerLarge)
            .background(BrandGradient)
            .drawBehind {
                drawCircle(Color.White.copy(alpha = 0.08f), radius = size.width * 0.5f, center = Offset(size.width, size.height * 0.15f))
                drawCircle(Color.White.copy(alpha = 0.05f), radius = size.width * 0.35f, center = Offset(0f, size.height))
            }
            .statusBarsPadding()
            .padding(start = Spacing.xxl, end = Spacing.xxl, top = Spacing.xxxl, bottom = 72.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(AppShapes.message).background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Draw,
                        contentDescription = stringResource(R.string.cd_app_logo),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Spacer(Modifier.width(Spacing.md))
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge, color = Color.White, modifier = Modifier.weight(1f))
                HeaderIconButton(Icons.Default.Settings, stringResource(R.string.cd_settings), onOpenSettings)
            }
            Spacer(Modifier.height(Spacing.xxxl))
            Text(stringResource(R.string.login_hero_title), style = MaterialTheme.typography.displaySmall, color = Color.White)
            Spacer(Modifier.height(Spacing.sm + Spacing.xxs))
            Text(
                stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
internal fun AuthDivider(text: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(Modifier.weight(1f), thickness = Dimens.dividerThickness, color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Spacing.md),
        )
        HorizontalDivider(Modifier.weight(1f), thickness = Dimens.dividerThickness, color = MaterialTheme.colorScheme.outlineVariant)
    }
}
