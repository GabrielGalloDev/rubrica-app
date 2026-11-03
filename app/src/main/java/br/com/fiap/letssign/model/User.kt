package br.com.fiap.letssign.model

enum class UserRole { ADMIN, SIGNER }

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val createdBy: String?,
    val createdAt: Long,
)
