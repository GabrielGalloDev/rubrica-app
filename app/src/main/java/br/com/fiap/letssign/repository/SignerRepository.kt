package br.com.fiap.letssign.repository

import br.com.fiap.letssign.database.dao.UserDao
import br.com.fiap.letssign.database.toDomain
import br.com.fiap.letssign.database.toEntity
import br.com.fiap.letssign.model.AppError
import br.com.fiap.letssign.model.User
import br.com.fiap.letssign.model.UserRole
import br.com.fiap.letssign.utils.PasswordHasher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/** Signer registration. Each admin only sees the signers they registered. */
class SignerRepository(
    private val userDao: UserDao,
    private val remote: RemoteDataSource?,
) {
    /** In Firebase mode an admin can't change another account's password, only send a reset link. */
    val canChangePassword: Boolean get() = remote == null

    fun observeSigners(adminId: String): Flow<List<User>> =
        userDao.observeSignersOf(adminId).map { list -> list.map { it.toDomain() } }

    suspend fun refresh(adminId: String) {
        remote?.fetchSignersOf(adminId)?.forEach { user ->
            val existing = userDao.findById(user.id)
            userDao.upsert(user.toEntity(existing?.passwordHash))
        }
    }

    suspend fun find(id: String): User? = userDao.findById(id)?.toDomain()

    suspend fun create(admin: User, name: String, email: String, phone: String, password: String): Result<User> = runCatching {
        val normalized = email.trim().lowercase()
        if (userDao.findByEmail(normalized) != null) throw AppError.EmailAlreadyInUse
        val user = remote?.createSigner(admin, name.trim(), normalized, phone.trim(), password)
            ?: User(UUID.randomUUID().toString(), name.trim(), normalized, phone.trim(), UserRole.SIGNER, admin.id, System.currentTimeMillis())
        userDao.upsert(user.toEntity(if (remote == null) PasswordHasher.hash(password) else null))
        user
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        val remote = remote ?: throw AppError.RemoteUnavailable
        remote.sendPasswordReset(email)
    }

    suspend fun update(user: User, newPassword: String?): Result<User> = runCatching {
        val existing = userDao.findById(user.id) ?: throw AppError.UserNotFound
        val updated = user.copy(name = user.name.trim(), phone = user.phone.trim())
        remote?.updateUser(updated)
        val hash = if (!newPassword.isNullOrBlank() && remote == null) PasswordHasher.hash(newPassword) else existing.passwordHash
        userDao.upsert(updated.toEntity(hash))
        updated
    }
}
