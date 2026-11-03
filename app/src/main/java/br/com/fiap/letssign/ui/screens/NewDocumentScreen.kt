package br.com.fiap.letssign.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.letssign.R
import br.com.fiap.letssign.components.FieldBox
import br.com.fiap.letssign.components.InlineErrorMessage
import br.com.fiap.letssign.components.LetsSignButton
import br.com.fiap.letssign.components.LetsSignOutlinedButton
import br.com.fiap.letssign.components.LetsSignTextField
import br.com.fiap.letssign.components.LetsSignTonalButton
import br.com.fiap.letssign.components.LetsSignTopBar
import br.com.fiap.letssign.components.LoadingIndicator
import br.com.fiap.letssign.components.PdfPage
import br.com.fiap.letssign.components.SectionTitle
import br.com.fiap.letssign.components.TopBarNavigation
import br.com.fiap.letssign.components.UserAvatar
import br.com.fiap.letssign.components.rememberPdfRenderer
import br.com.fiap.letssign.model.AppError
import br.com.fiap.letssign.model.DocumentSigner
import br.com.fiap.letssign.model.SignatureField
import br.com.fiap.letssign.model.SignerRole
import br.com.fiap.letssign.model.User
import br.com.fiap.letssign.ui.theme.AppShapes
import br.com.fiap.letssign.ui.theme.Dimens
import br.com.fiap.letssign.ui.theme.Emerald
import br.com.fiap.letssign.ui.theme.Spacing
import br.com.fiap.letssign.ui.theme.signerColor
import br.com.fiap.letssign.ui.viewmodel.NewDocumentUiState
import br.com.fiap.letssign.ui.viewmodel.NewDocumentViewModel
import br.com.fiap.letssign.ui.viewmodel.SendState
import br.com.fiap.letssign.ui.viewmodel.WizardStep
import br.com.fiap.letssign.utils.PdfValidator
import br.com.fiap.letssign.utils.labelRes
import br.com.fiap.letssign.utils.text

private const val PDF_MIME_TYPE = "application/pdf"

@Composable
fun NewDocumentScreen(
    viewModel: NewDocumentViewModel,
    onExit: () -> Unit,
    onManageSigners: () -> Unit,
    onOpenDocument: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sendState = state.sendState

    BackHandler(enabled = sendState !is SendState.Success) { if (!viewModel.back()) onExit() }

    if (sendState is SendState.Success) {
        SendSuccess(state.signers.size, onTrack = { onOpenDocument(viewModel.documentId) }, onHome = onExit)
        return
    }

    Scaffold(
        topBar = {
            LetsSignTopBar(
                title = if (state.isNewVersion) stringResource(R.string.wizard_new_version_title, state.version)
                else stringResource(R.string.wizard_title),
                subtitle = stringResource(R.string.wizard_step_of, state.step.ordinal + 1, WizardStep.entries.size, stringResource(state.step.titleRes)),
                navigation = TopBarNavigation.CLOSE,
                onNavigate = { viewModel.saveDraft(); onExit() },
            )
        },
        bottomBar = { WizardBottomBar(state, viewModel) },
    ) { padding ->
        if (state.isLoading) {
            LoadingIndicator(Modifier.padding(padding))
            return@Scaffold
        }
        Column(Modifier.padding(padding).fillMaxSize()) {
            StepIndicator(state.step, onClick = viewModel::goTo)
            Box(Modifier.weight(1f)) {
                when (state.step) {
                    WizardStep.INFO -> InfoStep(state, viewModel)
                    WizardStep.SIGNERS -> SignersStep(state, viewModel, onManageSigners)
                    WizardStep.FIELDS -> FieldsStep(state, viewModel)
                    WizardStep.REVIEW -> ReviewStep(state, viewModel)
                }
            }
        }
    }

    if (sendState is SendState.Failure) {
        AlertDialog(
            onDismissRequest = viewModel::dismissFailure,
            icon = { Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.wizard_send_failed_title)) },
            text = {
                Text(
                    when (val error = sendState.error) {
                        is AppError.Validation, AppError.PermissionDenied, AppError.DocumentLocked -> error.text()
                        else -> stringResource(R.string.wizard_send_failed_message)
                    }
                )
            },
            confirmButton = { Button(onClick = viewModel::send) { Text(stringResource(R.string.action_retry)) } },
            dismissButton = { TextButton(onClick = viewModel::dismissFailure) { Text(stringResource(R.string.action_close)) } },
        )
    }
}

@Composable
private fun StepIndicator(current: WizardStep, onClick: (WizardStep) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WizardStep.entries.forEachIndexed { index, step ->
            val done = step.ordinal < current.ordinal
            val active = step == current
            val color = if (done || active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            Surface(
                color = if (active || done) color else Color.Transparent,
                shape = CircleShape,
                modifier = Modifier.size(Dimens.stepIndicator).border(1.5.dp, color, CircleShape).clickable(enabled = done) { onClick(step) },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "${index + 1}",
                        color = if (active || done) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                }
            }
            if (index < WizardStep.entries.lastIndex) {
                HorizontalDivider(
                    Modifier.weight(1f).padding(horizontal = Spacing.xs),
                    thickness = 2.dp,
                    color = if (done) color else MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

@Composable
private fun WizardBottomBar(state: NewDocumentUiState, viewModel: NewDocumentViewModel) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest, shape = AppShapes.bottomBar, shadowElevation = 12.dp) {
        Column(Modifier.navigationBarsPadding().padding(horizontal = Dimens.screenPadding, vertical = Spacing.lg)) {
            state.stepError?.let {
                InlineErrorMessage(it.text())
                Spacer(Modifier.height(Spacing.sm + Spacing.xxs))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm + Spacing.xxs)) {
                if (state.step != WizardStep.INFO) {
                    LetsSignOutlinedButton(stringResource(R.string.action_back), onClick = { viewModel.back() }, modifier = Modifier.weight(1f))
                }
                if (state.step == WizardStep.REVIEW) {
                    LetsSignButton(
                        stringResource(R.string.wizard_confirm_send),
                        onClick = viewModel::send,
                        isLoading = state.sendState == SendState.Sending,
                        icon = Icons.AutoMirrored.Filled.Send,
                        modifier = Modifier.weight(2f),
                    )
                } else {
                    LetsSignButton(
                        stringResource(R.string.action_continue),
                        onClick = viewModel::next,
                        enabled = !state.isImporting,
                        modifier = Modifier.weight(2f),
                    )
                }
            }
        }
    }
}

// ============================ Step 1 ============================

@Composable
private fun InfoStep(state: NewDocumentUiState, viewModel: NewDocumentViewModel) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.onPdfPicked(uri, context.displayName(uri))
    }
    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md + Spacing.xxs),
    ) {
        LetsSignTextField(
            label = stringResource(R.string.wizard_document_title),
            value = state.title,
            onValueChange = viewModel::onTitleChange,
        )
        SectionTitle(stringResource(R.string.wizard_pdf_file))
        OutlinedCard(
            onClick = { picker.launch(arrayOf(PDF_MIME_TYPE)) },
            enabled = !state.isImporting && !state.isNewVersion,
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.cardLarge,
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        ) {
            Column(Modifier.fillMaxWidth().padding(Spacing.xxl), horizontalAlignment = Alignment.CenterHorizontally) {
                when {
                    state.isImporting -> {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(Spacing.sm + Spacing.xxs))
                        Text(stringResource(R.string.wizard_validating_file))
                    }
                    state.pdfFile != null -> {
                        Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(Dimens.iconLarge))
                        Spacer(Modifier.height(Spacing.sm))
                        Text(state.fileName, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                        Text(
                            stringResource(R.string.wizard_pdf_valid, pluralStringResource(R.plurals.pages_count, state.pageCount, state.pageCount)),
                            color = Emerald,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        if (!state.isNewVersion) {
                            Text(
                                stringResource(R.string.wizard_tap_to_change),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    else -> {
                        Icon(Icons.Default.UploadFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(Dimens.iconLarge))
                        Spacer(Modifier.height(Spacing.sm))
                        Text(stringResource(R.string.wizard_pick_pdf), fontWeight = FontWeight.SemiBold)
                        Text(
                            stringResource(R.string.wizard_pdf_max_size, PdfValidator.MAX_SIZE_MB),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        state.pdfError?.let { InlineErrorMessage(stringResource(it)) }

        val renderer = rememberPdfRenderer(state.pdfFile)
        if (renderer != null) {
            SectionTitle(stringResource(R.string.wizard_preview))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm + Spacing.xxs)) {
                items(renderer.pageCount) { index ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PdfPage(renderer, index, Modifier.width(Dimens.thumbnailWidth))
                        Text(stringResource(R.string.wizard_page_short, index + 1), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

private fun Context.displayName(uri: Uri): String? =
    contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }

// ============================ Step 2 ============================

private sealed interface PickerMode {
    data object Add : PickerMode
    data class Replace(val signerId: String) : PickerMode
}

@Composable
private fun SignersStep(state: NewDocumentUiState, viewModel: NewDocumentViewModel, onManageSigners: () -> Unit) {
    val available by viewModel.availableSigners.collectAsStateWithLifecycle()
    var pickerMode by remember { mutableStateOf<PickerMode?>(null) }

    LazyColumn(contentPadding = PaddingValues(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm + Spacing.xxs)) {
        item {
            Text(stringResource(R.string.wizard_signers_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.wizard_signers_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        items(state.signers, key = { it.id }) { signer ->
            SignerEditorCard(
                signer = signer,
                color = signerColor(state.signerIndex(signer.id)),
                onReplace = { pickerMode = PickerMode.Replace(signer.id) },
                onRemove = { viewModel.removeSigner(signer.id) },
                onRoleChange = { viewModel.setRole(signer.id, it) },
                onRequiredChange = { viewModel.setRequired(signer.id, it) },
            )
        }
        item {
            LetsSignTonalButton(
                stringResource(R.string.wizard_add_signer),
                onClick = { pickerMode = PickerMode.Add },
                icon = Icons.Default.PersonAdd,
            )
        }
    }

    pickerMode?.let { mode ->
        SignerPickerDialog(
            title = stringResource(if (mode is PickerMode.Replace) R.string.wizard_replace_with else R.string.wizard_select_signer),
            candidates = available.filter { user -> state.signers.none { it.userId == user.id } },
            onPick = { user ->
                if (mode is PickerMode.Replace) viewModel.replaceSigner(mode.signerId, user) else viewModel.addSigner(user)
                pickerMode = null
            },
            onRegisterNew = { pickerMode = null; onManageSigners() },
            onDismiss = { pickerMode = null },
        )
    }
}

@Composable
private fun SignerEditorCard(
    signer: DocumentSigner,
    color: Color,
    onReplace: () -> Unit,
    onRemove: () -> Unit,
    onRoleChange: (SignerRole) -> Unit,
    onRequiredChange: (Boolean) -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.md + Spacing.xxs)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(signer.name, color)
                Spacer(Modifier.size(Spacing.md))
                Column(Modifier.weight(1f)) {
                    Text(signer.name, fontWeight = FontWeight.SemiBold)
                    Text(signer.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onReplace) { Icon(Icons.Default.SwapHoriz, stringResource(R.string.action_replace)) }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, stringResource(R.string.action_remove), tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(Spacing.sm))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(Spacing.xs + Spacing.xxs)) {
                SignerRole.entries.forEach { role ->
                    FilterChip(
                        selected = signer.role == role,
                        onClick = { onRoleChange(role) },
                        label = { Text(stringResource(role.labelRes)) },
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.wizard_signature_required), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Switch(checked = signer.required, onCheckedChange = onRequiredChange)
            }
        }
    }
}

@Composable
private fun SignerPickerDialog(
    title: String,
    candidates: List<User>,
    onPick: (User) -> Unit,
    onRegisterNew: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            if (candidates.isEmpty()) {
                Text(stringResource(R.string.wizard_no_signers_available))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    items(candidates, key = { it.id }) { user ->
                        Row(
                            Modifier.fillMaxWidth().clickable { onPick(user) }.padding(vertical = Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            UserAvatar(user.name)
                            Spacer(Modifier.size(Spacing.sm + Spacing.xxs))
                            Column {
                                Text(user.name, fontWeight = FontWeight.Medium)
                                Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onRegisterNew) { Text(stringResource(R.string.wizard_register_new)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

// ============================ Step 3 ============================

@Composable
private fun FieldsStep(state: NewDocumentUiState, viewModel: NewDocumentViewModel) {
    val renderer = rememberPdfRenderer(state.pdfFile)

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs + Spacing.xxs),
        ) {
            state.signers.forEach { signer ->
                val count = state.fields.count { it.signerId == signer.id }
                FilterChip(
                    selected = state.activeSignerId == signer.id,
                    onClick = { viewModel.selectSigner(signer.id) },
                    leadingIcon = { Box(Modifier.size(10.dp).background(signerColor(state.signerIndex(signer.id)), CircleShape)) },
                    label = { Text(stringResource(R.string.wizard_signer_field_count, signer.name.substringBefore(" "), count)) },
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.goToPage(state.currentPage - 1) }, enabled = state.currentPage > 0) {
                Icon(Icons.Default.ChevronLeft, stringResource(R.string.cd_previous_page))
            }
            Text(stringResource(R.string.wizard_page_of, state.currentPage + 1, state.pageCount), fontWeight = FontWeight.Medium)
            IconButton(onClick = { viewModel.goToPage(state.currentPage + 1) }, enabled = state.currentPage < state.pageCount - 1) {
                Icon(Icons.Default.ChevronRight, stringResource(R.string.cd_next_page))
            }
            Spacer(Modifier.weight(1f))
            state.selectedFieldId?.let { id ->
                IconButton(onClick = { viewModel.deleteField(id) }) {
                    Icon(Icons.Default.Delete, stringResource(R.string.cd_delete_field), tint = MaterialTheme.colorScheme.error)
                }
            }
            FilledTonalButton(onClick = viewModel::addField, enabled = state.activeSignerId != null) {
                Icon(Icons.Default.AddBox, contentDescription = null, modifier = Modifier.size(Dimens.iconSmall))
                Spacer(Modifier.size(Spacing.xs + Spacing.xxs))
                Text(stringResource(R.string.wizard_add_field))
            }
        }
        Box(
            Modifier.weight(1f).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(Spacing.md),
            contentAlignment = Alignment.Center,
        ) {
            if (renderer == null) {
                CircularProgressIndicator()
            } else {
                PdfPage(renderer, state.currentPage) { w, h ->
                    Box(Modifier.matchParentSize().pointerInput(Unit) { detectTapGestures { viewModel.selectField(null) } })
                    state.fields.filter { it.page == state.currentPage }.forEach { field ->
                        EditableField(
                            field, w, h,
                            color = signerColor(state.signerIndex(field.signerId)),
                            label = state.signerName(field.signerId),
                            selected = state.selectedFieldId == field.id,
                            onSelect = { viewModel.selectField(field) },
                            onMove = { dx, dy -> viewModel.moveField(field.id, dx, dy) },
                            onResize = { dw, dh -> viewModel.resizeField(field.id, dw, dh) },
                        )
                    }
                }
            }
        }
        Text(
            stringResource(R.string.wizard_fields_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        )
    }
}

@Composable
private fun EditableField(
    field: SignatureField,
    pageWidth: Dp,
    pageHeight: Dp,
    color: Color,
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    onMove: (Float, Float) -> Unit,
    onResize: (Float, Float) -> Unit,
) {
    val density = LocalDensity.current
    val widthPx = with(density) { pageWidth.toPx() }
    val heightPx = with(density) { pageHeight.toPx() }
    Box(
        Modifier
            .offset(pageWidth * field.x, pageHeight * field.y)
            .size(pageWidth * field.width, pageHeight * field.height)
            .background(color.copy(alpha = if (selected) 0.32f else 0.18f), AppShapes.signatureField)
            .border(if (selected) 2.dp else 1.dp, color, AppShapes.signatureField)
            .pointerInput(field.id, widthPx, heightPx) {
                detectDragGestures(onDragStart = { onSelect() }) { change, drag ->
                    change.consume()
                    onMove(drag.x / widthPx, drag.y / heightPx)
                }
            }
            .pointerInput(field.id) { detectTapGestures { onSelect() } },
    ) {
        Text(
            label.substringBefore(" "), color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1,
            overflow = TextOverflow.Ellipsis, modifier = Modifier.align(Alignment.Center).padding(horizontal = Spacing.xs),
        )
        if (selected) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .offset(6.dp, 6.dp)
                    .size(Dimens.resizeHandle)
                    .background(color, CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .pointerInput(field.id, widthPx, heightPx) {
                        detectDragGestures { change, drag ->
                            change.consume()
                            onResize(drag.x / widthPx, drag.y / heightPx)
                        }
                    },
            )
        }
    }
}

// ============================ Step 4 ============================

@Composable
private fun ReviewStep(state: NewDocumentUiState, viewModel: NewDocumentViewModel) {
    val renderer = rememberPdfRenderer(state.pdfFile)
    val issues = viewModel.reviewIssues(state)
    LazyColumn(
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        modifier = Modifier.imePadding(),
    ) {
        item {
            ReviewSection(stringResource(R.string.wizard_review_document), onEdit = { viewModel.goTo(WizardStep.INFO) }) {
                Text(state.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${state.fileName} • ${pluralStringResource(R.plurals.pages_count, state.pageCount, state.pageCount)} • " +
                        stringResource(R.string.wizard_version_short, state.version),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (renderer != null) {
                    Spacer(Modifier.height(Spacing.sm + Spacing.xxs))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        items(renderer.pageCount) { index ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                PdfPage(renderer, index, Modifier.width(Dimens.reviewThumbnailWidth)) { w, h ->
                                    state.fields.filter { it.page == index }.forEach { field ->
                                        FieldBox(field, w, h, signerColor(state.signerIndex(field.signerId)), "")
                                    }
                                }
                                Text(stringResource(R.string.wizard_page_short, index + 1), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
        item {
            ReviewSection(stringResource(R.string.wizard_review_signers, state.signers.size), onEdit = { viewModel.goTo(WizardStep.SIGNERS) }) {
                state.signers.forEachIndexed { index, signer ->
                    SignerSummaryRow(signer, signerColor(index), state.fields.filter { it.signerId == signer.id })
                }
            }
        }
        item {
            ReviewSection(stringResource(R.string.wizard_review_fields), onEdit = { viewModel.goTo(WizardStep.FIELDS) }) {
                state.fields.groupBy { it.page }.toSortedMap().forEach { (page, list) ->
                    val names = list.map { state.signerName(it.signerId).substringBefore(" ") }.distinct().joinToString()
                    Text(
                        stringResource(
                            R.string.wizard_review_fields_line, page + 1,
                            pluralStringResource(R.plurals.fields_count, list.size, list.size), names,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        item {
            ReviewSection(stringResource(R.string.wizard_review_message)) {
                LetsSignTextField(
                    label = stringResource(R.string.wizard_review_message),
                    value = state.message,
                    onValueChange = viewModel::onMessageChange,
                    placeholder = stringResource(R.string.wizard_message_placeholder),
                    singleLine = false,
                    minLines = 3,
                )
            }
        }
        if (issues.isNotEmpty()) {
            item { InlineErrorMessage(issues.map { "• ${it.text()}" }.joinToString("\n")) }
        }
    }
}

@Composable
private fun ReviewSection(title: String, onEdit: (() -> Unit)? = null, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Dimens.cardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                onEdit?.let { TextButton(onClick = it) { Text(stringResource(R.string.action_edit)) } }
            }
            content()
        }
    }
}

@Composable
private fun SignerSummaryRow(signer: DocumentSigner, color: Color, fields: List<SignatureField>) {
    Row(Modifier.padding(vertical = Spacing.xs + Spacing.xxs), verticalAlignment = Alignment.CenterVertically) {
        UserAvatar(signer.name, color)
        Spacer(Modifier.size(Spacing.sm + Spacing.xxs))
        Column(Modifier.weight(1f)) {
            Text(signer.name, fontWeight = FontWeight.Medium)
            Text(
                signerSummary(signer, fields.size) + " • " +
                    stringResource(R.string.wizard_pages_list, fields.map { it.page + 1 }.distinct().sorted().joinToString()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SendSuccess(signerCount: Int, onTrack: () -> Unit, onHome: () -> Unit) {
    BackHandler(onBack = onHome)
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(color = Emerald.copy(alpha = 0.15f), shape = CircleShape) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald, modifier = Modifier.padding(Spacing.xl).size(Dimens.iconHero))
        }
        Spacer(Modifier.height(Spacing.xxl))
        Text(stringResource(R.string.wizard_sent_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(Spacing.sm))
        Text(
            pluralStringResource(R.plurals.wizard_sent_message, signerCount, signerCount),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Spacing.xxxl))
        LetsSignButton(stringResource(R.string.wizard_track_document), onClick = onTrack)
        Spacer(Modifier.height(Spacing.sm))
        LetsSignOutlinedButton(stringResource(R.string.wizard_back_home), onClick = onHome)
    }
}
