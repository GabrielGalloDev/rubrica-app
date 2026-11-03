package br.com.fiap.letssign.repository

import br.com.fiap.letssign.model.Document
import br.com.fiap.letssign.model.DocumentEvent
import br.com.fiap.letssign.model.User
import java.io.File

/**
 * Remote persistence contract. Repositories only know this interface, so the Firebase
 * implementation can be swapped for a REST API (e.g. Retrofit) without touching the screens
 * or the business rules. Implementations throw [br.com.fiap.letssign.model.AppError].
 */
interface RemoteDataSource {
    // ---- Authentication / users ----
    suspend fun signIn(email: String, password: String): String
    suspend fun signOut()
    suspend fun registerAdmin(name: String, email: String, phone: String, password: String): User
    suspend fun createSigner(admin: User, name: String, email: String, phone: String, password: String): User
    suspend fun updateUser(user: User)
    suspend fun fetchUser(id: String): User?
    suspend fun fetchSignersOf(adminId: String): List<User>
    suspend fun sendPasswordReset(email: String)

    // ---- Files ----
    suspend fun uploadFile(remotePath: String, file: File)
    suspend fun downloadFile(remotePath: String, destination: File)

    // ---- Documents ----
    /** Writes the document with its client-side id: repeating the call never duplicates anything. */
    suspend fun pushDocument(document: Document)
    suspend fun fetchDocumentsOwnedBy(userId: String): List<Document>
    suspend fun fetchDocumentsReceivedBy(userId: String): List<Document>
    suspend fun fetchDocument(id: String): Document?
    suspend fun appendEvent(documentId: String, event: DocumentEvent)

    /** Atomically marks the signature, recomputes the status and returns the updated document. */
    suspend fun markSigned(documentId: String, signerId: String, signedAt: Long, actorName: String): Document
}

object RemotePaths {
    fun original(documentId: String) = "documents/$documentId/original.pdf"
    fun signature(documentId: String, signerId: String) = "documents/$documentId/signatures/$signerId.png"
    fun final(documentId: String) = "documents/$documentId/final.pdf"
}
