package br.com.fiap.letssign.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.letssign.R
import br.com.fiap.letssign.components.ConfirmationDialog
import br.com.fiap.letssign.components.EmptyState
import br.com.fiap.letssign.components.InlineErrorMessage
import br.com.fiap.letssign.components.LetsSignButton
import br.com.fiap.letssign.components.LetsSignOutlinedButton
import br.com.fiap.letssign.components.LetsSignTonalButton
import br.com.fiap.letssign.components.LetsSignTopBar
import br.com.fiap.letssign.components.LoadingIndicator
import br.com.fiap.letssign.components.Pill
import br.com.fiap.letssign.components.SectionTitle
import br.com.fiap.letssign.components.StatusChip
import br.com.fiap.letssign.components.UserAvatar
import br.com.fiap.letssign.model.Document
import br.com.fiap.letssign.model.DocumentEvent
import br.com.fiap.letssign.model.DocumentSigner
import br.com.fiap.letssign.model.DocumentStatus
import br.com.fiap.letssign.model.SignerStatus
import br.com.fiap.letssign.ui.theme.Amber
import br.com.fiap.letssign.ui.theme.Dimens
import br.com.fiap.letssign.ui.theme.Emerald
import br.com.fiap.letssign.ui.theme.Slate
import br.com.fiap.letssign.ui.theme.Spacing
import br.com.fiap.letssign.ui.theme.eventColor
import br.com.fiap.letssign.ui.viewmodel.DocumentDetailViewModel
import br.com.fiap.letssign.utils.detailRes
import br.com.fiap.letssign.utils.formatDateTime
import br.com.fiap.letssign.utils.labelRes
import br.com.fiap.letssign.utils.titleRes

@Composable
fun DocumentDetailScreen(
    viewModel: DocumentDetailViewModel,
    onBack: () -> Unit,
    onEditDraft: (String) -> Unit,
    onOpenViewer: (documentId: String, signed: Boolean) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            LetsSignTopBar(
                title = stringResource(R.string.detail_title),
                onNavigate = onBack,
                actions = {
                    IconButton(onClick = viewModel::refresh) { Icon(Icons.Default.Refresh, stringResource(R.string.cd_refresh)) }
                },
            )
        },
    ) { padding ->
        val doc = state.document
        when {
            doc == null && !state.isLoaded -> LoadingIndicator(Modifier.padding(padding))
            doc == null -> EmptyState(
                Icons.Default.PictureAsPdf,
                stringResource(R.string.detail_unavailable_title),
                stringResource(R.string.detail_unavailable_message),
                Modifier.padding(padding),
            )
            else -> LazyColumn(
                Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(Dimens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                item { SummaryCard(doc) }
                item {
                    Actions(
                        doc, state.isBusy,
                        onEdit = { onEditDraft(doc.id) },
                        onDelete = viewModel::openDeleteDialog,
                        onView = { onOpenViewer(doc.id, doc.status != DocumentStatus.DRAFT) },
                        onNewVersion = { viewModel.createNewVersion(onEditDraft) },
                    )
                }
                state.error?.let { item { InlineErrorMessage(stringResource(it)) } }
                item { SectionTitle(stringResource(R.string.detail_section_signers)) }
                items(doc.signers, key = { it.id }) { signer -> SignerStatusCard(doc, signer) }
                if (doc.message.isNotBlank()) {
                    item { SectionTitle(stringResource(R.string.detail_section_message)) }
                    item { Card(Modifier.fillMaxWidth()) { Text(doc.message, Modifier.padding(Dimens.cardPadding)) } }
                }
                item { SectionTitle(stringResource(R.string.detail_section_history)) }
                item { Timeline(doc.events) }
            }
        }
    }

    if (state.isDeleteDialogVisible) {
        ConfirmationDialog(
            title = stringResource(R.string.detail_delete_title),
            message = stringResource(R.string.detail_delete_message),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = { viewModel.deleteDraft(onBack) },
            onDismiss = viewModel::dismissDeleteDialog,
        )
    }
}

@Composable
private fun SummaryCard(doc: Document) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Dimens.cardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusChip(doc.status)
                Spacer(Modifier.weight(1f))
                Text(
                    stringResource(R.string.detail_version, doc.version),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(Spacing.sm + Spacing.xxs))
            Text(doc.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "${doc.fileName} • ${pluralStringResource(R.plurals.pages_count, doc.pageCount, doc.pageCount)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (doc.status != DocumentStatus.DRAFT && doc.signers.isNotEmpty()) {
                Spacer(Modifier.height(Spacing.md + Spacing.xxs))
                Text(
                    stringResource(R.string.detail_signatures_done, doc.signedCount, doc.signers.size),
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(Modifier.height(Spacing.xs + Spacing.xxs))
                LinearProgressIndicator(
                    progress = { doc.signedCount.toFloat() / doc.signers.size },
                    modifier = Modifier.fillMaxWidth().height(Dimens.progressHeightLarge),
                    strokeCap = StrokeCap.Round,
                )
            }
            Spacer(Modifier.height(Spacing.md + Spacing.xxs))
            DateRow(stringResource(R.string.detail_created_at), doc.createdAt)
            doc.sentAt?.let { DateRow(stringResource(R.string.detail_sent_at), it) }
            doc.completedAt?.let { DateRow(stringResource(R.string.detail_completed_at), it) }
        }
    }
}

@Composable
private fun DateRow(label: String, time: Long) {
    Row(Modifier.fillMaxWidth().padding(vertical = Spacing.xxs)) {
        Text(label, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(time.formatDateTime(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun Actions(
    doc: Document,
    busy: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onView: () -> Unit,
    onNewVersion: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        if (doc.status == DocumentStatus.DRAFT) {
            LetsSignButton(stringResource(R.string.detail_continue_editing), onEdit, icon = Icons.Default.Edit)
            LetsSignOutlinedButton(stringResource(R.string.detail_delete_draft), onDelete, icon = Icons.Default.Delete)
        } else {
            val completed = doc.status == DocumentStatus.COMPLETED
            LetsSignButton(
                stringResource(if (completed) R.string.detail_final_document else R.string.detail_view_signed),
                onView,
                icon = if (completed) Icons.Default.PictureAsPdf else Icons.Default.Visibility,
            )
            LetsSignTonalButton(stringResource(R.string.detail_new_version), onNewVersion, enabled = !busy, icon = Icons.Default.ContentCopy)
            Text(
                stringResource(R.string.detail_new_version_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SignerStatusCard(doc: Document, signer: DocumentSigner) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(Dimens.cardPadding), verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(signer.name)
            Spacer(Modifier.size(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(signer.name, fontWeight = FontWeight.SemiBold)
                Text(signerSummary(signer, doc.fields.count { it.signerId == signer.id }),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                signer.signedAt?.let {
                    Text(stringResource(R.string.detail_signed_at, it.formatDateTime()), style = MaterialTheme.typography.bodySmall, color = Emerald)
                }
            }
            when {
                doc.status == DocumentStatus.DRAFT -> Pill(stringResource(R.string.status_draft), Slate)
                signer.status == SignerStatus.SIGNED -> Pill(stringResource(signer.status.labelRes), Emerald)
                else -> Pill(stringResource(signer.status.labelRes), Amber)
            }
        }
    }
}

/** "Contractor • optional • 2 fields" */
@Composable
internal fun signerSummary(signer: DocumentSigner, fieldCount: Int): String = listOfNotNull(
    stringResource(signer.role.labelRes),
    if (signer.required) null else stringResource(R.string.signer_optional),
    pluralStringResource(R.plurals.fields_count, fieldCount, fieldCount),
).joinToString(" • ")

@Composable
private fun Timeline(events: List<DocumentEvent>) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Dimens.cardPadding)) {
            events.forEachIndexed { index, event ->
                Row {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(12.dp).background(eventColor(event.type), CircleShape))
                        if (index < events.lastIndex) {
                            Box(Modifier.width(2.dp).height(44.dp).background(MaterialTheme.colorScheme.outlineVariant))
                        }
                    }
                    Spacer(Modifier.width(Spacing.md))
                    Column(Modifier.padding(bottom = Spacing.md)) {
                        Text(stringResource(event.type.titleRes), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            stringResource(event.type.detailRes, event.actorName),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            event.timestamp.formatDateTime(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
