package br.com.fiap.letssign.components

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.com.fiap.letssign.R
import br.com.fiap.letssign.ui.theme.AppShapes
import br.com.fiap.letssign.ui.theme.Dimens
import br.com.fiap.letssign.ui.theme.SignatureGuide
import br.com.fiap.letssign.ui.theme.SignatureHint
import br.com.fiap.letssign.ui.theme.SignatureInk
import br.com.fiap.letssign.ui.theme.Spacing
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min

private const val STROKE_WIDTH = 6f
private const val GUIDE_MARGIN = 24f
private const val GUIDE_HEIGHT_RATIO = 0.75f
private const val CROP_MARGIN = 12f

class CapturedSignature(val bitmap: Bitmap, val png: ByteArray)

/** Dialog where the signer draws the signature with a finger. */
@Composable
fun SignaturePadDialog(signerName: String, onDismiss: () -> Unit, onConfirm: (CapturedSignature) -> Unit) {
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    var current by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = AppShapes.cardLarge, modifier = Modifier.fillMaxWidth().padding(Spacing.lg)) {
            Column(Modifier.padding(Spacing.xl)) {
                Text(stringResource(R.string.signature_pad_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(signerName, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(Spacing.lg))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(Dimens.signaturePadHeight)
                        .background(Color.White, AppShapes.message)
                        .border(Dimens.borderWidth, MaterialTheme.colorScheme.outlineVariant, AppShapes.message)
                        .onSizeChanged { size = it }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { current = listOf(it) },
                                onDrag = { change, _ -> current = current + change.position },
                                onDragEnd = { strokes += current; current = emptyList() },
                                onDragCancel = { strokes += current; current = emptyList() },
                            )
                        },
                ) {
                    Canvas(Modifier.matchParentSize()) {
                        // Signature base line.
                        val baseY = this.size.height * GUIDE_HEIGHT_RATIO
                        drawLine(SignatureGuide, Offset(GUIDE_MARGIN, baseY), Offset(this.size.width - GUIDE_MARGIN, baseY), strokeWidth = 2f)
                        (strokes + listOf(current)).forEach { points ->
                            if (points.size < 2) return@forEach
                            val path = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                points.drop(1).forEach { lineTo(it.x, it.y) }
                            }
                            drawPath(path, SignatureInk, style = Stroke(width = STROKE_WIDTH, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                    }
                    if (strokes.isEmpty() && current.isEmpty()) {
                        Text(stringResource(R.string.signature_pad_hint), color = SignatureHint, modifier = Modifier.align(Alignment.Center))
                    }
                }
                Spacer(Modifier.height(Spacing.lg))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm, Alignment.End)) {
                    TextButton(onClick = { strokes.clear() }, enabled = strokes.isNotEmpty()) { Text(stringResource(R.string.action_clear)) }
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
                    Button(
                        onClick = { exportSignature(strokes, size)?.let(onConfirm) },
                        enabled = strokes.any { it.size > 1 },
                    ) { Text(stringResource(R.string.action_confirm)) }
                }
            }
        }
    }
}

/** Turns the strokes into a transparent PNG cropped around the signature. */
private fun exportSignature(strokes: List<List<Offset>>, size: IntSize): CapturedSignature? {
    val points = strokes.flatten()
    if (points.isEmpty() || size.width == 0) return null
    val left = max(0f, points.minOf { it.x } - CROP_MARGIN)
    val top = max(0f, points.minOf { it.y } - CROP_MARGIN)
    val right = min(size.width.toFloat(), points.maxOf { it.x } + CROP_MARGIN)
    val bottom = min(size.height.toFloat(), points.maxOf { it.y } + CROP_MARGIN)
    val width = (right - left).toInt().coerceAtLeast(1)
    val height = (bottom - top).toInt().coerceAtLeast(1)

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = Paint().apply {
        color = SignatureInk.toArgb()
        strokeWidth = STROKE_WIDTH
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }
    strokes.filter { it.size > 1 }.forEach { stroke ->
        val path = android.graphics.Path().apply {
            moveTo(stroke.first().x - left, stroke.first().y - top)
            stroke.drop(1).forEach { lineTo(it.x - left, it.y - top) }
        }
        canvas.drawPath(path, paint)
    }
    val png = ByteArrayOutputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.toByteArray()
    }
    return CapturedSignature(bitmap, png)
}
