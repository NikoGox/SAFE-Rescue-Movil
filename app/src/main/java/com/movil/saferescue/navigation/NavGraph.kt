package com.movil.saferescue.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
// --- CAMBIO 1: Importar el BottomNavigationBar ---
import com.movil.saferescue.ui.components.AppDrawer
import com.movil.saferescue.ui.components.AppTopBar
import com.movil.saferescue.ui.components.SRBottomNavigationBar
import com.movil.saferescue.ui.screen.HomeScreen
import com.movil.saferescue.ui.screen.LoginScreenVm
import com.movil.saferescue.ui.screen.NotificationScreenVm
import com.movil.saferescue.ui.screen.ProfileScreen
import com.movil.saferescue.ui.screen.RegisterScreenVm
import com.movil.saferescue.ui.viewmodel.AuthViewModelFactory
import com.movil.saferescue.ui.viewmodel.NotificationViewModelFactory
import com.movil.saferescue.ui.viewmodel.ProfileViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModelFactory: AuthViewModelFactory,
    profileViewModelFactory: ProfileViewModelFactory,
    notificationViewModelFactory: NotificationViewModelFactory
) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    // --- NAVEGACIÓN ---
    val goHome: () -> Unit = { navController.navigate(Route.Home.path) { launchSingleTop = true } }
    val goLogin: () -> Unit = { navController.navigate(Route.Login.path) }
    val goRegister: () -> Unit = { navController.navigate(Route.Register.path) }
    val goProfile: () -> Unit = { navController.navigate(Route.Profile.path) }
    val goNotifications: () -> Unit = { navController.navigate(Route.Notification.path) }
    val onNavigateBack: () -> Unit = { navController.popBackStack() }

    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route

    var isAuthenticated by remember { mutableStateOf(false) }

    val onLoginSuccess = {
        isAuthenticated = true
        navController.navigate(Route.Home.path) {
            popUpTo(Route.Login.path) { inclusive = true }
        }
    }

    val onLogout = {
        isAuthenticated = false
        navController.navigate(Route.Login.path) {
            popUpTo(0)
        }
    }

    // Rutas que no deben mostrar ni TopBar ni BottomBar
    val routesWithoutBars = listOf(Route.Login.path, Route.Register.path)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                isAuthenticated = isAuthenticated,
                onCloseDrawer = { scope.launch { drawerState.close() } },
                onHome = goHome,
                onGoProfile = goProfile,
                onGoLogin = goLogin,
                onGoRegister = goRegister,
                onLogout = onLogout,
                onGoNotifications = goNotifications
            )
        }
    ) {
        Scaffold(
            topBar = {
                if (currentRoute !in routesWithoutBars) {
                    AppTopBar(
                        isAuthenticated = isAuthenticated,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onGoLogin = goLogin,
                        onGoRegister = goRegister,
                        onLogout = onLogout ,
                        onGoNotifications = goNotifications
                    )
                }
            },
            // --- CAMBIO 2: Añadir el BottomBar al Scaffold ---
            bottomBar = {
                // Solo mostramos la barra de navegación si estamos en la ruta "Home"
                if (currentRoute == Route.Home.path) {
                    SRBottomNavigationBar(
                        currentRoute = currentRoute,
                        onItemClick = { route ->
                            // El clic solo funciona si el usuario está autenticado
                            if (isAuthenticated) {
                                navController.navigate(route) {
                                    // Evita apilar la misma pantalla múltiples veces
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                // Si el usuario ya está logueado, empieza en Home, sino en Login
                startDestination = if (isAuthenticated) Route.Home.path else Route.Login.path,
                modifier = Modifier.padding(innerPadding)
            ) {
                // --- CAMBIO 3: Pasar el estado de autenticación a HomeScreen ---
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
                        notificationViewModelFactory = notificationViewModelFactory,
                        onNavigateBack = onNavigateBack
                    )
                }
                // Aquí irían las rutas de "Incidents" y "Chat" que definiste en el BottomBar
                // composable("incidents_route") { ... }
                // composable("chat_route") { ... }
            }
        }
    }
}
