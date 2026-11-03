package br.com.fiap.letssign.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.letssign.R
import br.com.fiap.letssign.components.ActionCard
import br.com.fiap.letssign.components.DocumentCard
import br.com.fiap.letssign.components.EmptyState
import br.com.fiap.letssign.components.HomeHeader
import br.com.fiap.letssign.components.SectionHeader
import br.com.fiap.letssign.components.SegmentPill
import br.com.fiap.letssign.components.StatTile
import br.com.fiap.letssign.components.SyncWarning
import br.com.fiap.letssign.model.DocumentStatus
import br.com.fiap.letssign.model.User
import br.com.fiap.letssign.ui.theme.Dimens
import br.com.fiap.letssign.ui.theme.Spacing
import br.com.fiap.letssign.ui.viewmodel.AdminFilter
import br.com.fiap.letssign.ui.viewmodel.HomeViewModel
import br.com.fiap.letssign.utils.formatDate

@Composable
fun AdminHomeScreen(
    admin: User,
    viewModel: HomeViewModel,
    onNewDocument: () -> Unit,
    onSigners: () -> Unit,
    onOpenDocument: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = bottomInset + Spacing.xxxl),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        item {
            HomeHeader(
                name = admin.name,
                subtitle = stringResource(R.string.home_admin_subtitle),
                refreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                onOpenSettings = onOpenSettings,
                onLogout = onLogout,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm + Spacing.xxs)) {
                    StatTile(stringResource(R.string.home_stat_sent), state.count(DocumentStatus.SENT), Icons.AutoMirrored.Filled.Send, Modifier.weight(1f))
                    StatTile(stringResource(R.string.home_stat_partial), state.count(DocumentStatus.PARTIALLY_SIGNED), Icons.Default.HourglassTop, Modifier.weight(1f))
                    StatTile(stringResource(R.string.home_stat_completed), state.count(DocumentStatus.COMPLETED), Icons.Default.TaskAlt, Modifier.weight(1f))
                }
            }
        }
        item {
            Row(
                Modifier.padding(horizontal = Dimens.screenPadding).padding(top = Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                ActionCard(
                    stringResource(R.string.home_action_new_document), stringResource(R.string.home_action_new_document_subtitle),
                    Icons.AutoMirrored.Filled.NoteAdd, onNewDocument, Modifier.weight(1f), highlighted = true,
                )
                ActionCard(
                    stringResource(R.string.home_action_signers), stringResource(R.string.home_action_signers_subtitle),
                    Icons.Default.Group, onSigners, Modifier.weight(1f),
                )
            }
        }
        if (state.isOffline) item { SyncWarning(Modifier.padding(horizontal = Dimens.screenPadding)) }
        item {
            SectionHeader(
                stringResource(R.string.home_documents),
                pluralStringResource(R.plurals.home_documents_total, state.documents.size, state.documents.size),
                Modifier.padding(horizontal = Dimens.screenPadding),
            )
        }
        item {
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = Dimens.screenPadding),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                AdminFilter.entries.forEach { filter ->
                    SegmentPill(
                        stringResource(R.string.home_filter_count, stringResource(filter.labelRes), state.count(filter)),
                        selected = state.adminFilter == filter,
                        onClick = { viewModel.selectFilter(filter) },
                    )
                }
            }
        }
        val filtered = state.filteredDocuments
        if (state.isLoaded && filtered.isEmpty()) {
            item {
                EmptyState(
                    Icons.Default.Description,
                    stringResource(R.string.home_admin_empty_title),
                    stringResource(R.string.home_admin_empty_message),
                )
            }
        }
        items(filtered, key = { it.id }) { doc ->
            DocumentCard(
                doc,
                subtitle = stringResource(R.string.home_document_created, doc.version, doc.createdAt.formatDate()),
                onClick = { onOpenDocument(doc.id) },
                modifier = Modifier.padding(horizontal = Dimens.screenPadding),
            )
        }
    }
}

@get:StringRes
private val AdminFilter.labelRes: Int
    get() = when (this) {
        AdminFilter.ALL -> R.string.home_filter_all
        AdminFilter.DRAFT -> R.string.home_filter_drafts
        AdminFilter.SENT -> R.string.home_filter_sent
        AdminFilter.PARTIAL -> R.string.home_filter_partial
        AdminFilter.COMPLETED -> R.string.home_filter_completed
    }
