package br.com.fiap.letssign.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class DocumentWithDetails(
    @Embedded val document: DocumentEntity,
    @Relation(parentColumn = "id", entityColumn = "documentId")
    val signers: List<DocumentSignerEntity>,
    @Relation(parentColumn = "id", entityColumn = "documentId")
    val fields: List<SignatureFieldEntity>,
    @Relation(parentColumn = "id", entityColumn = "documentId")
    val events: List<DocumentEventEntity>,
)
