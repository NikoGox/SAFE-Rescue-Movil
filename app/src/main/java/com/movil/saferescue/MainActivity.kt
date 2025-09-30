package com.movil.saferescue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.movil.saferescue.ui.screen.InicioSesionScreen
import com.movil.saferescue.ui.theme.SAFERescueTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Maneja la transición de la pantalla de Splash
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        setContent {
            // Usa el tema de Compose de SAFERescue (que usa los colores definidos)
            SAFERescueTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Llamamos a la pantalla de inicio de sesión.
                    // Pasamos una función vacía ({}) para el manejo del clic por ahora.
                    InicioSesionScreen(onLoginClicked = {})
                }
            }
        }
    }
}