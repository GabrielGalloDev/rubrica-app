package br.com.fiap.letssign.database

import br.com.fiap.letssign.database.entity.DocumentEntity
import br.com.fiap.letssign.database.entity.DocumentEventEntity
import br.com.fiap.letssign.database.entity.DocumentSignerEntity
import br.com.fiap.letssign.database.entity.DocumentWithDetails
import br.com.fiap.letssign.database.entity.SignatureFieldEntity
import br.com.fiap.letssign.database.entity.UserEntity
import br.com.fiap.letssign.model.Document
import br.com.fiap.letssign.model.DocumentEvent
import br.com.fiap.letssign.model.DocumentSigner
import br.com.fiap.letssign.model.SignatureField
import br.com.fiap.letssign.model.User

fun UserEntity.toDomain() = User(id, name, email, phone, role, createdBy, createdAt)

fun User.toEntity(passwordHash: String?) =
    UserEntity(id, name, email, phone, role, passwordHash, createdBy, createdAt)

fun DocumentWithDetails.toDomain() = Document(
    id = document.id,
    ownerId = document.ownerId,
    ownerName = document.ownerName,
    title = document.title,
    message = document.message,
    version = document.version,
    parentId = document.parentId,
    status = document.status,
    fileName = document.fileName,
    pageCount = document.pageCount,
    localPdfPath = document.localPdfPath,
    finalPdfPath = document.finalPdfPath,
    createdAt = document.createdAt,
    sentAt = document.sentAt,
    completedAt = document.completedAt,
    signers = signers.sortedBy { it.signOrder }.map { it.toDomain() },
    fields = fields.sortedWith(compareBy({ it.page }, { it.y })).map { it.toDomain() },
    events = events.sortedBy { it.timestamp }.map { it.toDomain() },
)

fun DocumentSignerEntity.toDomain() =
    DocumentSigner(id, userId, name, email, phone, role, required, signOrder, status, signedAt)

fun SignatureFieldEntity.toDomain() = SignatureField(id, signerId, page, x, y, width, height)

fun DocumentEventEntity.toDomain() = DocumentEvent(id, type, actorName, timestamp, description)

fun Document.toEntity() = DocumentEntity(
    id, ownerId, ownerName, title, message, version, parentId, status, fileName, pageCount,
    localPdfPath, finalPdfPath, createdAt, sentAt, completedAt,
)

fun DocumentSigner.toEntity(documentId: String, signatureImagePath: String? = null) = DocumentSignerEntity(
    id, documentId, userId, name, email, phone, role, required, order, status, signedAt, signatureImagePath,
)

fun SignatureField.toEntity(documentId: String) =
    SignatureFieldEntity(id, documentId, signerId, page, x, y, width, height)

fun DocumentEvent.toEntity(documentId: String) =
    DocumentEventEntity(id, documentId, type, actorName, timestamp, description)
