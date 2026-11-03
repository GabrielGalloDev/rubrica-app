package br.com.fiap.letssign.utils

import br.com.fiap.letssign.R
import br.com.fiap.letssign.model.Document
import br.com.fiap.letssign.model.DocumentSigner
import br.com.fiap.letssign.model.DocumentStatus
import br.com.fiap.letssign.model.SignatureField
import br.com.fiap.letssign.model.SignerRole
import br.com.fiap.letssign.model.ValidationIssue
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentValidatorTest {
    private val signer = DocumentSigner("s1", "u1", "Maria Silva", "maria@x.com", "11999990000", SignerRole.CONTRACTOR, order = 0)
    private val field = SignatureField("f1", "s1", page = 0, x = 0.1f, y = 0.1f, width = 0.3f, height = 0.1f)

    private fun document(
        signers: List<DocumentSigner> = listOf(signer),
        fields: List<SignatureField> = listOf(field),
        title: String = "Contract",
    ) = Document(
        id = "d1", ownerId = "admin", ownerName = "Admin", title = title, message = "", version = 1, parentId = null,
        status = DocumentStatus.DRAFT, fileName = "contract.pdf", pageCount = 2, localPdfPath = null, finalPdfPath = null,
        createdAt = 0L, sentAt = null, completedAt = null, signers = signers, fields = fields,
    )

    @Test
    fun `valid document has no issues`() {
        assertTrue(DocumentValidator.validate(document(), hasPdf = true).isEmpty())
    }

    @Test
    fun `requires title, pdf and at least one signer`() {
        val issues = DocumentValidator.validate(document(signers = emptyList(), fields = emptyList(), title = " "), hasPdf = false)
        assertTrue(ValidationIssue(R.string.error_document_title_required) in issues)
        assertTrue(ValidationIssue(R.string.error_document_pdf_required) in issues)
        assertTrue(ValidationIssue(R.string.error_document_signer_required) in issues)
    }

    @Test
    fun `every signer needs a field`() {
        val issues = DocumentValidator.validate(document(fields = emptyList()), hasPdf = true)
        assertTrue(ValidationIssue(R.string.error_document_signer_without_field, listOf("Maria Silva")) in issues)
    }

    @Test
    fun `fields must stay inside the page`() {
        val outside = field.copy(x = 0.9f, width = 0.3f)
        val issues = DocumentValidator.validate(document(fields = listOf(outside)), hasPdf = true)
        assertTrue(ValidationIssue(R.string.error_document_field_outside_page) in issues)
    }

    @Test
    fun `fields must belong to a signer`() {
        val orphan = field.copy(id = "f2", signerId = "unknown")
        val issues = DocumentValidator.validate(document(fields = listOf(field, orphan)), hasPdf = true)
        assertTrue(ValidationIssue(R.string.error_document_field_without_signer) in issues)
    }
}
