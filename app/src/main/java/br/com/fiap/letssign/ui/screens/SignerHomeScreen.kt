package br.com.fiap.letssign.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.letssign.R
import br.com.fiap.letssign.components.DocumentCard
import br.com.fiap.letssign.components.EmptyState
import br.com.fiap.letssign.components.HomeHeader
import br.com.fiap.letssign.components.Pill
import br.com.fiap.letssign.components.SectionHeader
import br.com.fiap.letssign.components.SegmentPill
import br.com.fiap.letssign.components.StatTile
import br.com.fiap.letssign.components.SyncWarning
import br.com.fiap.letssign.model.SignerStatus
import br.com.fiap.letssign.model.User
import br.com.fiap.letssign.ui.theme.Amber
import br.com.fiap.letssign.ui.theme.Dimens
import br.com.fiap.letssign.ui.theme.Emerald
import br.com.fiap.letssign.ui.theme.Spacing
import br.com.fiap.letssign.ui.viewmodel.HomeViewModel
import br.com.fiap.letssign.ui.viewmodel.SignerTab
import br.com.fiap.letssign.utils.formatDate

@Composable
fun SignerHomeScreen(
    signer: User,
    viewModel: HomeViewModel,
    onOpenDocument: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pending = viewModel.pendingFor(state.documents)
    val signed = viewModel.signedFor(state.documents)
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = bottomInset + Spacing.xxxl),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        item {
            HomeHeader(
                name = signer.name,
                subtitle = stringResource(R.string.home_signer_subtitle),
                refreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                onOpenSettings = onOpenSettings,
                onLogout = onLogout,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm + Spacing.xxs)) {
                    StatTile(stringResource(R.string.home_stat_pending), pending.size, Icons.Default.HourglassTop, Modifier.weight(1f))
                    StatTile(stringResource(R.string.home_stat_signed), signed.size, Icons.Default.TaskAlt, Modifier.weight(1f))
                }
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
            Row(Modifier.padding(horizontal = Dimens.screenPadding), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                SegmentPill(
                    stringResource(R.string.home_filter_count, stringResource(R.string.home_tab_pending), pending.size),
                    selected = state.signerTab == SignerTab.PENDING,
                    onClick = { viewModel.selectTab(SignerTab.PENDING) },
                )
                SegmentPill(
                    stringResource(R.string.home_filter_count, stringResource(R.string.home_tab_signed), signed.size),
                    selected = state.signerTab == SignerTab.SIGNED,
                    onClick = { viewModel.selectTab(SignerTab.SIGNED) },
                )
            }
        }
        val shown = if (state.signerTab == SignerTab.PENDING) pending else signed
        if (state.isLoaded && shown.isEmpty()) {
            item {
                if (state.signerTab == SignerTab.PENDING) {
                    EmptyState(Icons.Default.Inbox, stringResource(R.string.home_signer_empty_pending_title), stringResource(R.string.home_signer_empty_pending_message))
                } else {
                    EmptyState(Icons.Default.TaskAlt, stringResource(R.string.home_signer_empty_signed_title), stringResource(R.string.home_signer_empty_signed_message))
                }
            }
        }
        items(shown, key = { it.id }) { doc ->
            val me = doc.signerForUser(signer.id)
            DocumentCard(
                doc,
                subtitle = stringResource(R.string.home_document_from, doc.ownerName, doc.sentAt?.formatDate().orEmpty()),
                onClick = { onOpenDocument(doc.id) },
                modifier = Modifier.padding(horizontal = Dimens.screenPadding),
                badge = me?.let {
                    {
                        if (it.status == SignerStatus.SIGNED) Pill(stringResource(R.string.home_badge_you_signed), Emerald)
                        else Pill(stringResource(R.string.home_badge_waiting_you), Amber)
                    }
                },
            )
        }
    }
}
