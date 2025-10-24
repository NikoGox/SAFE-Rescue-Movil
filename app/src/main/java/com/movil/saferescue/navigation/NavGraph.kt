// Ruta: app/src/main/java/com/movil/saferescue/navigation/NavGraph.kt
package com.movil.saferescue.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.movil.saferescue.ui.components.AppDrawer
import com.movil.saferescue.ui.components.AppTopBar
import com.movil.saferescue.ui.components.SRBottomNavigationBar
import com.movil.saferescue.ui.screen.*
import com.movil.saferescue.ui.viewmodel.*
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModelFactory: AuthViewModelFactory, // Recibe la factory desde MainActivity
    profileViewModelFactory: ProfileViewModelFactory,
    mensajeViewModelFactory: MensajeViewModelFactory,
    incidentsViewModelFactory: IncidentsViewModelFactory
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)
    val isAuthenticated by authViewModel.isAuthenticated.collectAsStateWithLifecycle()
    val incidentsViewModel: IncidentsViewModel = viewModel(factory = incidentsViewModelFactory)

    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route

    // Control de navegación para sesión persistente
    LaunchedEffect(isAuthenticated) {
        when (isAuthenticated) {
            true -> {
                if (currentRoute == Route.Login.path || currentRoute == null) {
                    navController.navigate(Route.Home.path) {
                        popUpTo(Route.Login.path) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            false -> {
                if (currentRoute != Route.Login.path && currentRoute != Route.Register.path) {
                    navController.navigate(Route.Login.path) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            null -> {} // Estado inicial, no hacer nada
        }
    }

    // Navegación normal entre pantallas
    val navigateTo: (String) -> Unit = { route ->
        if (route != currentRoute) {
            when {
                // Si no está autenticado y trata de ir a una ruta protegida
                isAuthenticated != true && route !in listOf(Route.Login.path, Route.Register.path) -> {
                    navController.navigate(Route.Login.path) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                // Si está autenticado o va a rutas públicas
                else -> {
                    navController.navigate(route) {
                        // Si va al home, limpiar el stack
                        if (route == Route.Home.path) {
                            popUpTo(0) { inclusive = true }
                        }
                        launchSingleTop = true
                    }
                }
            }
        }
        scope.launch { drawerState.close() }
    }

    // Función de logout
    val onLogout: () -> Unit = {
        scope.launch {
            authViewModel.logout()
            navController.navigate(Route.Login.path) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val routesWithBottomBar = listOf(Route.Home.path, Route.Incidente.path, Route.Notification.path, Route.Chat.path, Route.Profile.path)
    val routesWithoutBars = listOf(Route.Login.path, Route.Register.path)

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isAuthenticated == true && currentRoute !in routesWithoutBars && currentRoute != Route.Chat.path,
        drawerContent = {
            if (isAuthenticated == true) {
                AppDrawer(
                    currentRoute = currentRoute ?: "",
                    navigateTo = navigateTo,
                    onLogout = onLogout,
                    isAuthenticated = true
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (currentRoute !in routesWithoutBars) {
                    val isChatScreen = currentRoute == Route.Chat.path
                    AppTopBar(
                        title = "SAFE Rescue",
                        isAuthenticated = isAuthenticated == true,
                        onNavigate = navigateTo,
                        navigationIcon = {
                            if (isChatScreen) {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                                }
                            } else if (isAuthenticated == true) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, "Abrir menú")
                                }
                            }
                        },
                        actions = {
                            if (isAuthenticated == true) {
                                IconButton(onClick = { navigateTo(Route.Notification.path) }) {
                                    Icon(Icons.Default.Notifications, "Notificaciones")
                                }
                            } else {
                                IconButton(onClick = { navigateTo(Route.Login.path) }) {
                                    Icon(Icons.AutoMirrored.Filled.Login, "Login")
                                }
                                IconButton(onClick = { navigateTo(Route.Register.path) }) {
                                    Icon(Icons.Default.PersonAdd, "Registro")
                                }
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (currentRoute in routesWithBottomBar && isAuthenticated == true) {
                    SRBottomNavigationBar(
                        currentRoute = currentRoute ?: Route.Home.path,
                        onItemClick = navigateTo
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Route.Login.path,
                modifier = Modifier.padding(innerPadding)
            ) {
                // Rutas protegidas que requieren autenticación
                composable(Route.Home.path) {
                    if (isAuthenticated == true) {
                        HomeScreen(
                            isAuthenticated = true,
                            onGoLogin = { navigateTo(Route.Login.path) },
                            onGoRegister = { navigateTo(Route.Register.path) }
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.navigate(Route.Login.path) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }
                composable(Route.Login.path) {
                    LoginScreenVm(
                        onLoginOkNavigateHome = { navigateTo(Route.Home.path) },
                        onGoRegister = { navigateTo(Route.Register.path) },
                        factory = authViewModelFactory,
                        authViewModelFromParent = authViewModel
                    )
                }
                composable(Route.Register.path) {
                    RegisterScreenVm(
                        onRegisteredNavigateLogin = { navigateTo(Route.Login.path) },
                        onGoLogin = { navigateTo(Route.Login.path) },
                        factory = authViewModelFactory
                    )
                }
                composable(Route.Profile.path) {
                    ProfileScreen(factory = profileViewModelFactory)
                }
                composable(Route.Notification.path) {
                    NotificationScreenVm(
                        mensajeViewModelFactory = mensajeViewModelFactory,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(Route.Incidente.path) {
                    if (isAuthenticated == true) {
                        IncidentsScreen(viewModel = incidentsViewModel)
                    }
                }
                composable(Route.Chat.path) {
                    ChatScreenVm(
                        mensajeViewModelFactory = mensajeViewModelFactory,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
