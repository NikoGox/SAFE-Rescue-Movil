package com.movil.saferescue.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.movil.saferescue.ui.components.AppDrawer
import com.movil.saferescue.ui.components.AppTopBarWithNotifications
import com.movil.saferescue.ui.components.NotificationsPanel
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
    incidenteViewModelFactory: IncidenteViewModelFactory 
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)
    val isAuthenticated by authViewModel.isAuthenticated.collectAsStateWithLifecycle()
    val incidenteViewModel: IncidenteViewModel = viewModel(factory = incidenteViewModelFactory)
    val mensajeViewModel: MensajeViewModel = viewModel(factory = mensajeViewModelFactory)
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route

    var isPanelOpen by remember { mutableStateOf(false) }
    val notifications by mensajeViewModel.userNotifications.collectAsStateWithLifecycle()
    val isAdmin by mensajeViewModel.isCurrentUserAdmin.collectAsStateWithLifecycle(initialValue = false)
    val isBombero by mensajeViewModel.isCurrentUserBombero.collectAsStateWithLifecycle(initialValue = false)

    // Handle back press to close drawer, independent of gestures
    if (drawerState.isOpen) {
        BackHandler(enabled = true) {
            scope.launch { drawerState.close() }
        }
    }

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated == false) {
            isPanelOpen = false
            scope.launch { drawerState.close() }
            navController.navigate(Route.Login.path) {
                popUpTo(0) { inclusive = true }
            }
        } else if (isAuthenticated == true && (currentRoute == Route.Login.path || currentRoute == null)) {
            navController.navigate(Route.Home.path) {
                popUpTo(Route.Login.path) { inclusive = true }
            }
        }
    }

    val navigateTo: (String) -> Unit = { route ->
        isPanelOpen = false
        if (route != currentRoute) {
            navController.navigate(route) {
                if (route == Route.Home.path) {
                    popUpTo(0) { inclusive = true }
                }
                launchSingleTop = true
            }
        }
        scope.launch { drawerState.close() }
    }

    val onLogout: () -> Unit = {
        scope.launch {
            authViewModel.logout()
        }
    }

    val routesWithoutBars = listOf(Route.Login.path, Route.Register.path)
    // Disable gestures on Home screen (map) to prevent conflict, but allow tap-to-close via the scrim
    val isGesturesEnabled = currentRoute != Route.Home.path && isAuthenticated == true && !isPanelOpen && currentRoute !in routesWithoutBars && currentRoute != Route.Chat.path

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isGesturesEnabled,
        drawerContent = { AppDrawer(currentRoute ?: "", navigateTo, onLogout, isAuthenticated == true, isAdmin, isBombero) }
    ) {
        Scaffold(
            topBar = {
                if (isAuthenticated == true && currentRoute !in routesWithoutBars) {
                    AppTopBarWithNotifications(
                        viewModel = mensajeViewModel,
                        isAuthenticated = true,
                        isChatScreen = currentRoute == Route.Chat.path,
                        isPanelOpen = isPanelOpen,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateBack = { navController.popBackStack() },
                        onTogglePanel = { isPanelOpen = !isPanelOpen }
                    )
                }
            },
            bottomBar = {
                if (isAuthenticated == true && currentRoute !in routesWithoutBars) {
                    SRBottomNavigationBar(
                        currentRoute = currentRoute ?: Route.Home.path,
                        onItemClick = navigateTo
                    )
                }
            }
        ) { innerPadding ->
            Box {
                NavHost(
                    navController = navController,
                    startDestination = Route.Login.path,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Route.Home.path) {
                        HomeScreen(
                            isAuthenticated = (isAuthenticated == true),
                            onGoLogin = { navigateTo(Route.Login.path) },
                            onGoRegister = { navigateTo(Route.Register.path) },
                            incidenteViewModel = incidenteViewModel
                        )
                    }
                    composable(Route.Login.path) { LoginScreenVm({ navigateTo(Route.Home.path) }, { navigateTo(Route.Register.path) }, authViewModelFactory, authViewModel) }
                    composable(Route.Register.path) { RegisterScreenVm({ navigateTo(Route.Login.path) }, { navigateTo(Route.Login.path) }, authViewModelFactory) }
                    composable(Route.Profile.path) { ProfileScreen(profileViewModelFactory) }
                    composable(Route.Notification.path) { CreateNotificationScreen(mensajeViewModelFactory) { navController.popBackStack() } }
                    composable(Route.Incidente.path) { IncidenteScreen(incidenteViewModel, isAdmin, isBombero) }
                    composable(Route.Chat.path) { ChatScreenVm(mensajeViewModelFactory) { navController.popBackStack() } }
                    composable(Route.CrearIncidente.path) {
                        CrearIncidenteScreen(incidenteViewModel) {
                            navController.popBackStack()
                        }
                    }
                    composable(Route.IncidentesAsignados.path) { IncidentesAsignadosScreen(incidenteViewModel) }
                }

                AnimatedVisibility(
                    visible = isPanelOpen,
                    enter = slideInVertically { fullHeight -> -fullHeight },
                    exit = slideOutVertically { fullHeight -> -fullHeight }
                ) {
                    NotificationsPanel(
                        notifications = notifications,
                        onDeleteNotification = { id -> mensajeViewModel.deleteUserNotification(id) },
                        onMarkAsRead = { id -> mensajeViewModel.markNotificationAsRead(id) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}