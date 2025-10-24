package com.movil.saferescue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.movil.saferescue.navigation.AppNavGraph
import com.movil.saferescue.ui.theme.SAFERescueTheme
import com.movil.saferescue.ui.viewmodel.AuthViewModelFactory
import com.movil.saferescue.ui.viewmodel.IncidentsViewModelFactory
import com.movil.saferescue.ui.viewmodel.MensajeViewModelFactory
import com.movil.saferescue.ui.viewmodel.ProfileViewModelFactory

class MainActivity : ComponentActivity() {

    // 
    private val authViewModelFactory by lazy {
        val appContainer = application as SafeRescueApplication
        AuthViewModelFactory(
            userRepository = appContainer.userRepository,
            userPreferences = appContainer.userPreferences
        )
    }

    // El resto de las factories ya estaban correctas. No necesitan cambios.
    private val profileViewModelFactory by lazy {
        val appContainer = application as SafeRescueApplication
        ProfileViewModelFactory(
            appContainer.userRepository,
            appContainer.database.fotoDao(),
            applicationContext
        )
    }

    private val mensajeViewModelFactory by lazy {
        val appContainer = application as SafeRescueApplication
        MensajeViewModelFactory(appContainer.mensajeRepository)
    }

    private val incidenteViewModelFactory by lazy {
        val appContainer = application as SafeRescueApplication
        IncidentsViewModelFactory(
            incidenteRepository = appContainer.incidenteRepository,
            userRepository = appContainer.userRepository,
            applicationContext = applicationContext
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SAFERescueTheme {
                val navController = rememberNavController()

                // Pasamos las factorías, que ahora se crean correctamente.
                AppNavGraph(
                    navController = navController,
                    authViewModelFactory = authViewModelFactory,
                    profileViewModelFactory = profileViewModelFactory,
                    mensajeViewModelFactory = mensajeViewModelFactory,
                    incidentsViewModelFactory = incidenteViewModelFactory
                )
            }
        }
    }
}
