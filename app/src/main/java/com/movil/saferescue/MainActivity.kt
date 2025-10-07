package com.movil.saferescue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost // ⬅️ ¡Nueva Importación!
import androidx.navigation.compose.composable // ⬅️ ¡Nueva Importación!
import androidx.navigation.compose.rememberNavController // ⬅️ ¡Nueva Importación!
import com.movil.saferescue.navigation.Route // ⬅️ Importa tu clase sellada Route
import com.movil.saferescue.ui.screen.HomeScreen
import com.movil.saferescue.ui.screen.InicioSesionScreen
import com.movil.saferescue.ui.theme.SAFERescueTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            SAFERescueTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 2. Ahora AppNavHost usa NavController
                    AppNavHost()
                }
            }
        }
    }
}

@Composable
fun AppNavHost() {
    // 3. NavController maneja el estado de la navegación
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        // ✅ PANTALLA INICIAL: Inicio de Sesión
        startDestination = Route.Login.path
    ) {
        // Pantalla de LOGIN
        composable(Route.Login.path) {
            InicioSesionScreen(
                // ✅ NAVEGACIÓN A HOME: Al hacer clic, navega a la ruta "home"
                onLoginClicked = {
                    navController.navigate(Route.Home.path) {
                        // Opcional pero recomendado: Evita volver a la pantalla de Login con el botón "atrás"
                        popUpTo(Route.Login.path) { inclusive = true }
                    }
                },

            )
        }

        // Pantalla de HOME
        composable(Route.Home.path) {
            HomeScreen(
                // ✅ NAVEGACIÓN A LOGIN: El botón "Volver a Iniciar Sesión" en Home usa esta acción.
                onGoLogin = { navController.navigate(Route.Login.path) },
                onGoRegister = { navController.navigate(Route.Register.path) }
            )
        }

        // Pantalla de REGISTRO
        composable(Route.Register.path) {
            // Placeholder: Sustituir por RegisterScreen real cuando exista
            HomeScreen(
                onGoLogin = { navController.navigate(Route.Login.path) },
                onGoRegister = { }
            )
        }
    }
}