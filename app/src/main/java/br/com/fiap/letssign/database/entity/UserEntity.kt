package br.com.fiap.letssign.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.fiap.letssign.model.UserRole

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    /** PBKDF2 password hash (offline login). Null when the credential only lives in Firebase Auth. */
    val passwordHash: String?,
    val createdBy: String?,
    val createdAt: Long,
)
