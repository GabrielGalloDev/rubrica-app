package br.com.fiap.letssign.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.letssign.R
import br.com.fiap.letssign.model.SignatureField
import br.com.fiap.letssign.ui.theme.AppShapes
import br.com.fiap.letssign.utils.PdfPageRenderer
import java.io.File

private const val DEFAULT_PAGE_RATIO = 0.707f

/** Opens the PDF for rendering while the screen is visible. Returns null on error. */
@Composable
fun rememberPdfRenderer(file: File?): PdfPageRenderer? {
    var renderer by remember(file) { mutableStateOf<PdfPageRenderer?>(null) }
    DisposableEffect(file) {
        val opened = file?.takeIf { it.exists() }?.let { runCatching { PdfPageRenderer(it) }.getOrNull() }
        renderer = opened
        onDispose { opened?.close() }
    }
    return renderer
}

/**
 * Shows a PDF page with its original ratio. [overlay] receives the page size in dp, used to
 * convert the relative coordinates of the fields.
 */
@Composable
fun PdfPage(
    renderer: PdfPageRenderer,
    pageIndex: Int,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.(pageWidth: Dp, pageHeight: Dp) -> Unit = { _, _ -> },
) {
    val ratio = renderer.aspectRatios.getOrElse(pageIndex) { DEFAULT_PAGE_RATIO }
    // No fillMaxWidth: aspectRatio takes the available width and, if the height doesn't fit,
    // shrinks the page (needed on the placement screen, which doesn't scroll).
    Surface(
        modifier = modifier.aspectRatio(ratio),
        shadowElevation = 3.dp,
        shape = AppShapes.pdfPage,
        color = Color.White,
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val widthPx = with(LocalDensity.current) { maxWidth.roundToPx() }
            val bitmap by produceState<ImageBitmap?>(null, renderer, pageIndex, widthPx) {
                value = runCatching { renderer.render(pageIndex, widthPx).asImageBitmap() }.getOrNull()
            }
            bitmap?.let {
                Image(
                    it,
                    contentDescription = stringResource(R.string.cd_pdf_page, pageIndex + 1),
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize(),
                )
            } ?: CircularProgressIndicator(Modifier.align(Alignment.Center).size(28.dp))
            overlay(maxWidth, maxHeight)
        }
    }
}

/** Read-only signature field, placed relative to the page. */
@Composable
fun FieldBox(
    field: SignatureField,
    pageWidth: Dp,
    pageHeight: Dp,
    color: Color,
    label: String,
    signature: Bitmap? = null,
    highlighted: Boolean = true,
) {
    Box(
        Modifier
            .offset(pageWidth * field.x, pageHeight * field.y)
            .size(pageWidth * field.width, pageHeight * field.height)
            .background(
                if (signature != null) Color.Transparent else color.copy(alpha = if (highlighted) 0.18f else 0.08f),
                AppShapes.signatureField,
            )
            .border(if (highlighted) 1.5.dp else 1.dp, color.copy(alpha = if (highlighted) 1f else 0.4f), AppShapes.signatureField),
    ) {
        if (signature != null) {
            Image(
                signature.asImageBitmap(),
                contentDescription = stringResource(R.string.cd_signature),
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(2.dp),
            )
        } else {
            Text(
                label,
                color = color,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.Center).padding(horizontal = 3.dp),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
