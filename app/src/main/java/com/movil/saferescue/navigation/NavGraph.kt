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
    // Instanciamos el ViewModel aquí para que sea compartido por todo el NavGraph
    val mensajeViewModel: MensajeViewModel = viewModel(factory = mensajeViewModelFactory)
    
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route

    var isPanelOpen by remember { mutableStateOf(false) }
    val notifications by mensajeViewModel.userNotifications.collectAsStateWithLifecycle()
    val isAdmin by mensajeViewModel.isCurrentUserAdmin.collectAsStateWithLifecycle(initialValue = false)
    val isBombero by mensajeViewModel.isCurrentUserBombero.collectAsStateWithLifecycle(initialValue = false)
    
    // Detectar si estamos en una conversación activa para mostrar la flecha de atrás
    val isConversationRoute = currentRoute == Route.Conversation.path

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
    val isGesturesEnabled = currentRoute != Route.Home.path && isAuthenticated == true && !isPanelOpen && currentRoute !in routesWithoutBars && currentRoute != Route.Conversation.path

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
                        isChatScreen = isConversationRoute, // Solo true si estamos DENTRO de un chat
                        isPanelOpen = isPanelOpen,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateBack = { 
                            // Si estamos en una conversación, volvemos a la lista.
                            navController.popBackStack() 
                        },
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
                    
                    // Ruta 1: Lista de Chats
                    composable(Route.Chat.path) { 
                        // Pasamos la instancia COMPARTIDA del ViewModel
                        ChatListScreen(
                            viewModel = mensajeViewModel,
                            onConversationClick = { chatId ->
                                // Guardamos el estado en el VM compartido y navegamos
                                mensajeViewModel.selectConversation(chatId)
                                navController.navigate(Route.Conversation.path)
                            }
                        ) 
                    }

                    // Ruta 2: Conversación Activa
                    composable(Route.Conversation.path) {
                        // Pasamos la MISMA instancia compartida del ViewModel
                        ConversationScreen(
                            viewModel = mensajeViewModel,
                            conversationId = null, // El estado ya está en el VM
                            onBack = { navController.popBackStack() }
                        )
                    }
                    
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
