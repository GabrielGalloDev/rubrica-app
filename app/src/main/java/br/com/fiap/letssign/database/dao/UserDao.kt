package br.com.fiap.letssign.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import br.com.fiap.letssign.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE createdBy = :adminId AND role = 'SIGNER' ORDER BY name COLLATE NOCASE")
    fun observeSignersOf(adminId: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun findById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email COLLATE NOCASE LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(user: UserEntity)
}
