package com.movil.saferescue.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.movil.saferescue.ui.components.AppDrawer
import com.movil.saferescue.ui.components.AppTopBar
import com.movil.saferescue.ui.screen.HomeScreen
import com.movil.saferescue.ui.screen.LoginScreenVm
import com.movil.saferescue.ui.screen.ProfileScreen
import com.movil.saferescue.ui.screen.RegisterScreenVm
import com.movil.saferescue.ui.viewmodel.AuthViewModelFactory
import com.movil.saferescue.ui.viewmodel.ProfileViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModelFactory: AuthViewModelFactory,
    profileViewModelFactory: ProfileViewModelFactory
) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val goHome: () -> Unit = { navController.navigate(Route.Home.path) }
    val goLogin: () -> Unit = { navController.navigate(Route.Login.path) }
    val goRegister: () -> Unit = { navController.navigate(Route.Register.path) }
    val goProfile: () -> Unit = { navController.navigate(Route.Profile.path) }

    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route

    var isAuthenticated by remember { mutableStateOf(false) }

    // Función para actualizar el estado y navegar al hacer login
    val onLoginSuccess = {
        isAuthenticated = true
        // Navega a Home y limpia el backstack para que el usuario no pueda volver al login con el botón de atrás
        navController.navigate(Route.Home.path) {
            popUpTo(Route.Login.path) { inclusive = true }
        }
    }

    // Función para hacer logout
    val onLogout = {
        isAuthenticated = false
        // Navega al Login y limpia todo el backstack
        navController.navigate(Route.Login.path) {
            popUpTo(0)
        }
    }

    val routesWithoutTopBar = listOf(Route.Login.path, Route.Register.path)

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
                onLogout = onLogout // Pasa la función de logout
            )

        }
    ) {
        Scaffold(
            topBar = {
                if (currentRoute !in routesWithoutTopBar) {
                    AppTopBar(
                        isAuthenticated = isAuthenticated,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onGoProfile = goProfile,
                        onGoLogin = goLogin,
                        onGoRegister = goRegister,
                        onLogout = onLogout // Pasa la función de logout
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Route.Login.path,
                modifier = Modifier.padding(innerPadding)
            ) {

                composable(Route.Home.path) {
                    HomeScreen(
                        onGoLogin = goLogin,
                        onGoRegister = goRegister
                    )
                }
                composable(Route.Login.path) {
                    LoginScreenVm(
                        onLoginOkNavigateHome = onLoginSuccess, // Usa onLoginSuccess
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
            }
        }
    }
}
