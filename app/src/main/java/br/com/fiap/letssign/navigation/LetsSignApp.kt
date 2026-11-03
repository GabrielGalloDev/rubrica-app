package br.com.fiap.letssign.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.fiap.letssign.R
import br.com.fiap.letssign.factory.LocalAppContainer
import br.com.fiap.letssign.model.User
import br.com.fiap.letssign.model.UserRole
import br.com.fiap.letssign.navigation.LetsSignDestination.Companion.ARG_DRAFT_ID
import br.com.fiap.letssign.navigation.LetsSignDestination.Companion.ARG_ID
import br.com.fiap.letssign.navigation.LetsSignDestination.Companion.ARG_SIGNED
import br.com.fiap.letssign.repository.AppLanguage
import br.com.fiap.letssign.ui.screens.AdminHomeScreen
import br.com.fiap.letssign.ui.screens.DocumentDetailScreen
import br.com.fiap.letssign.ui.screens.LoginScreen
import br.com.fiap.letssign.ui.screens.NewDocumentScreen
import br.com.fiap.letssign.ui.screens.PdfViewerScreen
import br.com.fiap.letssign.ui.screens.RegisterScreen
import br.com.fiap.letssign.ui.screens.SignDocumentScreen
import br.com.fiap.letssign.ui.screens.SignerFormScreen
import br.com.fiap.letssign.ui.screens.SignerHomeScreen
import br.com.fiap.letssign.ui.screens.SettingsScreen
import br.com.fiap.letssign.ui.screens.SignerListScreen
import br.com.fiap.letssign.ui.screens.SplashScreen
import br.com.fiap.letssign.ui.viewmodel.AuthViewModel
import br.com.fiap.letssign.ui.viewmodel.DocumentDetailViewModel
import br.com.fiap.letssign.ui.viewmodel.HomeViewModel
import br.com.fiap.letssign.ui.viewmodel.NewDocumentViewModel
import br.com.fiap.letssign.ui.viewmodel.PdfViewerViewModel
import br.com.fiap.letssign.ui.viewmodel.SignDocumentViewModel
import br.com.fiap.letssign.ui.viewmodel.SignerFormViewModel
import br.com.fiap.letssign.ui.viewmodel.SettingsViewModel
import br.com.fiap.letssign.ui.viewmodel.SignerListViewModel
import kotlinx.coroutines.launch

@Composable
fun LetsSignApp(
    onLanguageApplied: (AppLanguage) -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val container = LocalAppContainer.current
    val auth = container.authRepository
    val user by auth.currentUser.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val openSettings = { navController.navigate(LetsSignDestination.Settings.route) }
    val demoAdminName = stringResource(R.string.demo_admin_name)

    fun goHome(signedIn: User) {
        val home = if (signedIn.role == UserRole.ADMIN) LetsSignDestination.AdminHome else LetsSignDestination.SignerHome
        navController.navigate(home.route) { popUpTo(0) { inclusive = true } }
    }

    fun goToLogin() {
        navController.navigate(LetsSignDestination.Login.route) { popUpTo(0) { inclusive = true } }
    }

    val logout: () -> Unit = {
        scope.launch {
            auth.logout()
            goToLogin()
        }
    }

    /** Protects routes: requires a signed-in user with the expected profile. */
    @Composable
    fun RequireUser(role: UserRole?, content: @Composable (User) -> Unit) {
        val current = user
        if (current != null && (role == null || current.role == role)) content(current)
        else LaunchedEffect(current) { goToLogin() }
    }

    NavHost(navController, startDestination = LetsSignDestination.Splash.route) {
        composable(LetsSignDestination.Splash.route) {
            SplashScreen()
            LaunchedEffect(Unit) {
                auth.seedDemoAdminIfNeeded(demoAdminName)
                auth.restoreSession()?.let(::goHome) ?: goToLogin()
            }
        }
        composable(LetsSignDestination.Login.route) {
            LoginScreen(
                viewModel = viewModel(factory = AuthViewModel.factory(container)),
                onLoggedIn = ::goHome,
                onNavigateToRegister = { navController.navigate(LetsSignDestination.Register.route) },
                onOpenSettings = openSettings,
            )
        }
        composable(LetsSignDestination.Settings.route) {
            SettingsScreen(
                viewModel = viewModel(factory = SettingsViewModel.factory(container)),
                onBack = { navController.popBackStack() },
                onLanguageApplied = onLanguageApplied,
            )
        }
        composable(LetsSignDestination.Register.route) {
            RegisterScreen(
                viewModel = viewModel(factory = AuthViewModel.factory(container)),
                onBack = { navController.popBackStack() },
                onRegistered = ::goHome,
            )
        }

        // ------------------- Admin -------------------
        composable(LetsSignDestination.AdminHome.route) {
            RequireUser(UserRole.ADMIN) { admin ->
                AdminHomeScreen(
                    admin = admin,
                    viewModel = viewModel(key = "admin-home-${admin.id}", factory = HomeViewModel.factory(container, admin, asAdmin = true)),
                    onNewDocument = { navController.navigate(LetsSignDestination.NewDocument.create()) },
                    onSigners = { navController.navigate(LetsSignDestination.Signers.route) },
                    onOpenDocument = { navController.navigate(LetsSignDestination.DocumentDetail.create(it)) },
                    onOpenSettings = openSettings,
                    onLogout = logout,
                )
            }
        }
        composable(LetsSignDestination.Signers.route) {
            RequireUser(UserRole.ADMIN) { admin ->
                SignerListScreen(
                    viewModel = viewModel(factory = SignerListViewModel.factory(container, admin)),
                    onBack = { navController.popBackStack() },
                    onAdd = { navController.navigate(LetsSignDestination.SignerForm.create()) },
                    onEdit = { navController.navigate(LetsSignDestination.SignerForm.create(it)) },
                )
            }
        }
        composable(
            LetsSignDestination.SignerForm.route,
            arguments = listOf(navArgument(ARG_ID) { type = NavType.StringType; nullable = true; defaultValue = null }),
        ) { entry ->
            RequireUser(UserRole.ADMIN) { admin ->
                val signerId = entry.arguments?.getString(ARG_ID)
                SignerFormScreen(
                    viewModel = viewModel(key = "signer-form-$signerId", factory = SignerFormViewModel.factory(container, admin, signerId)),
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(
            LetsSignDestination.NewDocument.route,
            arguments = listOf(navArgument(ARG_DRAFT_ID) { type = NavType.StringType; nullable = true; defaultValue = null }),
        ) { entry ->
            RequireUser(UserRole.ADMIN) { admin ->
                val draftId = entry.arguments?.getString(ARG_DRAFT_ID)
                val defaultMessage = stringResource(R.string.wizard_default_message)
                NewDocumentScreen(
                    viewModel = viewModel(
                        key = "new-doc-${draftId ?: "new"}",
                        factory = NewDocumentViewModel.factory(container, admin, draftId, defaultMessage),
                    ),
                    onExit = { navController.popBackStack(LetsSignDestination.AdminHome.route, inclusive = false) },
                    onManageSigners = { navController.navigate(LetsSignDestination.SignerForm.create()) },
                    onOpenDocument = { id ->
                        navController.navigate(LetsSignDestination.DocumentDetail.create(id)) {
                            popUpTo(LetsSignDestination.AdminHome.route)
                        }
                    },
                )
            }
        }
        composable(
            LetsSignDestination.DocumentDetail.route,
            arguments = listOf(navArgument(ARG_ID) { type = NavType.StringType }),
        ) { entry ->
            RequireUser(UserRole.ADMIN) { admin ->
                val documentId = entry.arguments?.getString(ARG_ID).orEmpty()
                DocumentDetailScreen(
                    viewModel = viewModel(key = "detail-$documentId", factory = DocumentDetailViewModel.factory(container, admin, documentId)),
                    onBack = { navController.popBackStack() },
                    onEditDraft = { id ->
                        navController.navigate(LetsSignDestination.NewDocument.create(id)) {
                            popUpTo(LetsSignDestination.AdminHome.route)
                        }
                    },
                    onOpenViewer = { id, signed -> navController.navigate(LetsSignDestination.Viewer.create(id, signed)) },
                )
            }
        }
        composable(
            LetsSignDestination.Viewer.route,
            arguments = listOf(
                navArgument(ARG_ID) { type = NavType.StringType },
                navArgument(ARG_SIGNED) { type = NavType.BoolType },
            ),
        ) { entry ->
            RequireUser(role = null) { current ->
                val documentId = entry.arguments?.getString(ARG_ID).orEmpty()
                val signed = entry.arguments?.getBoolean(ARG_SIGNED) ?: false
                PdfViewerScreen(
                    viewModel = viewModel(
                        key = "viewer-$documentId-$signed",
                        factory = PdfViewerViewModel.factory(container, current, documentId, signed),
                    ),
                    onBack = { navController.popBackStack() },
                )
            }
        }

        // ------------------- Signer -------------------
        composable(LetsSignDestination.SignerHome.route) {
            RequireUser(UserRole.SIGNER) { signer ->
                SignerHomeScreen(
                    signer = signer,
                    viewModel = viewModel(key = "signer-home-${signer.id}", factory = HomeViewModel.factory(container, signer, asAdmin = false)),
                    onOpenDocument = { navController.navigate(LetsSignDestination.Sign.create(it)) },
                    onOpenSettings = openSettings,
                    onLogout = logout,
                )
            }
        }
        composable(
            LetsSignDestination.Sign.route,
            arguments = listOf(navArgument(ARG_ID) { type = NavType.StringType }),
        ) { entry ->
            RequireUser(UserRole.SIGNER) { signer ->
                val documentId = entry.arguments?.getString(ARG_ID).orEmpty()
                SignDocumentScreen(
                    viewModel = viewModel(key = "sign-$documentId", factory = SignDocumentViewModel.factory(container, signer, documentId)),
                    signerId = signer.id,
                    onBack = { navController.popBackStack() },
                    onOpenSigned = { navController.navigate(LetsSignDestination.Viewer.create(it, signed = true)) },
                )
            }
        }
    }
}
