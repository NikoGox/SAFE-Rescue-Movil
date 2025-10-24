// Ruta: app/src/main/java/com/movil/saferescue/ui/viewmodel/AuthViewModelFactory.kt
package com.movil.saferescue.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.movil.saferescue.data.local.storage.UserPreferences
import com.movil.saferescue.data.repository.UserRepository

/**
 * Factory para crear una instancia de AuthViewModel.
 * Provee las dependencias necesarias: UserRepository y UserPreferences.
 */
class AuthViewModelFactory(
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            // Pasa las dependencias correctas al constructor de AuthViewModel
            return AuthViewModel(
                userRepository = userRepository,
                userPreferences = userPreferences
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
