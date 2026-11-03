package br.com.fiap.letssign.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.letssign.R
import br.com.fiap.letssign.components.ErrorMessage
import br.com.fiap.letssign.components.FieldBox
import br.com.fiap.letssign.components.InlineErrorMessage
import br.com.fiap.letssign.components.LetsSignButton
import br.com.fiap.letssign.components.LetsSignOutlinedButton
import br.com.fiap.letssign.components.LetsSignTopBar
import br.com.fiap.letssign.components.LoadingIndicator
import br.com.fiap.letssign.components.PdfPage
import br.com.fiap.letssign.components.SignaturePadDialog
import br.com.fiap.letssign.components.StatusChip
import br.com.fiap.letssign.components.rememberPdfRenderer
import br.com.fiap.letssign.model.Document
import br.com.fiap.letssign.model.DocumentSigner
import br.com.fiap.letssign.model.DocumentStatus
import br.com.fiap.letssign.model.SignerStatus
import br.com.fiap.letssign.ui.theme.AppShapes
import br.com.fiap.letssign.ui.theme.Dimens
import br.com.fiap.letssign.ui.theme.Emerald
import br.com.fiap.letssign.ui.theme.Indigo
import br.com.fiap.letssign.ui.theme.Slate
import br.com.fiap.letssign.ui.theme.Spacing
import br.com.fiap.letssign.ui.viewmodel.SignDocumentUiState
import br.com.fiap.letssign.ui.viewmodel.SignDocumentViewModel
import br.com.fiap.letssign.utils.formatDateTime
import br.com.fiap.letssign.utils.labelRes

@Composable
fun SignDocumentScreen(
    viewModel: SignDocumentViewModel,
    signerId: String,
    onBack: () -> Unit,
    onOpenSigned: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val renderer = rememberPdfRenderer(state.pdf)
    val doc = state.document
    val me = doc?.signerForUser(signerId)

    Scaffold(
        topBar = { LetsSignTopBar(title = doc?.title ?: stringResource(R.string.viewer_default_title), onNavigate = onBack) },
        bottomBar = {
            if (doc != null && me != null && state.loadError == null) {
                SignBottomBar(doc, me, state, viewModel, onOpenSigned)
            }
        },
    ) { padding ->
        when {
            state.loadError != null -> ErrorMessage(stringResource(state.loadError!!), Modifier.padding(padding))
            doc == null || me == null || renderer == null -> LoadingIndicator(Modifier.padding(padding))
            else -> LazyColumn(
                Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                contentPadding = PaddingValues(Dimens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                item { SignInfoCard(doc, me) }
                items(renderer.pageCount) { index ->
                    PdfPage(renderer, index, Modifier.fillMaxWidth()) { w, h ->
                        doc.fields.filter { it.page == index }.forEach { field ->
                            val mine = field.signerId == me.id
                            val owner = doc.signers.firstOrNull { it.id == field.signerId }
                            val ownerSigned = owner?.status == SignerStatus.SIGNED
                            FieldBox(
                                field, w, h,
                                color = if (mine) Indigo else if (ownerSigned) Emerald else Slate,
                                label = if (mine) {
                                    stringResource(R.string.sign_here)
                                } else {
                                    (owner?.name?.substringBefore(" ").orEmpty()) + if (ownerSigned) " ✓" else ""
                                },
                                signature = if (mine) state.captured?.bitmap ?: state.savedSignature else null,
                                highlighted = mine,
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.isPadVisible && me != null) {
        SignaturePadDialog(me.name, onDismiss = viewModel::dismissPad, onConfirm = viewModel::onSignatureCaptured)
    }

    state.justSigned?.let { signed ->
        AlertDialog(
            onDismissRequest = viewModel::dismissSignedDialog,
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald) },
            title = { Text(stringResource(R.string.sign_done_title)) },
            text = {
                Text(
                    stringResource(
                        if (signed.status == DocumentStatus.COMPLETED) R.string.sign_done_completed else R.string.sign_done_partial
                    )
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissSignedDialog(); onBack() }) { Text(stringResource(R.string.sign_done_back)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSignedDialog(); onOpenSigned(signed.id) }) {
                    Text(stringResource(R.string.sign_done_view_pdf))
                }
            },
        )
    }
}

@Composable
private fun SignInfoCard(doc: Document, me: DocumentSigner) {
    val alreadySigned = me.status == SignerStatus.SIGNED
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Dimens.cardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusChip(doc.status)
                Spacer(Modifier.weight(1f))
                Text(stringResource(me.role.labelRes), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(Spacing.sm))
            Text(stringResource(R.string.sign_sent_by, doc.ownerName), fontWeight = FontWeight.SemiBold)
            doc.sentAt?.let {
                Text(it.formatDateTime(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (doc.message.isNotBlank()) {
                Spacer(Modifier.height(Spacing.sm + Spacing.xxs))
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = AppShapes.message) {
                    Text(
                        stringResource(R.string.sign_message_quote, doc.message),
                        Modifier.padding(Spacing.md),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Spacer(Modifier.height(Spacing.sm + Spacing.xxs))
            val myFields = doc.fields.filter { it.signerId == me.id }
            Text(
                if (alreadySigned) {
                    stringResource(R.string.sign_you_signed_at, me.signedAt?.formatDateTime().orEmpty())
                } else {
                    pluralStringResource(
                        R.plurals.sign_fields_to_sign, myFields.size, myFields.size,
                        myFields.map { it.page + 1 }.distinct().sorted().joinToString(),
                    )
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (alreadySigned) Emerald else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun SignBottomBar(
    doc: Document,
    me: DocumentSigner,
    state: SignDocumentUiState,
    viewModel: SignDocumentViewModel,
    onOpenSigned: (String) -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest, shape = AppShapes.bottomBar, shadowElevation = 12.dp) {
        Column(Modifier.navigationBarsPadding().padding(horizontal = Dimens.screenPadding, vertical = Spacing.lg)) {
            state.saveError?.let {
                InlineErrorMessage(stringResource(it))
                Spacer(Modifier.height(Spacing.sm))
            }
            when {
                me.status == SignerStatus.SIGNED -> LetsSignButton(
                    stringResource(if (doc.status == DocumentStatus.COMPLETED) R.string.sign_view_final else R.string.sign_view_signed),
                    onClick = { onOpenSigned(doc.id) },
                    icon = Icons.Default.PictureAsPdf,
                )
                state.captured == null -> LetsSignButton(
                    stringResource(R.string.sign_action),
                    onClick = viewModel::openPad,
                    icon = Icons.Default.Draw,
                )
                else -> Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm + Spacing.xxs)) {
                    LetsSignOutlinedButton(
                        stringResource(R.string.sign_redo),
                        onClick = viewModel::openPad,
                        enabled = !state.isSaving,
                        modifier = Modifier.weight(1f),
                    )
                    LetsSignButton(
                        stringResource(R.string.sign_save),
                        onClick = viewModel::save,
                        isLoading = state.isSaving,
                        icon = Icons.Default.Save,
                        modifier = Modifier.weight(2f),
                    )
                }
            }
        }
    }
}
