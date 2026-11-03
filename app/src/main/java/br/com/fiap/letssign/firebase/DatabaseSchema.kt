package br.com.fiap.letssign.firebase

/** Realtime Database paths and field names (see database.rules.json). */
object DatabaseSchema {
    const val USERS = "users"
    const val DOCUMENTS = "documents"
    const val USER_DOCUMENTS = "userDocuments"
    const val FILES = "files"
    const val EVENTS = "events"

    object UserFields {
        const val ID = "id"
        const val NAME = "name"
        const val EMAIL = "email"
        const val PHONE = "phone"
        const val ROLE = "role"
        const val CREATED_BY = "createdBy"
        const val CREATED_AT = "createdAt"
    }

    object DocumentFields {
        const val ID = "id"
        const val OWNER_ID = "ownerId"
        const val OWNER_NAME = "ownerName"
        const val TITLE = "title"
        const val MESSAGE = "message"
        const val VERSION = "version"
        const val PARENT_ID = "parentId"
        const val STATUS = "status"
        const val FILE_NAME = "fileName"
        const val PAGE_COUNT = "pageCount"
        const val CREATED_AT = "createdAt"
        const val SENT_AT = "sentAt"
        const val COMPLETED_AT = "completedAt"
        const val SIGNER_USER_IDS = "signerUserIds"
        const val SIGNERS = "signers"
        const val FIELDS = "fields"
        const val EVENTS = "events"
    }

    object SignerFields {
        const val ID = "id"
        const val USER_ID = "userId"
        const val NAME = "name"
        const val EMAIL = "email"
        const val PHONE = "phone"
        const val ROLE = "role"
        const val REQUIRED = "required"
        const val ORDER = "order"
        const val STATUS = "status"
        const val SIGNED_AT = "signedAt"
    }

    object FieldFields {
        const val ID = "id"
        const val SIGNER_ID = "signerId"
        const val PAGE = "page"
        const val X = "x"
        const val Y = "y"
        const val WIDTH = "width"
        const val HEIGHT = "height"
    }

    object EventFields {
        const val ID = "id"
        const val TYPE = "type"
        const val ACTOR_NAME = "actorName"
        const val TIMESTAMP = "timestamp"
        const val DESCRIPTION = "description"
    }

    /** Database keys can't contain ".", so "documents/x/original.pdf" becomes "files/x/original_pdf". */
    fun fileKey(remotePath: String): String = remotePath.removePrefix("$DOCUMENTS/").replace('.', '_')
}
