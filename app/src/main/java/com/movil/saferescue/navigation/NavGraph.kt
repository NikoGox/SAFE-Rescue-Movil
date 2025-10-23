package com.movil.saferescue.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.movil.saferescue.ui.components.AppDrawer // Tu AppDrawer refactorizado
import com.movil.saferescue.ui.components.AppTopBar
import com.movil.saferescue.ui.components.SRBottomNavigationBar
import com.movil.saferescue.ui.screen.*
import com.movil.saferescue.ui.viewmodel.*
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModelFactory: AuthViewModelFactory,
    profileViewModelFactory: ProfileViewModelFactory,
    mensajeViewModelFactory: MensajeViewModelFactory,
    incidentsViewModelFactory: IncidentsViewModelFactory
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // --- VIEWMODELS CENTRALIZADOS (Esto ya lo tenías bien) ---
    val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)
    val isAuthenticated by authViewModel.isAuthenticated.collectAsStateWithLifecycle()
    val incidentsViewModel: IncidentsViewModel = viewModel(factory = incidentsViewModelFactory)

    // --- OBTENER RUTA ACTUAL (Esto ya lo tenías bien) ---
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route ?: Route.Login.path

    // --- INICIO DE LA CORRECCIÓN PRINCIPAL ---

    // 1. LÓGICA DE NAVEGACIÓN GENÉRICA
    val navigateTo: (String) -> Unit = { route ->
        if (route != currentRoute) {
            navController.navigate(route) {
                // Esta lógica es buena para la BottomBar y el Drawer,
                // asegura que no se apilen pantallas innecesariamente.
                popUpTo(Route.Home.path) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
        // Siempre cerramos el drawer después de un clic.
        scope.launch { drawerState.close() }
    }

    // 2. LÓGICA DE LOGOUT REACTIVA
    val onLogout: () -> Unit = {
        scope.launch {
            authViewModel.logout()
            // No es necesario navegar explícitamente a Login.
            // El NavHost reaccionará al cambio de `isAuthenticated` y lo hará por ti.
            // Solo necesitamos cerrar el drawer.
            drawerState.close()
        }
    }

    // --- FIN DE LA CORRECCIÓN PRINCIPAL ---

    // Definiciones de rutas para acciones específicas (Login/Register)
    val onLoginSuccess = {
        navController.navigate(Route.Home.path) {
            popUpTo(Route.Login.path) { inclusive = true }
        }
    }
    val goRegister: () -> Unit = { navController.navigate(Route.Register.path) }
    val goLogin: () -> Unit = { navController.navigate(Route.Login.path) }
    val onNavigateBack: () -> Unit = { navController.popBackStack() }

    // Listas de rutas para la UI (Esto ya lo tenías bien)
    val routesWithBottomBar = listOf(Route.Home.path, Route.Incidente.path, Route.Notification.path, Route.Chat.path)
    val routesWithoutBars = listOf(Route.Login.path, Route.Register.path)
    val routesWithDrawer = routesWithBottomBar + listOf(Route.Profile.path)

    ModalNavigationDrawer(
        gesturesEnabled = currentRoute in routesWithDrawer,
        drawerState = drawerState,
        drawerContent = {
            // 3. LLAMADA AL NUEVO Y MEJORADO APPDRAWER
            AppDrawer(
                currentRoute = currentRoute,
                navigateTo = navigateTo,
                onLogout = onLogout,
                isAuthenticated = isAuthenticated
            )
        }
    ) {
        Scaffold(
            topBar = {
                if (currentRoute !in routesWithoutBars) {
                    AppTopBar(
                        isAuthenticated = isAuthenticated,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onLogout = onLogout,
                        onNavigate = { route -> navigateTo(route) }
                    )
                }
            },
            bottomBar = {
                if (currentRoute in routesWithBottomBar) {
                    // La lógica para la Bottom Bar se simplifica usando la función 'navigateTo'
                    SRBottomNavigationBar(
                        currentRoute = currentRoute,
                        onItemClick = { route -> navigateTo(route) }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if (isAuthenticated) Route.Home.path else Route.Login.path,
                modifier = Modifier.padding(innerPadding)
            ) {
                // Composables (El resto del código estaba correcto)
                composable(Route.Home.path) {
                    HomeScreen(
                        isAuthenticated = isAuthenticated,
                        onGoLogin = goLogin,
                        onGoRegister = goRegister
                    )
                }
                composable(Route.Login.path) {
                    LoginScreenVm(
                        onLoginOkNavigateHome = onLoginSuccess,
                        onGoRegister = goRegister,
                        factory = authViewModelFactory
                    )
                }
                composable(Route.Register.path) {
                    RegisterScreenVm(
                        onRegisteredNavigateLogin = goLogin,
                        onGoLogin = goLogin,
                        factory = authViewModelFactory
                    )
                }
                composable(Route.Profile.path) {
                    ProfileScreen(factory = profileViewModelFactory)
                }
                composable(Route.Notification.path) {
                    NotificationScreenVm(
                        mensajeViewModelFactory = mensajeViewModelFactory,
                        onNavigateBack = onNavigateBack
                    )
                }
                composable(Route.Incidente.path) {
                    IncidentsScreen(viewModel = incidentsViewModel)
                }
                composable(Route.Chat.path) {
                    ChatScreenVm(
                        mensajeViewModelFactory = mensajeViewModelFactory,
                        onNavigateBack = onNavigateBack
                    )
                }
            }
        }
    }
}
