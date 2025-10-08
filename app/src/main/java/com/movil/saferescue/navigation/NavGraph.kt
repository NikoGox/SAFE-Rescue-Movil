package com.movil.saferescue.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.movil.saferescue.ui.components.AppDrawer
import com.movil.saferescue.ui.screen.HomeScreen
import com.movil.saferescue.ui.screen.InicioSesionScreen
import com.movil.saferescue.ui.screen.RegisterScreen
import com.movil.saferescue.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    LaunchedEffect(authViewModel, navController) {
        authViewModel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.NavigateTo -> {
                    navController.navigate(event.route.path) {
                        event.popUpToRoute?.let { popUpRoute ->
                            popUpTo(popUpRoute.path) {
                                inclusive = event.inclusive
                            }
                        }
                    }
                }
                is NavigationEvent.PopBackStack -> navController.popBackStack()
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    navController.navigate(route.path) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        NavHost(navController = navController, startDestination = Route.Login.path) {
            composable(Route.Login.path) {
                InicioSesionScreen(viewModel = authViewModel)
            }
            composable(Route.Register.path) {
                RegisterScreen(
                    onRegistered = {
                        navController.navigate(Route.Home.path) {
                            popUpTo(Route.Login.path) { inclusive = true }
                        }
                    },
                    onGoLogin = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Route.Home.path) {
                HomeScreen(
                    onGoLogin = {
                        navController.navigate(Route.Login.path) {
                            popUpTo(Route.Home.path) { inclusive = true }
                        }
                    },
                    onGoRegister = { navController.navigate(Route.Register.path) },
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    }
                )
            }
        }
    }
}