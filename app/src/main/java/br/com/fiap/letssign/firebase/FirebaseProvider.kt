package br.com.fiap.letssign.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

/**
 * Entry point to Firebase. It is only configured when the app was built with a
 * google-services.json; otherwise the app runs in local mode (Room only).
 */
class FirebaseProvider(context: Context) {
    private val app: FirebaseApp? = FirebaseApp.getApps(context).firstOrNull()

    val isConfigured: Boolean = app != null

    val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance(requireApp())

    /** A google-services.json downloaded before creating the database has no URL, so use the default one. */
    val database: DatabaseReference by lazy {
        val firebaseApp = requireApp()
        val url = firebaseApp.options.databaseUrl
            ?: "https://${firebaseApp.options.projectId}-default-rtdb.firebaseio.com"
        FirebaseDatabase.getInstance(firebaseApp, url).reference
    }

    /** Separate auth instance used to create signer accounts without replacing the admin session. */
    fun secondaryAuth(context: Context): FirebaseAuth {
        val primary = requireApp()
        val secondary = FirebaseApp.getApps(context).firstOrNull { it.name == SECONDARY_APP }
            ?: FirebaseApp.initializeApp(context, primary.options, SECONDARY_APP)
        return FirebaseAuth.getInstance(secondary)
    }

    private fun requireApp(): FirebaseApp = app ?: error("Firebase is not configured")

    private companion object {
        const val SECONDARY_APP = "signer-admin"
    }
}
