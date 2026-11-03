package br.com.fiap.letssign.firebase

import br.com.fiap.letssign.firebase.DatabaseSchema.DocumentFields
import br.com.fiap.letssign.firebase.DatabaseSchema.EventFields
import br.com.fiap.letssign.firebase.DatabaseSchema.FieldFields
import br.com.fiap.letssign.firebase.DatabaseSchema.SignerFields
import br.com.fiap.letssign.firebase.DatabaseSchema.UserFields
import br.com.fiap.letssign.model.Document
import br.com.fiap.letssign.model.DocumentEvent
import br.com.fiap.letssign.model.DocumentSigner
import br.com.fiap.letssign.model.DocumentStatus
import br.com.fiap.letssign.model.EventType
import br.com.fiap.letssign.model.SignatureField
import br.com.fiap.letssign.model.SignerRole
import br.com.fiap.letssign.model.SignerStatus
import br.com.fiap.letssign.model.User
import br.com.fiap.letssign.model.UserRole
import com.google.firebase.database.DataSnapshot

// Manual mapping: avoids reflection and empty constructors in the models.

internal fun User.toMap(): Map<String, Any?> = mapOf(
    UserFields.ID to id,
    UserFields.NAME to name,
    UserFields.EMAIL to email,
    UserFields.PHONE to phone,
    UserFields.ROLE to role.name,
    UserFields.CREATED_BY to createdBy,
    UserFields.CREATED_AT to createdAt,
)

internal fun Map<String, Any?>.toUser() = User(
    id = str(UserFields.ID),
    name = str(UserFields.NAME),
    email = str(UserFields.EMAIL),
    phone = str(UserFields.PHONE),
    role = UserRole.valueOf(str(UserFields.ROLE)),
    createdBy = this[UserFields.CREATED_BY] as String?,
    createdAt = long(UserFields.CREATED_AT) ?: 0L,
)

internal fun Document.toMap(): Map<String, Any?> = mapOf(
    DocumentFields.ID to id,
    DocumentFields.OWNER_ID to ownerId,
    DocumentFields.OWNER_NAME to ownerName,
    DocumentFields.TITLE to title,
    DocumentFields.MESSAGE to message,
    DocumentFields.VERSION to version,
    DocumentFields.PARENT_ID to parentId,
    DocumentFields.STATUS to status.name,
    DocumentFields.FILE_NAME to fileName,
    DocumentFields.PAGE_COUNT to pageCount,
    DocumentFields.CREATED_AT to createdAt,
    DocumentFields.SENT_AT to sentAt,
    DocumentFields.COMPLETED_AT to completedAt,
    // {uid: true} map so the security rules can check participation.
    DocumentFields.SIGNER_USER_IDS to signers.associate { it.userId to true },
    DocumentFields.SIGNERS to signers.map { it.toMap() },
    DocumentFields.FIELDS to fields.map { it.toMap() },
    DocumentFields.EVENTS to events.map { it.toMap() },
)

internal fun Map<String, Any?>.toDocument() = Document(
    id = str(DocumentFields.ID),
    ownerId = str(DocumentFields.OWNER_ID),
    ownerName = str(DocumentFields.OWNER_NAME),
    title = str(DocumentFields.TITLE),
    message = str(DocumentFields.MESSAGE),
    version = long(DocumentFields.VERSION)?.toInt() ?: 1,
    parentId = this[DocumentFields.PARENT_ID] as String?,
    status = DocumentStatus.valueOf(str(DocumentFields.STATUS)),
    fileName = str(DocumentFields.FILE_NAME),
    pageCount = long(DocumentFields.PAGE_COUNT)?.toInt() ?: 0,
    localPdfPath = null,
    finalPdfPath = null,
    createdAt = long(DocumentFields.CREATED_AT) ?: 0L,
    sentAt = long(DocumentFields.SENT_AT),
    completedAt = long(DocumentFields.COMPLETED_AT),
    signers = this[DocumentFields.SIGNERS].asMapList().map { it.toSigner() },
    fields = this[DocumentFields.FIELDS].asMapList().map { it.toField() },
    events = this[DocumentFields.EVENTS].asMapList().map { it.toEvent() },
)

private fun DocumentSigner.toMap(): Map<String, Any?> = mapOf(
    SignerFields.ID to id,
    SignerFields.USER_ID to userId,
    SignerFields.NAME to name,
    SignerFields.EMAIL to email,
    SignerFields.PHONE to phone,
    SignerFields.ROLE to role.name,
    SignerFields.REQUIRED to required,
    SignerFields.ORDER to order,
    SignerFields.STATUS to status.name,
    SignerFields.SIGNED_AT to signedAt,
)

private fun Map<String, Any?>.toSigner() = DocumentSigner(
    id = str(SignerFields.ID),
    userId = str(SignerFields.USER_ID),
    name = str(SignerFields.NAME),
    email = str(SignerFields.EMAIL),
    phone = str(SignerFields.PHONE),
    role = signerRoleOf(str(SignerFields.ROLE)),
    required = this[SignerFields.REQUIRED] as? Boolean ?: true,
    order = long(SignerFields.ORDER)?.toInt() ?: 0,
    status = SignerStatus.valueOf(str(SignerFields.STATUS)),
    signedAt = long(SignerFields.SIGNED_AT),
)

/** Accepts the Portuguese names written by the first version of the app. */
private fun signerRoleOf(value: String): SignerRole = when (value) {
    "CONTRATANTE" -> SignerRole.CONTRACTOR
    "CONTRATADO" -> SignerRole.CONTRACTED
    "TESTEMUNHA" -> SignerRole.WITNESS
    else -> SignerRole.valueOf(value)
}

private fun SignatureField.toMap(): Map<String, Any?> = mapOf(
    FieldFields.ID to id,
    FieldFields.SIGNER_ID to signerId,
    FieldFields.PAGE to page,
    FieldFields.X to x.toDouble(),
    FieldFields.Y to y.toDouble(),
    FieldFields.WIDTH to width.toDouble(),
    FieldFields.HEIGHT to height.toDouble(),
)

private fun Map<String, Any?>.toField() = SignatureField(
    id = str(FieldFields.ID),
    signerId = str(FieldFields.SIGNER_ID),
    page = long(FieldFields.PAGE)?.toInt() ?: 0,
    x = float(FieldFields.X),
    y = float(FieldFields.Y),
    width = float(FieldFields.WIDTH),
    height = float(FieldFields.HEIGHT),
)

internal fun DocumentEvent.toMap(): Map<String, Any?> = mapOf(
    EventFields.ID to id,
    EventFields.TYPE to type.name,
    EventFields.ACTOR_NAME to actorName,
    EventFields.TIMESTAMP to timestamp,
    EventFields.DESCRIPTION to description,
)

private fun Map<String, Any?>.toEvent() = DocumentEvent(
    id = str(EventFields.ID),
    type = EventType.valueOf(str(EventFields.TYPE)),
    actorName = str(EventFields.ACTOR_NAME),
    timestamp = long(EventFields.TIMESTAMP) ?: 0L,
    description = str(EventFields.DESCRIPTION),
)

@Suppress("UNCHECKED_CAST")
internal fun DataSnapshot.asMap() = value as? Map<String, Any?>

/** The Realtime Database returns lists either as a List or as a Map with numeric keys. */
@Suppress("UNCHECKED_CAST")
internal fun Any?.asMapList(): List<Map<String, Any?>> = when (this) {
    is List<*> -> filterIsInstance<Map<String, Any?>>()
    is Map<*, *> -> entries.sortedBy { (it.key as? String)?.toIntOrNull() ?: Int.MAX_VALUE }
        .mapNotNull { it.value as? Map<String, Any?> }
    else -> emptyList()
}

private fun Map<String, Any?>.str(key: String) = this[key] as? String ?: ""
private fun Map<String, Any?>.long(key: String) = (this[key] as? Number)?.toLong()
private fun Map<String, Any?>.float(key: String) = (this[key] as? Number)?.toFloat() ?: 0f
