package br.com.fiap.letssign.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.letssign.R
import br.com.fiap.letssign.components.LetsSignTopBar
import br.com.fiap.letssign.components.LoadingIndicator
import br.com.fiap.letssign.components.PdfPage
import br.com.fiap.letssign.components.UiStateContainer
import br.com.fiap.letssign.components.rememberPdfRenderer
import br.com.fiap.letssign.model.UiState
import br.com.fiap.letssign.ui.theme.Dimens
import br.com.fiap.letssign.ui.theme.Spacing
import br.com.fiap.letssign.ui.viewmodel.PdfViewerViewModel
import br.com.fiap.letssign.utils.sharePdf

@Composable
fun PdfViewerScreen(viewModel: PdfViewerViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val content = (state as? UiState.Success)?.data
    val title = content?.let {
        if (it.isFinal) stringResource(R.string.viewer_final_title, it.title) else it.title
    } ?: stringResource(R.string.viewer_default_title)

    Scaffold(
        topBar = {
            LetsSignTopBar(
                title = title,
                onNavigate = onBack,
                actions = {
                    content?.let { c ->
                        IconButton(onClick = { sharePdf(context, c.file, title) }) {
                            Icon(Icons.Default.Share, stringResource(R.string.cd_share))
                        }
                    }
                },
            )
        },
    ) { padding ->
        UiStateContainer(state, Modifier.padding(padding), onRetry = viewModel::load) { data ->
            val renderer = rememberPdfRenderer(data.file)
            if (renderer == null) {
                LoadingIndicator(Modifier.padding(padding))
            } else {
                LazyColumn(
                    Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                    contentPadding = PaddingValues(Dimens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    items(renderer.pageCount) { index -> PdfPage(renderer, index, Modifier.fillMaxWidth()) }
                }
            }
        }
    }
}
