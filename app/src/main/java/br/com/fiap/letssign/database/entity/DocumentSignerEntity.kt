package br.com.fiap.letssign.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.fiap.letssign.model.SignerRole
import br.com.fiap.letssign.model.SignerStatus

@Entity(
    tableName = "document_signers",
    foreignKeys = [ForeignKey(
        entity = DocumentEntity::class,
        parentColumns = ["id"],
        childColumns = ["documentId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("documentId"), Index("userId")],
)
data class DocumentSignerEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val userId: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: SignerRole,
    val required: Boolean,
    val signOrder: Int,
    val status: SignerStatus,
    val signedAt: Long?,
    /** Captured signature image (PNG) on the device. */
    val signatureImagePath: String?,
)
