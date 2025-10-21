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
import com.movil.saferescue.ui.viewmodel.IncidentsViewModelFactory
import com.movil.saferescue.ui.viewmodel.NotificationViewModelFactory
import com.movil.saferescue.ui.viewmodel.ProfileViewModelFactory

class MainActivity : ComponentActivity() {

    private val db by lazy { AppDatabase.getDatabase(applicationContext) }

    private val userRepository by lazy {
        UserRepository(
            userDao = db.userDao(),
            fotoDao = db.fotoDao(),
            context = applicationContext
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

    private val authViewModelFactory by lazy {
        AuthViewModelFactory(userRepository, applicationContext)
    }

    private val profileViewModelFactory by lazy {
        ProfileViewModelFactory(userRepository)
    }

    private val notificationViewModelFactory by lazy {
        NotificationViewModelFactory(notificationRepository)
    }

    private val incidenteViewModelFactory by lazy {
        IncidentsViewModelFactory(incidenteRepository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SAFERescueTheme {
                val navController = rememberNavController()

                // Se pasan todas las factories al grafo de navegación, como siempre.
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
