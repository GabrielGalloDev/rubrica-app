package br.com.fiap.letssign.navigation

sealed class LetsSignDestination(val route: String) {
    data object Splash : LetsSignDestination("splash")
    data object Login : LetsSignDestination("login")
    data object Register : LetsSignDestination("register")
    data object Settings : LetsSignDestination("settings")

    // Admin
    data object AdminHome : LetsSignDestination("admin")
    data object Signers : LetsSignDestination("signers")
    data object SignerForm : LetsSignDestination("signer_form?$ARG_ID={$ARG_ID}") {
        fun create(id: String? = null) = if (id == null) "signer_form" else "signer_form?$ARG_ID=$id"
    }
    data object NewDocument : LetsSignDestination("new_document?$ARG_DRAFT_ID={$ARG_DRAFT_ID}") {
        fun create(draftId: String? = null) =
            if (draftId == null) "new_document" else "new_document?$ARG_DRAFT_ID=$draftId"
    }
    data object DocumentDetail : LetsSignDestination("detail/{$ARG_ID}") {
        fun create(id: String) = "detail/$id"
    }

    // Admin and signer
    data object Viewer : LetsSignDestination("viewer/{$ARG_ID}/{$ARG_SIGNED}") {
        fun create(id: String, signed: Boolean) = "viewer/$id/$signed"
    }

    // Signer
    data object SignerHome : LetsSignDestination("signer")
    data object Sign : LetsSignDestination("sign/{$ARG_ID}") {
        fun create(id: String) = "sign/$id"
    }

    companion object {
        const val ARG_ID = "id"
        const val ARG_DRAFT_ID = "draftId"
        const val ARG_SIGNED = "signed"
    }
}
