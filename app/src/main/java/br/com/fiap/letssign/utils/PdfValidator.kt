package br.com.fiap.letssign.utils

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import br.com.fiap.letssign.model.AppError
import java.io.File

object PdfValidator {
    /** Files go to the Realtime Database as Base64, which limits each value to 10 MB. */
    const val MAX_SIZE_MB = 7
    private const val MAX_SIZE_BYTES = MAX_SIZE_MB * 1024L * 1024L
    private const val PDF_SIGNATURE = "%PDF-"
    private const val PROBE_SIZE = 32

    /** Validates the file and returns its page count. Throws a PDF [AppError] otherwise. */
    fun validate(file: File): Int {
        if (!file.exists() || file.length() == 0L) throw AppError.PdfEmpty
        if (file.length() > MAX_SIZE_BYTES) throw AppError.PdfTooLarge
        val header = file.inputStream().use { input -> ByteArray(PDF_SIGNATURE.length).also { input.read(it) } }
        if (String(header, Charsets.US_ASCII) != PDF_SIGNATURE) throw AppError.PdfInvalidFormat
        return try {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
                PdfRenderer(fd).use { renderer ->
                    if (renderer.pageCount == 0) throw AppError.PdfNoPages
                    // Renders the first page to make sure the content can be processed.
                    renderer.openPage(0).use { page ->
                        val bitmap = Bitmap.createBitmap(PROBE_SIZE, PROBE_SIZE, Bitmap.Config.ARGB_8888)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bitmap.recycle()
                    }
                    renderer.pageCount
                }
            }
        } catch (error: AppError) {
            throw error
        } catch (_: SecurityException) {
            throw AppError.PdfPasswordProtected
        } catch (_: Exception) {
            throw AppError.PdfCorrupted
        }
    }
}
