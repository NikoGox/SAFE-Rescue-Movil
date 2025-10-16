package com.movil.saferescue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.movil.saferescue.data.local.database.AppDatabase
// --- CAMBIO 1: Importamos el NotificationViewModelFactory ---
import com.movil.saferescue.data.repository.NotificationRepository
import com.movil.saferescue.data.repository.UserRepository
import com.movil.saferescue.navigation.AppNavGraph
import com.movil.saferescue.ui.theme.SAFERescueTheme
import com.movil.saferescue.ui.viewmodel.AuthViewModelFactory
import com.movil.saferescue.ui.viewmodel.NotificationViewModelFactory
import com.movil.saferescue.ui.viewmodel.ProfileViewModelFactory
// Se eliminó 'kotlin.getValue' ya que no es necesario importarlo explícitamente

class MainActivity : ComponentActivity() {

    // Instancia única de la base de datos
    private val db by lazy { AppDatabase.getInstance(applicationContext) }

    // --- CAMBIO 2: Inicialización de Repositorios por separado ---
    // Cada repositorio es su propia propiedad 'val' con su propio 'lazy' block.
    private val userRepository by lazy {
        UserRepository(
            userDao = db.userDao(),
            fotoDao = db.fotoDao()
        )
    }

    private val notificationRepository by lazy {
        NotificationRepository(
            notificationDao = db.notificationDao()
        )
    }

    // --- CAMBIO 3: Creación de las Factories con el repositorio correcto ---
    // A cada Factory se le pasa el repositorio que necesita.
    private val authViewModelFactory by lazy { AuthViewModelFactory(userRepository) }
    private val profileViewModelFactory by lazy { ProfileViewModelFactory(userRepository) }

    // --- CAMBIO 4: Creamos la nueva Factory para las Notificaciones ---
    private val notificationViewModelFactory by lazy { NotificationViewModelFactory(notificationRepository) }


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
                    notificationViewModelFactory = notificationViewModelFactory
                )
            }
        }
    }
}
