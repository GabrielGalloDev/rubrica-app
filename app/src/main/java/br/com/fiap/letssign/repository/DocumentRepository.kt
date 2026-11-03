package br.com.fiap.letssign.repository

import android.net.Uri
import br.com.fiap.letssign.database.dao.DocumentDao
import br.com.fiap.letssign.database.toDomain
import br.com.fiap.letssign.database.toEntity
import br.com.fiap.letssign.model.AppError
import br.com.fiap.letssign.model.Document
import br.com.fiap.letssign.model.DocumentEvent
import br.com.fiap.letssign.model.DocumentStatus
import br.com.fiap.letssign.model.EventType
import br.com.fiap.letssign.model.SignerStatus
import br.com.fiap.letssign.model.User
import br.com.fiap.letssign.utils.DocumentValidator
import br.com.fiap.letssign.utils.PdfStamper
import br.com.fiap.letssign.utils.PdfValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class DocumentRepository(
    private val dao: DocumentDao,
    private val remote: RemoteDataSource?,
    private val files: FileStorage,
    private val stamper: PdfStamper,
) {
    // ---------------- Queries ----------------

    fun observeOwnedBy(ownerId: String): Flow<List<Document>> =
        dao.observeByOwner(ownerId).map { list -> list.map { it.toDomain() } }

    fun observeReceivedBy(userId: String): Flow<List<Document>> =
        dao.observeReceivedBy(userId).map { list -> list.map { it.toDomain() } }

    fun observe(id: String): Flow<Document?> = dao.observeById(id).map { it?.toDomain() }

    suspend fun find(id: String): Document? = dao.findById(id)?.toDomain()

    suspend fun refreshOwnedBy(ownerId: String) {
        remote?.fetchDocumentsOwnedBy(ownerId)?.forEach { saveFromRemote(it) }
    }

    suspend fun refreshReceivedBy(userId: String) {
        remote?.fetchDocumentsReceivedBy(userId)?.forEach { saveFromRemote(it) }
    }

    suspend fun refresh(id: String) {
        remote?.fetchDocument(id)?.let { saveFromRemote(it) }
    }

    /** Stores a document coming from the server, keeping the local file paths. */
    private suspend fun saveFromRemote(document: Document) {
        val existing = dao.findById(document.id)
        val merged = document.copy(
            localPdfPath = existing?.document?.localPdfPath?.takeIf { File(it).exists() },
            finalPdfPath = existing?.document?.finalPdfPath
                ?.takeIf { File(it).exists() && document.status == DocumentStatus.COMPLETED },
        )
        val localImages = existing?.signers?.associate { it.id to it.signatureImagePath }.orEmpty()
        dao.replaceAll(
            merged.toEntity(),
            merged.signers.map { it.toEntity(merged.id, localImages[it.id]) },
            merged.fields.map { it.toEntity(merged.id) },
            merged.events.map { it.toEntity(merged.id) },
        )
    }

    // ---------------- Draft ----------------

    /** Copies the chosen PDF into the app storage and validates it. Returns the page count. */
    suspend fun importPdf(documentId: String, uri: Uri): Int = withContext(Dispatchers.IO) {
        val temp = files.importTemp(documentId)
        try {
            files.copyFromUri(uri, temp)
            val pages = PdfValidator.validate(temp)
            val target = files.originalPdf(documentId)
            target.delete()
            temp.renameTo(target)
            pages
        } finally {
            temp.delete()
        }
    }

    fun originalFile(documentId: String): File = files.originalPdf(documentId)

    suspend fun saveDraft(document: Document) {
        if (document.status != DocumentStatus.DRAFT) throw AppError.DocumentLocked
        val existing = dao.findById(document.id)?.toDomain()
        val events = existing?.events ?: document.events
        dao.replaceAll(
            document.toEntity(),
            document.signers.map { it.toEntity(document.id) },
            document.fields.map { it.toEntity(document.id) },
            events.map { it.toEntity(document.id) },
        )
    }

    suspend fun deleteDraft(id: String) {
        val doc = dao.findById(id) ?: return
        if (doc.document.status != DocumentStatus.DRAFT) throw AppError.DocumentLocked
        dao.deleteDocument(id)
        files.deleteDocument(id)
    }

    /**
     * Sent documents are immutable. Changing one creates a new version (new id and new flow),
     * copying the PDF, signers and fields with every signature reset.
     */
    suspend fun createNewVersion(sourceId: String, admin: User): Document = withContext(Dispatchers.IO) {
        val source = find(sourceId) ?: throw AppError.DocumentNotFound
        val sourcePdf = ensureOriginalPdf(source)
        val newId = UUID.randomUUID().toString()
        sourcePdf.copyTo(files.originalPdf(newId), overwrite = true)

        val signerIdMap = source.signers.associate { it.id to UUID.randomUUID().toString() }
        val now = System.currentTimeMillis()
        val draft = source.copy(
            id = newId,
            version = source.version + 1,
            parentId = source.id,
            status = DocumentStatus.DRAFT,
            localPdfPath = files.originalPdf(newId).path,
            finalPdfPath = null,
            createdAt = now,
            sentAt = null,
            completedAt = null,
            signers = source.signers.map {
                it.copy(id = signerIdMap.getValue(it.id), status = SignerStatus.PENDING, signedAt = null)
            },
            fields = source.fields.map {
                it.copy(id = UUID.randomUUID().toString(), signerId = signerIdMap.getValue(it.signerId))
            },
            events = listOf(DocumentEvent(UUID.randomUUID().toString(), EventType.NEW_VERSION, admin.name, now)),
        )
        dao.replaceAll(
            draft.toEntity(),
            draft.signers.map { it.toEntity(newId) },
            draft.fields.map { it.toEntity(newId) },
            draft.events.map { it.toEntity(newId) },
        )
        draft
    }

    // ---------------- Sending ----------------

    /**
     * Validates and sends the document. The ids (document and "sent" event) are stable, so a
     * retry after a failure overwrites the same record without duplicating requests.
     */
    suspend fun send(documentId: String, actor: User): Document = withContext(Dispatchers.IO) {
        val draft = find(documentId) ?: throw AppError.DocumentNotFound
        if (draft.status != DocumentStatus.DRAFT) return@withContext draft // already sent

        val issues = DocumentValidator.validate(draft, originalFile(documentId).exists())
        if (issues.isNotEmpty()) throw AppError.Validation(issues)

        val now = System.currentTimeMillis()
        val sent = draft.copy(
            status = DocumentStatus.SENT,
            sentAt = now,
            events = draft.events.filterNot { it.type == EventType.SENT } +
                DocumentEvent("$documentId-sent", EventType.SENT, actor.name, now),
        )
        remote?.let {
            it.uploadFile(RemotePaths.original(documentId), originalFile(documentId))
            it.pushDocument(sent)
        }
        dao.replaceAll(
            sent.toEntity(),
            sent.signers.map { s -> s.toEntity(documentId) },
            sent.fields.map { f -> f.toEntity(documentId) },
            sent.events.map { e -> e.toEntity(documentId) },
        )
        sent
    }

    // ---------------- Signing ----------------

    suspend fun registerView(document: Document, viewer: User) {
        val signer = document.signerForUser(viewer.id) ?: return
        val eventId = "${document.id}-viewed-${signer.id}"
        val alreadyViewed = document.events.any { it.id == eventId }
        if (alreadyViewed || signer.status == SignerStatus.SIGNED) return
        val event = DocumentEvent(eventId, EventType.VIEWED, viewer.name, System.currentTimeMillis())
        runCatching { remote?.appendEvent(document.id, event) }
        dao.upsertEvents(listOf(event.toEntity(document.id)))
    }

    /** Saves the signature, marks the signer as signed and, if it was the last one, builds the final PDF. */
    suspend fun sign(documentId: String, signer: User, signaturePng: ByteArray): Document = withContext(Dispatchers.IO) {
        val document = find(documentId) ?: throw AppError.DocumentNotFound
        val participant = document.signerForUser(signer.id) ?: throw AppError.AccessDenied
        if (participant.status == SignerStatus.SIGNED) return@withContext document

        val imageFile = files.signatureImage(documentId, participant.id)
        imageFile.writeBytes(signaturePng)
        val now = System.currentTimeMillis()

        val updated = if (remote != null) {
            remote.uploadFile(RemotePaths.signature(documentId, participant.id), imageFile)
            remote.markSigned(documentId, participant.id, now, signer.name)
        } else {
            signLocally(document, participant.id, signer.name, now)
        }
        val withPaths = updated.copy(localPdfPath = document.localPdfPath, finalPdfPath = document.finalPdfPath)
        dao.replaceAll(
            withPaths.toEntity(),
            withPaths.signers.map { s ->
                s.toEntity(documentId, if (s.id == participant.id) imageFile.path else dao.signatureImagePath(s.id))
            },
            withPaths.fields.map { it.toEntity(documentId) },
            withPaths.events.map { it.toEntity(documentId) },
        )

        if (withPaths.status == DocumentStatus.COMPLETED) {
            val final = buildSignedPdf(documentId)
            runCatching { remote?.uploadFile(RemotePaths.final(documentId), final) }
        }
        find(documentId) ?: throw AppError.DocumentNotFound
    }

    private fun signLocally(document: Document, signerId: String, actorName: String, now: Long): Document {
        val signers = document.signers.map {
            if (it.id == signerId) it.copy(status = SignerStatus.SIGNED, signedAt = now) else it
        }
        val signed = document.copy(signers = signers)
        val events = signed.events.toMutableList()
        events += DocumentEvent(UUID.randomUUID().toString(), EventType.SIGNED, actorName, now)
        val status = signed.computeStatus()
        val completed = status == DocumentStatus.COMPLETED
        if (completed) events += DocumentEvent(UUID.randomUUID().toString(), EventType.COMPLETED, "", now)
        return signed.copy(status = status, events = events, completedAt = if (completed) now else null)
    }

    // ---------------- Files ----------------

    /** Makes sure the original PDF is on the device (downloading it from the server when needed). */
    suspend fun ensureOriginalPdf(document: Document): File = withContext(Dispatchers.IO) {
        val file = files.originalPdf(document.id)
        if (!file.exists()) {
            remote?.downloadFile(RemotePaths.original(document.id), file) ?: throw AppError.DocumentNotFound
        }
        if (document.localPdfPath != file.path) {
            dao.findById(document.id)?.let { dao.upsertDocument(it.document.copy(localPdfPath = file.path)) }
        }
        file
    }

    /**
     * Builds the PDF with the signatures collected so far. When the document is completed the
     * result is stored as the record's final PDF; otherwise it is a temporary preview.
     */
    suspend fun buildSignedPdf(documentId: String): File = withContext(Dispatchers.IO) {
        val details = dao.findById(documentId) ?: throw AppError.DocumentNotFound
        val document = details.toDomain()
        val original = ensureOriginalPdf(document)
        val images = details.signers.filter { it.status == SignerStatus.SIGNED }.associate { s ->
            val local = s.signatureImagePath?.let(::File)?.takeIf { it.exists() }
                ?: files.signatureImage(documentId, s.id)
            if (!local.exists()) runCatching { remote?.downloadFile(RemotePaths.signature(documentId, s.id), local) }
            s.id to local
        }
        val completed = document.status == DocumentStatus.COMPLETED
        val output = if (completed) files.finalPdf(documentId) else files.previewPdf(documentId)
        stamper.stamp(document, original, images, output)
        if (completed) dao.upsertDocument(details.document.copy(finalPdfPath = output.path, localPdfPath = original.path))
        output
    }
}
