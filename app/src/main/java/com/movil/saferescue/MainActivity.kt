package com.movil.saferescue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.movil.saferescue.data.local.database.AppDatabase
import com.movil.saferescue.data.repository.UserRepository
import com.movil.saferescue.navigation.AppNavGraph
import com.movil.saferescue.ui.theme.SAFERescueTheme
import com.movil.saferescue.ui.viewmodel.AuthViewModelFactory
import com.movil.saferescue.ui.viewmodel.ProfileViewModelFactory

class MainActivity : ComponentActivity() {


    private val db by lazy { AppDatabase.getInstance(this) }
    private val repository by lazy { UserRepository(db.userDao(), db.fotoDao()) }

    // Factories para los ViewModels, que inyectan el repositorio
    private val authViewModelFactory by lazy { AuthViewModelFactory(repository) }
    private val profileViewModelFactory by lazy { ProfileViewModelFactory(repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SAFERescueTheme {
                val navController = rememberNavController()

                // Se establece el grafo de navegación como el contenido principal de la app
                AppNavGraph(
                    navController = navController,
                    authViewModelFactory = authViewModelFactory,
                    profileViewModelFactory = profileViewModelFactory
                )
            }
        }
    }
}
