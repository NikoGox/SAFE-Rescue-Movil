// Archivo: SafeRescueApplication.kt
package com.movil.saferescue

import android.app.Application
import com.movil.saferescue.data.local.database.AppDatabase
import com.movil.saferescue.data.local.storage.UserPreferences
import com.movil.saferescue.data.repository.IncidenteRepository
import com.movil.saferescue.data.repository.MensajeRepository
import com.movil.saferescue.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SafeRescueApplication : Application() {
    // Usaremos un SupervisorJob para que si una corrutina falla, no cancele las demás.
    // Aunque no es estrictamente necesario aquí, es una buena práctica para un scope de aplicación.
    private val applicationScope = CoroutineScope(SupervisorJob())

    // 1. La base de datos sigue siendo lazy para que no se cree hasta que se necesite,
    //    pero la vamos a "calentar" proactivamente.
    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val userPreferences by lazy { UserPreferences(this) }

    // 2. Los repositorios también pueden seguir siendo lazy.
    val userRepository by lazy { UserRepository(database.userDao(), database.fotoDao(), userPreferences) }
    val incidenteRepository by lazy { IncidenteRepository(database.incidenteDao(), database.fotoDao()) }
    val mensajeRepository by lazy { MensajeRepository(database.mensajeDao(), database.userDao()) }

    // 3. El método onCreate es el lugar perfecto para iniciar procesos de la aplicación.
    override fun onCreate() {
        super.onCreate()
        warmUpDatabase()
    }

    /**
     * Inicia una corrutina en un hilo de fondo (IO) para forzar la creación
     * y apertura de la base de datos Room. Esto evita que el hilo principal
     * se bloquee la primera vez que se accede a la base de datos.
     */
    private fun warmUpDatabase() {
        // Lanzamos la corrutina en el contexto de IO, optimizado para operaciones de disco.
        CoroutineScope(Dispatchers.IO).launch {
            database.userDao()
        }
    }
}
