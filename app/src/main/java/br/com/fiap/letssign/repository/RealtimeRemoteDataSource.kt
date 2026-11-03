package br.com.fiap.letssign.repository

import android.content.Context
import android.util.Base64
import br.com.fiap.letssign.firebase.DatabaseSchema
import br.com.fiap.letssign.firebase.DatabaseSchema.DocumentFields
import br.com.fiap.letssign.firebase.DatabaseSchema.EventFields
import br.com.fiap.letssign.firebase.DatabaseSchema.UserFields
import br.com.fiap.letssign.firebase.FirebaseProvider
import br.com.fiap.letssign.firebase.asMap
import br.com.fiap.letssign.firebase.asMapList
import br.com.fiap.letssign.firebase.firebaseCall
import br.com.fiap.letssign.firebase.toAppError
import br.com.fiap.letssign.firebase.toDocument
import br.com.fiap.letssign.firebase.toMap
import br.com.fiap.letssign.firebase.toUser
import br.com.fiap.letssign.model.AppError
import br.com.fiap.letssign.model.Document
import br.com.fiap.letssign.model.DocumentEvent
import br.com.fiap.letssign.model.DocumentStatus
import br.com.fiap.letssign.model.EventType
import br.com.fiap.letssign.model.SignerStatus
import br.com.fiap.letssign.model.User
import br.com.fiap.letssign.model.UserRole
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Firebase implementation:
 *  - Auth: e-mail/password credentials for admins and signers;
 *  - Realtime Database:
 *      users/{uid}                      profile
 *      documents/{docId}                document with embedded signers, fields and events
 *      userDocuments/{uid}/{docId}      index of the documents received by each signer
 *      files/{docId}/...                PDFs and signatures as Base64 (BLOB), no Cloud Storage
 */
class RealtimeRemoteDataSource(
    private val context: Context,
    private val firebase: FirebaseProvider,
) : RemoteDataSource {

    private val db get() = firebase.database
    private val users get() = db.child(DatabaseSchema.USERS)
    private val documents get() = db.child(DatabaseSchema.DOCUMENTS)

    override suspend fun signIn(email: String, password: String): String = firebaseCall {
        firebase.auth.signInWithEmailAndPassword(email, password).await().user?.uid
            ?: throw AppError.InvalidCredentials
    }

    override suspend fun signOut() = firebase.auth.signOut()

    override suspend fun registerAdmin(name: String, email: String, phone: String, password: String): User =
        firebaseCall {
            val uid = firebase.auth.createUserWithEmailAndPassword(email, password).await().user?.uid
                ?: throw AppError.Unexpected()
            val user = User(uid, name, email, phone, UserRole.ADMIN, null, System.currentTimeMillis())
            users.child(uid).setValue(user.toMap()).await()
            user
        }

    /** Uses a secondary Firebase instance so the signed-in admin session is not replaced. */
    override suspend fun createSigner(admin: User, name: String, email: String, phone: String, password: String): User =
        firebaseCall {
            val secondaryAuth = firebase.secondaryAuth(context)
            val uid = try {
                secondaryAuth.createUserWithEmailAndPassword(email, password).await().user?.uid
                    ?: throw AppError.Unexpected()
            } finally {
                secondaryAuth.signOut()
            }
            val user = User(uid, name, email, phone, UserRole.SIGNER, admin.id, System.currentTimeMillis())
            users.child(uid).setValue(user.toMap()).await()
            user
        }

    override suspend fun updateUser(user: User) = firebaseCall {
        users.child(user.id).updateChildren(mapOf(UserFields.NAME to user.name, UserFields.PHONE to user.phone)).await()
        Unit
    }

    override suspend fun fetchUser(id: String): User? = firebaseCall {
        users.child(id).get().await().asMap()?.toUser()
    }

    override suspend fun fetchSignersOf(adminId: String): List<User> = firebaseCall {
        users.orderByChild(UserFields.CREATED_BY).equalTo(adminId).get().await()
            .children.mapNotNull { it.asMap()?.toUser() }
    }

    override suspend fun sendPasswordReset(email: String) = firebaseCall {
        firebase.auth.sendPasswordResetEmail(email).await()
        Unit
    }

    override suspend fun uploadFile(remotePath: String, file: File) = firebaseCall {
        fileRef(remotePath).setValue(Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)).await()
        Unit
    }

    override suspend fun downloadFile(remotePath: String, destination: File) = firebaseCall {
        val encoded = fileRef(remotePath).get().await().getValue(String::class.java)
            ?: throw AppError.DocumentNotFound
        destination.parentFile?.mkdirs()
        destination.writeBytes(Base64.decode(encoded, Base64.NO_WRAP))
    }

    private fun fileRef(remotePath: String) = db.child(DatabaseSchema.FILES).child(DatabaseSchema.fileKey(remotePath))

    /** Writes the document and each signer's index in a single atomic update. */
    override suspend fun pushDocument(document: Document) = firebaseCall {
        val updates = mutableMapOf<String, Any?>("${DatabaseSchema.DOCUMENTS}/${document.id}" to document.toMap())
        document.signers.forEach { updates["${DatabaseSchema.USER_DOCUMENTS}/${it.userId}/${document.id}"] = true }
        db.updateChildren(updates).await()
        Unit
    }

    override suspend fun fetchDocumentsOwnedBy(userId: String): List<Document> = firebaseCall {
        documents.orderByChild(DocumentFields.OWNER_ID).equalTo(userId).get().await()
            .children.mapNotNull { it.asMap()?.toDocument() }
    }

    override suspend fun fetchDocumentsReceivedBy(userId: String): List<Document> = firebaseCall {
        db.child(DatabaseSchema.USER_DOCUMENTS).child(userId).get().await()
            .children.mapNotNull { child -> child.key?.let { fetchDocument(it) } }
    }

    override suspend fun fetchDocument(id: String): Document? = firebaseCall {
        documents.child(id).get().await().asMap()?.toDocument()
    }

    override suspend fun appendEvent(documentId: String, event: DocumentEvent) = firebaseCall {
        documents.child(documentId).child(DatabaseSchema.EVENTS).transact { data ->
            val events = data.value.asMapList()
            if (events.none { it[EventFields.ID] == event.id }) data.value = events + event.toMap()
        }
        Unit
    }

    override suspend fun markSigned(documentId: String, signerId: String, signedAt: Long, actorName: String): Document =
        firebaseCall {
            val snapshot = documents.child(documentId).transact { data ->
                // On the first pass the local cache may be empty; the server retries with the real value.
                @Suppress("UNCHECKED_CAST")
                val current = (data.value as? Map<String, Any?>)?.toDocument() ?: return@transact
                val target = current.signers.firstOrNull { it.id == signerId } ?: return@transact
                if (target.status == SignerStatus.SIGNED) return@transact // idempotent

                val signers = current.signers.map {
                    if (it.id == signerId) it.copy(status = SignerStatus.SIGNED, signedAt = signedAt) else it
                }
                var updated = current.copy(signers = signers)
                val events = current.events.toMutableList()
                events += DocumentEvent(UUID.randomUUID().toString(), EventType.SIGNED, actorName, signedAt)
                val status = updated.computeStatus()
                var completedAt = current.completedAt
                if (status == DocumentStatus.COMPLETED) {
                    completedAt = signedAt
                    events += DocumentEvent(UUID.randomUUID().toString(), EventType.COMPLETED, "", signedAt)
                }
                updated = updated.copy(status = status, completedAt = completedAt, events = events)
                data.value = updated.toMap()
            }
            val document = snapshot.asMap()?.toDocument() ?: throw AppError.DocumentNotFound
            if (document.signers.none { it.id == signerId }) throw AppError.AccessDenied
            document
        }
}

/** Runs a Realtime Database transaction and returns the committed value. */
private suspend fun DatabaseReference.transact(update: (MutableData) -> Unit): DataSnapshot =
    suspendCancellableCoroutine { cont ->
        runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                update(currentData)
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                when {
                    error != null -> cont.resumeWithException(error.toAppError())
                    currentData == null -> cont.resumeWithException(AppError.Unexpected())
                    else -> cont.resume(currentData)
                }
            }
        })
    }
