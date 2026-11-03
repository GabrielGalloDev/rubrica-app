package br.com.fiap.letssign.model

import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentStatusTest {
    private fun signer(id: String, required: Boolean = true, signed: Boolean = false) = DocumentSigner(
        id = id, userId = "u-$id", name = id, email = "$id@x.com", phone = "", role = SignerRole.CONTRACTED,
        required = required, order = 0, status = if (signed) SignerStatus.SIGNED else SignerStatus.PENDING,
    )

    private fun document(status: DocumentStatus, vararg signers: DocumentSigner) = Document(
        id = "d1", ownerId = "admin", ownerName = "Admin", title = "Contract", message = "", version = 1, parentId = null,
        status = status, fileName = "contract.pdf", pageCount = 1, localPdfPath = null, finalPdfPath = null,
        createdAt = 0L, sentAt = 0L, completedAt = null, signers = signers.toList(),
    )

    @Test
    fun `draft stays draft`() {
        assertEquals(DocumentStatus.DRAFT, document(DocumentStatus.DRAFT, signer("a", signed = true)).computeStatus())
    }

    @Test
    fun `sent while nobody signed`() {
        assertEquals(DocumentStatus.SENT, document(DocumentStatus.SENT, signer("a"), signer("b")).computeStatus())
    }

    @Test
    fun `partially signed while a required signer is pending`() {
        assertEquals(
            DocumentStatus.PARTIALLY_SIGNED,
            document(DocumentStatus.SENT, signer("a", signed = true), signer("b")).computeStatus(),
        )
    }

    @Test
    fun `completes when every required signer signed, even with optional pending`() {
        assertEquals(
            DocumentStatus.COMPLETED,
            document(DocumentStatus.SENT, signer("a", signed = true), signer("b", required = false)).computeStatus(),
        )
    }

    @Test
    fun `signer lookup by user`() {
        val doc = document(DocumentStatus.SENT, signer("a"))
        assertEquals("a", doc.signerForUser("u-a")?.id)
        assertEquals(null, doc.signerForUser("someone-else"))
    }
}
