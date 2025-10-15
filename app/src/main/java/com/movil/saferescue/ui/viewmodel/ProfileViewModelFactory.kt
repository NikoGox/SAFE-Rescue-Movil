package com.movil.saferescue.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.movil.saferescue.data.repository.UserRepository

/**
 * Factory para crear instancias de ProfileViewModel.
 * Es necesaria porque ProfileViewModel tiene un constructor con parámetros (el repositorio),
 * y el sistema de ViewModels de Android no sabe cómo inyectar esa dependencia por sí solo.
 */
class ProfileViewModelFactory(
    private val repository: UserRepository // 1. Recibe el repositorio como dependencia
) : ViewModelProvider.Factory {

    /**
     * Este método es llamado por el sistema para crear el ViewModel.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // 2. Comprueba si la clase que se pide es ProfileViewModel
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            // 3. Si lo es, crea y devuelve una nueva instancia, pasándole el repositorio.
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(repository) as T
        }
        // 4. Si se pide crear un ViewModel desconocido, lanza una excepción.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
