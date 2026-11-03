package br.com.fiap.letssign.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import br.com.fiap.letssign.R
import java.io.File

private const val PDF_MIME_TYPE = "application/pdf"

/** Shares / opens the PDF with another app (reader, e-mail, WhatsApp...). */
fun sharePdf(context: Context, file: File, title: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = PDF_MIME_TYPE
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, title)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.viewer_share_chooser)))
}
