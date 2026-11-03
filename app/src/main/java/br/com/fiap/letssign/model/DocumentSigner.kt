package br.com.fiap.letssign.model

enum class SignerRole { CONTRACTOR, CONTRACTED, WITNESS }

enum class SignerStatus { PENDING, SIGNED }

/** A participant of a specific document (snapshot of the signer at the time it was sent). */
data class DocumentSigner(
    val id: String,
    val userId: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: SignerRole,
    val required: Boolean = true,
    val order: Int,
    val status: SignerStatus = SignerStatus.PENDING,
    val signedAt: Long? = null,
)
