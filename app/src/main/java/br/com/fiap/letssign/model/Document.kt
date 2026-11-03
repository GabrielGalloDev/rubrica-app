package br.com.fiap.letssign.model

enum class DocumentStatus { DRAFT, SENT, PARTIALLY_SIGNED, COMPLETED }

data class Document(
    val id: String,
    val ownerId: String,
    val ownerName: String,
    val title: String,
    val message: String,
    val version: Int,
    val parentId: String?,
    val status: DocumentStatus,
    val fileName: String,
    val pageCount: Int,
    val localPdfPath: String?,
    val finalPdfPath: String?,
    val createdAt: Long,
    val sentAt: Long?,
    val completedAt: Long?,
    val signers: List<DocumentSigner> = emptyList(),
    val fields: List<SignatureField> = emptyList(),
    val events: List<DocumentEvent> = emptyList(),
) {
    val signedCount: Int get() = signers.count { it.status == SignerStatus.SIGNED }

    fun signerForUser(userId: String): DocumentSigner? = signers.firstOrNull { it.userId == userId }

    /** Business rule: the document only completes when every required signer has signed. */
    fun computeStatus(): DocumentStatus = when {
        status == DocumentStatus.DRAFT -> DocumentStatus.DRAFT
        signers.isNotEmpty() && signers.filter { it.required }.all { it.status == SignerStatus.SIGNED } ->
            DocumentStatus.COMPLETED
        signers.any { it.status == SignerStatus.SIGNED } -> DocumentStatus.PARTIALLY_SIGNED
        else -> DocumentStatus.SENT
    }
}
