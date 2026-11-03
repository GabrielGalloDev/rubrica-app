package br.com.fiap.letssign.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import br.com.fiap.letssign.database.entity.DocumentEntity
import br.com.fiap.letssign.database.entity.DocumentEventEntity
import br.com.fiap.letssign.database.entity.DocumentSignerEntity
import br.com.fiap.letssign.database.entity.DocumentWithDetails
import br.com.fiap.letssign.database.entity.SignatureFieldEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Transaction
    @Query("SELECT * FROM documents WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun observeByOwner(ownerId: String): Flow<List<DocumentWithDetails>>

    /** Received documents: only sent ones (never drafts) where the user is a participant. */
    @Transaction
    @Query(
        """SELECT * FROM documents WHERE status != 'DRAFT' AND id IN
           (SELECT documentId FROM document_signers WHERE userId = :userId)
           ORDER BY sentAt DESC"""
    )
    fun observeReceivedBy(userId: String): Flow<List<DocumentWithDetails>>

    @Transaction
    @Query("SELECT * FROM documents WHERE id = :id")
    fun observeById(id: String): Flow<DocumentWithDetails?>

    @Transaction
    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun findById(id: String): DocumentWithDetails?

    @Upsert
    suspend fun upsertDocument(document: DocumentEntity)

    @Upsert
    suspend fun upsertSigners(signers: List<DocumentSignerEntity>)

    @Upsert
    suspend fun upsertFields(fields: List<SignatureFieldEntity>)

    @Upsert
    suspend fun upsertEvents(events: List<DocumentEventEntity>)

    @Query("DELETE FROM document_signers WHERE documentId = :documentId")
    suspend fun deleteSigners(documentId: String)

    @Query("DELETE FROM signature_fields WHERE documentId = :documentId")
    suspend fun deleteFields(documentId: String)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocument(id: String)

    @Query("SELECT signatureImagePath FROM document_signers WHERE id = :signerId")
    suspend fun signatureImagePath(signerId: String): String?

    /** Fully replaces a document's data (edited draft or remote sync). */
    @Transaction
    suspend fun replaceAll(
        document: DocumentEntity,
        signers: List<DocumentSignerEntity>,
        fields: List<SignatureFieldEntity>,
        events: List<DocumentEventEntity>,
    ) {
        upsertDocument(document)
        deleteSigners(document.id)
        deleteFields(document.id)
        upsertSigners(signers)
        upsertFields(fields)
        upsertEvents(events)
    }
}
