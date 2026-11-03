package br.com.fiap.letssign.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.fiap.letssign.model.EventType

@Entity(
    tableName = "document_events",
    foreignKeys = [ForeignKey(
        entity = DocumentEntity::class,
        parentColumns = ["id"],
        childColumns = ["documentId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("documentId")],
)
data class DocumentEventEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val type: EventType,
    val actorName: String,
    val timestamp: Long,
    val description: String,
)
