package br.com.fiap.letssign.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import br.com.fiap.letssign.R

enum class TopBarNavigation { BACK, CLOSE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetsSignTopBar(
    title: String,
    onNavigate: () -> Unit,
    subtitle: String? = null,
    navigation: TopBarNavigation = TopBarNavigation.BACK,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Column {
                Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                subtitle?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigate) {
                when (navigation) {
                    TopBarNavigation.BACK ->
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_back))
                    TopBarNavigation.CLOSE ->
                        Icon(Icons.Default.Close, stringResource(R.string.cd_close))
                }
            }
        },
        actions = actions,
    )
}
