package br.com.fiap.letssign.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.com.fiap.letssign.database.dao.DocumentDao
import br.com.fiap.letssign.database.dao.UserDao
import br.com.fiap.letssign.database.entity.DocumentEntity
import br.com.fiap.letssign.database.entity.DocumentEventEntity
import br.com.fiap.letssign.database.entity.DocumentSignerEntity
import br.com.fiap.letssign.database.entity.SignatureFieldEntity
import br.com.fiap.letssign.database.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        DocumentEntity::class,
        DocumentSignerEntity::class,
        SignatureFieldEntity::class,
        DocumentEventEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun documentDao(): DocumentDao

    companion object {
        private const val NAME = "letssign.db"

        // Room works as a local cache of the synced data, so an incompatible schema is rebuilt.
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, NAME)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
