package br.com.fiap.letssign.utils

import br.com.fiap.letssign.R
import br.com.fiap.letssign.model.Document
import br.com.fiap.letssign.model.ValidationIssue

/** Business rules checked before a document is sent. */
object DocumentValidator {

    fun validate(document: Document, hasPdf: Boolean): List<ValidationIssue> = buildList {
        if (document.title.isBlank()) add(ValidationIssue(R.string.error_document_title_required))
        if (!hasPdf || document.pageCount <= 0) add(ValidationIssue(R.string.error_document_pdf_required))
        if (document.signers.isEmpty()) add(ValidationIssue(R.string.error_document_signer_required))
        if (document.signers.none { it.required }) add(ValidationIssue(R.string.error_document_required_signer))
        if (document.signers.map { it.userId }.toSet().size != document.signers.size) {
            add(ValidationIssue(R.string.error_document_duplicated_signer))
        }

        val signerIds = document.signers.map { it.id }.toSet()
        if (document.fields.any { it.signerId !in signerIds }) add(ValidationIssue(R.string.error_document_field_without_signer))
        if (document.fields.any { !it.isInsidePage || it.page !in 0 until document.pageCount }) {
            add(ValidationIssue(R.string.error_document_field_outside_page))
        }
        document.signers.filter { s -> document.fields.none { it.signerId == s.id } }.forEach {
            add(ValidationIssue(R.string.error_document_signer_without_field, listOf(it.name)))
        }
    }
}
