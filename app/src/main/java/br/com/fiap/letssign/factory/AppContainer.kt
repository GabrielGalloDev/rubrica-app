package br.com.fiap.letssign.factory

import android.content.Context
import br.com.fiap.letssign.database.AppDatabase
import br.com.fiap.letssign.firebase.FirebaseProvider
import br.com.fiap.letssign.repository.AppPreferences
import br.com.fiap.letssign.repository.AuthRepository
import br.com.fiap.letssign.repository.DocumentRepository
import br.com.fiap.letssign.repository.FileStorage
import br.com.fiap.letssign.repository.RealtimeRemoteDataSource
import br.com.fiap.letssign.repository.RemoteDataSource
import br.com.fiap.letssign.repository.SignerRepository
import br.com.fiap.letssign.utils.LocaleManager
import br.com.fiap.letssign.utils.PdfStamper

/** Manual dependency injection: builds the database, remote source and repositories once. */
class AppContainer(context: Context) {
    private val applicationContext = context.applicationContext

    private val database = AppDatabase.create(applicationContext)

    private val firebaseProvider = FirebaseProvider(applicationContext)

    /** Firebase is only active when the app was built with a google-services.json. */
    val isFirebaseConfigured: Boolean = firebaseProvider.isConfigured

    val preferences: AppPreferences = AppPreferences(applicationContext)

    private val remote: RemoteDataSource? =
        if (isFirebaseConfigured) RealtimeRemoteDataSource(applicationContext, firebaseProvider) else null

    val authRepository: AuthRepository =
        AuthRepository(database.userDao(), remote, preferences)

    val signerRepository: SignerRepository =
        SignerRepository(database.userDao(), remote)

    val documentRepository: DocumentRepository = DocumentRepository(
        dao = database.documentDao(),
        remote = remote,
        files = FileStorage(applicationContext),
        stamper = PdfStamper { LocaleManager.wrap(applicationContext, preferences.language.value) },
    )
}
