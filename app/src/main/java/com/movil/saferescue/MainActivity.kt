package com.movil.saferescue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.movil.saferescue.data.local.database.AppDatabase
import com.movil.saferescue.data.repository.IncidenteRepository
import com.movil.saferescue.data.repository.NotificationRepository
import com.movil.saferescue.data.repository.UserRepository
import com.movil.saferescue.navigation.AppNavGraph
import com.movil.saferescue.ui.theme.SAFERescueTheme
import com.movil.saferescue.ui.viewmodel.AuthViewModelFactory
import com.movil.saferescue.ui.viewmodel.NotificationViewModelFactory
import com.movil.saferescue.ui.viewmodel.ProfileViewModelFactory
import com.movil.saferescue.ui.viewmodel.IncidentsViewModelFactory

class MainActivity : ComponentActivity() {

    // Instancia única de la base de datos
    private val db by lazy { AppDatabase.getInstance(applicationContext) }

    private val userRepository by lazy {
        UserRepository(
            userDao = db.userDao(),
            fotoDao = db.fotoDao()
        )
    }

    private val notificationRepository by lazy {
        NotificationRepository(
            notificationDao = db.notificationDao(),
            userDao = db.userDao()
        )
    }

    private val incidenteRepository by lazy {
        IncidenteRepository(
            incidenteDao = db.incidenteDao(),
            fotoDao = db.fotoDao()
        )
    }

    private val authViewModelFactory by lazy { AuthViewModelFactory(userRepository) }
    private val profileViewModelFactory by lazy { ProfileViewModelFactory(userRepository) }
    private val notificationViewModelFactory by lazy { NotificationViewModelFactory(notificationRepository) }
    private val incidenteViewModelFactory by lazy { IncidentsViewModelFactory(incidenteRepository) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // El tema de la aplicación envuelve todo el contenido.
            SAFERescueTheme {
                // Se crea el NavController que gestionará la navegación.
                val navController = rememberNavController()

                // --- CAMBIO 5: Pasamos la nueva Factory al NavGraph ---
                AppNavGraph(
                    navController = navController,
                    authViewModelFactory = authViewModelFactory,
                    profileViewModelFactory = profileViewModelFactory,
                    notificationViewModelFactory = notificationViewModelFactory,
                    incidentsViewModelFactory = incidenteViewModelFactory
                )
            }
        }
    }
}
