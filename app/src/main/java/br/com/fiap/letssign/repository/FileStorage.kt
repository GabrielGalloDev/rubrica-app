package br.com.fiap.letssign.repository

import android.content.Context
import android.net.Uri
import br.com.fiap.letssign.model.AppError
import java.io.File

/** Files in the app private storage: documents/{id}/... */
class FileStorage(private val context: Context) {
    private val root get() = File(context.filesDir, ROOT_DIR)

    fun documentDir(documentId: String) = File(root, documentId).apply { mkdirs() }
    fun originalPdf(documentId: String) = File(documentDir(documentId), ORIGINAL_PDF)
    fun finalPdf(documentId: String) = File(documentDir(documentId), FINAL_PDF)
    fun previewPdf(documentId: String) = File(documentDir(documentId), PREVIEW_PDF)
    fun importTemp(documentId: String) = File(documentDir(documentId), IMPORT_TEMP)
    fun signatureImage(documentId: String, signerId: String) =
        File(File(documentDir(documentId), SIGNATURES_DIR).apply { mkdirs() }, "$signerId.png")

    fun copyFromUri(uri: Uri, destination: File) {
        destination.parentFile?.mkdirs()
        val input = context.contentResolver.openInputStream(uri) ?: throw AppError.PdfEmpty
        input.use { src -> destination.outputStream().use { dst -> src.copyTo(dst) } }
    }

    fun deleteDocument(documentId: String) {
        File(root, documentId).deleteRecursively()
    }

    private companion object {
        const val ROOT_DIR = "documents"
        const val ORIGINAL_PDF = "original.pdf"
        const val FINAL_PDF = "final.pdf"
        const val PREVIEW_PDF = "preview.pdf"
        const val IMPORT_TEMP = "import.tmp"
        const val SIGNATURES_DIR = "signatures"
    }
}
