package br.com.fiap.letssign.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.fiap.letssign.model.DocumentStatus

@Entity(tableName = "documents", indices = [Index("ownerId")])
data class DocumentEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val ownerName: String,
    val title: String,
    val message: String,
    val version: Int,
    val parentId: String?,
    val status: DocumentStatus,
    val fileName: String,
    val pageCount: Int,
    /** Path of the original PDF in the device file system. */
    val localPdfPath: String?,
    /** Path of the final PDF (stamped signatures plus audit trail). */
    val finalPdfPath: String?,
    val createdAt: Long,
    val sentAt: Long?,
    val completedAt: Long?,
)
