package br.com.fiap.letssign.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.letssign.R
import br.com.fiap.letssign.components.EmptyState
import br.com.fiap.letssign.components.LetsSignTopBar
import br.com.fiap.letssign.components.UserAvatar
import br.com.fiap.letssign.model.User
import br.com.fiap.letssign.ui.theme.Dimens
import br.com.fiap.letssign.ui.theme.LetsSignTheme
import br.com.fiap.letssign.ui.theme.Spacing
import br.com.fiap.letssign.ui.theme.signerColor
import br.com.fiap.letssign.ui.viewmodel.SignerListViewModel

@Composable
fun SignerListScreen(
    viewModel: SignerListViewModel,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
) {
    val signers by viewModel.signers.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { LetsSignTopBar(title = stringResource(R.string.signers_title), onNavigate = onBack) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text(stringResource(R.string.signers_add), fontWeight = FontWeight.Bold) },
                containerColor = LetsSignTheme.extendedColors.highlight,
                contentColor = LetsSignTheme.extendedColors.onHighlight,
            )
        },
    ) { padding ->
        if (signers.isEmpty()) {
            EmptyState(
                Icons.Default.Group,
                stringResource(R.string.signers_empty_title),
                stringResource(R.string.signers_empty_message),
                Modifier.padding(padding).padding(top = Spacing.huge),
            )
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(Dimens.screenPadding, Spacing.sm, Dimens.screenPadding, Dimens.fabBottomPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm + Spacing.xxs),
            ) {
                items(signers, key = { it.id }) { signer ->
                    SignerRow(signer, onClick = { onEdit(signer.id) }) {
                        IconButton(onClick = { onEdit(signer.id) }) { Icon(Icons.Default.Edit, stringResource(R.string.action_edit)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SignerRow(signer: User, onClick: () -> Unit, trailing: @Composable () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(Dimens.cardPadding), verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(signer.name, signerColor(signer.id.hashCode()))
            Spacer(Modifier.size(Spacing.md + Spacing.xxs))
            Column(Modifier.weight(1f)) {
                Text(signer.name, style = MaterialTheme.typography.titleMedium)
                Text(signer.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(signer.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            trailing()
        }
    }
}
