package br.com.fiap.letssign.repository

import br.com.fiap.letssign.database.dao.UserDao
import br.com.fiap.letssign.database.entity.UserEntity
import br.com.fiap.letssign.database.toDomain
import br.com.fiap.letssign.database.toEntity
import br.com.fiap.letssign.model.AppError
import br.com.fiap.letssign.model.User
import br.com.fiap.letssign.model.UserRole
import br.com.fiap.letssign.utils.PasswordHasher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AuthRepository(
    private val userDao: UserDao,
    private val remote: RemoteDataSource?,
    private val preferences: AppPreferences,
) {
    private val currentUserState = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = currentUserState.asStateFlow()

    val isRemoteEnabled: Boolean get() = remote != null

    suspend fun restoreSession(): User? {
        val id = preferences.currentUserId ?: return null
        val user = userDao.findById(id)?.toDomain()
        currentUserState.value = user
        return user
    }

    suspend fun login(email: String, password: String): Result<User> = runCatching {
        val normalized = email.trim().lowercase()
        val user = if (remote != null) {
            try {
                val uid = remote.signIn(normalized, password)
                val profile = remote.fetchUser(uid) ?: throw AppError.ProfileNotFound
                // Keeps a local hash so the same device can sign in offline.
                userDao.upsert(profile.toEntity(PasswordHasher.hash(password)))
                profile
            } catch (error: AppError.Network) {
                loginLocally(normalized, password) ?: throw error
            } catch (error: AppError.UserNotFound) {
                throw AppError.InvalidCredentials
            }
        } else {
            loginLocally(normalized, password) ?: throw AppError.InvalidCredentials
        }
        startSession(user)
        user
    }

    private suspend fun loginLocally(email: String, password: String): User? {
        val entity = userDao.findByEmail(email) ?: return null
        val hash = entity.passwordHash ?: return null
        return if (PasswordHasher.verify(password, hash)) entity.toDomain() else null
    }

    suspend fun registerAdmin(name: String, email: String, phone: String, password: String): Result<User> = runCatching {
        val normalized = email.trim().lowercase()
        if (userDao.findByEmail(normalized) != null) throw AppError.EmailAlreadyInUse
        val user = remote?.registerAdmin(name.trim(), normalized, phone.trim(), password)
            ?: User(UUID.randomUUID().toString(), name.trim(), normalized, phone.trim(), UserRole.ADMIN, null, System.currentTimeMillis())
        userDao.upsert(user.toEntity(PasswordHasher.hash(password)))
        startSession(user)
        user
    }

    /** Sends the Firebase Auth password reset e-mail. */
    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        val remote = remote ?: throw AppError.RemoteUnavailable
        remote.sendPasswordReset(email.trim().lowercase())
    }

    suspend fun logout() {
        runCatching { remote?.signOut() }
        preferences.currentUserId = null
        currentUserState.value = null
    }

    /** Creates a demo admin in local mode to make the first access easier. */
    suspend fun seedDemoAdminIfNeeded(demoName: String) {
        if (remote != null || userDao.count() > 0) return
        userDao.upsert(
            UserEntity(
                id = UUID.randomUUID().toString(),
                name = demoName,
                email = DEMO_ADMIN_EMAIL,
                phone = DEMO_ADMIN_PHONE,
                role = UserRole.ADMIN,
                passwordHash = PasswordHasher.hash(DEMO_ADMIN_PASSWORD),
                createdBy = null,
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    private fun startSession(user: User) {
        preferences.currentUserId = user.id
        currentUserState.value = user
    }

    companion object {
        const val DEMO_ADMIN_EMAIL = "admin@letssign.com"
        const val DEMO_ADMIN_PASSWORD = "admin123"
        private const val DEMO_ADMIN_PHONE = "(11) 99999-0000"
    }
}
