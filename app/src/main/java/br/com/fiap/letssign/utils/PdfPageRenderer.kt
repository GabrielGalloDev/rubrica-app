package br.com.fiap.letssign.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.File

/** Page renderer for display (thread-safe through a mutex, since PdfRenderer is not). */
class PdfPageRenderer(file: File) : Closeable {
    private val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    private val renderer = PdfRenderer(fd)
    private val mutex = Mutex()

    val pageCount: Int = renderer.pageCount

    /** Width/height ratio of each page. */
    val aspectRatios: List<Float> = (0 until pageCount).map { index ->
        renderer.openPage(index).use { it.width.toFloat() / it.height.toFloat() }
    }

    suspend fun render(index: Int, widthPx: Int): Bitmap = withContext(Dispatchers.IO) {
        mutex.withLock {
            renderer.openPage(index).use { page ->
                val width = widthPx.coerceIn(MIN_WIDTH, MAX_WIDTH)
                val height = (width * page.height.toFloat() / page.width).toInt()
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmap
            }
        }
    }

    override fun close() {
        runCatching { renderer.close() }
        runCatching { fd.close() }
    }

    private companion object {
        const val MIN_WIDTH = 200
        const val MAX_WIDTH = 1600
    }
}
