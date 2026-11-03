package br.com.fiap.letssign.utils

import android.content.Context
import android.graphics.BitmapFactory
import br.com.fiap.letssign.R
import br.com.fiap.letssign.model.Document
import br.com.fiap.letssign.model.SignerStatus
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import java.io.File
import java.text.Normalizer

/**
 * Builds the signed PDF: stamps each signer's signature on their fields (converting the
 * relative coordinates to the PDF system) and appends an audit trail page.
 *
 * [localizedContext] returns a context in the current app language for the audit texts.
 */
class PdfStamper(private val localizedContext: () -> Context) {

    fun stamp(document: Document, original: File, signatureImages: Map<String, File>, output: File) {
        val context = localizedContext()
        PDDocument.load(original).use { pdf ->
            document.fields.groupBy { it.page }.forEach { (pageIndex, fields) ->
                if (pageIndex >= pdf.numberOfPages) return@forEach
                val page = pdf.getPage(pageIndex)
                val box = page.cropBox
                PDPageContentStream(pdf, page, PDPageContentStream.AppendMode.APPEND, true, true).use { cs ->
                    fields.forEach { field ->
                        val signer = document.signers.firstOrNull { it.id == field.signerId } ?: return@forEach
                        val imageFile = signatureImages[signer.id]
                        if (signer.status != SignerStatus.SIGNED || imageFile == null || !imageFile.exists()) return@forEach
                        val image = LosslessFactory.createFromImage(pdf, BitmapFactory.decodeFile(imageFile.path))
                        // The PDF origin is the bottom-left corner.
                        val w = field.width * box.width
                        val h = field.height * box.height
                        val x = box.lowerLeftX + field.x * box.width
                        val y = box.lowerLeftY + box.height - (field.y + field.height) * box.height
                        val labelHeight = (h * LABEL_RATIO).coerceIn(MIN_LABEL, MAX_LABEL)
                        cs.drawImage(image, x, y + labelHeight, w, h - labelHeight)
                        cs.beginText()
                        cs.setFont(PDType1Font.HELVETICA, labelHeight * 0.8f)
                        cs.setNonStrokingColor(0.25f, 0.25f, 0.35f)
                        cs.newLineAtOffset(x + 1f, y + 1f)
                        cs.showText(ascii("${signer.name} - ${signer.signedAt?.let { context.dateTime(it) }.orEmpty()}"))
                        cs.endText()
                    }
                }
            }
            appendAuditPage(context, pdf, document)
            output.parentFile?.mkdirs()
            pdf.save(output)
        }
    }

    private fun appendAuditPage(context: Context, pdf: PDDocument, document: Document) {
        val page = PDPage(PDRectangle.A4)
        pdf.addPage(page)
        PDPageContentStream(pdf, page).use { cs ->
            var y = page.mediaBox.height - TOP_MARGIN
            fun line(text: String, size: Float = 10f, bold: Boolean = false, gap: Float = 16f) {
                cs.beginText()
                cs.setFont(if (bold) PDType1Font.HELVETICA_BOLD else PDType1Font.HELVETICA, size)
                cs.newLineAtOffset(LEFT_MARGIN, y)
                cs.showText(ascii(text))
                cs.endText()
                y -= gap
            }
            cs.setNonStrokingColor(0.36f, 0.3f, 0.96f)
            line(context.getString(R.string.pdf_audit_title), 18f, bold = true, gap = 28f)
            cs.setNonStrokingColor(0.1f, 0.1f, 0.15f)
            line(context.getString(R.string.pdf_audit_document, document.title, document.version), 11f, bold = true)
            line(context.getString(R.string.pdf_audit_id, document.id))
            line(context.getString(R.string.pdf_audit_sender, document.ownerName))
            document.sentAt?.let { line(context.getString(R.string.pdf_audit_sent_at, context.dateTime(it))) }
            document.completedAt?.let { line(context.getString(R.string.pdf_audit_completed_at, context.dateTime(it))) }
            y -= SECTION_GAP
            line(context.getString(R.string.pdf_audit_signers), 13f, bold = true, gap = 20f)
            document.signers.forEach { s ->
                val status = if (s.status == SignerStatus.SIGNED) {
                    context.getString(R.string.pdf_audit_signed_at, s.signedAt?.let { context.dateTime(it) }.orEmpty())
                } else {
                    context.getString(R.string.signer_status_pending)
                }
                val role = context.getString(s.role.labelRes) +
                    if (s.required) "" else " (${context.getString(R.string.signer_optional)})"
                line("- ${s.name} <${s.email}> | $role | $status")
            }
            y -= SECTION_GAP
            line(context.getString(R.string.pdf_audit_history), 13f, bold = true, gap = 20f)
            document.events.forEach { e ->
                if (y < BOTTOM_MARGIN) return@forEach
                val detail = context.getString(e.type.detailRes, e.actorName)
                line("${context.dateTime(e.timestamp)}  ${context.getString(e.type.titleRes)} - $detail", 9f, gap = 14f)
            }
        }
    }

    private fun Context.dateTime(millis: Long) = Formatters.dateTime(millis, resources.configuration.locales[0])

    /** The standard fonts (WinAnsi) don't cover every character, so accents are removed. */
    private fun ascii(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .replace(Regex("[^\\x20-\\x7E]"), "")

    private companion object {
        const val LABEL_RATIO = 0.22f
        const val MIN_LABEL = 4f
        const val MAX_LABEL = 9f
        const val TOP_MARGIN = 60f
        const val LEFT_MARGIN = 50f
        const val BOTTOM_MARGIN = 50f
        const val SECTION_GAP = 10f
    }
}
