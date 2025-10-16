package com.movil.saferescue.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.movil.saferescue.data.repository.NotificationRepository

class NotificationViewModelFactory(
    private val repository: NotificationRepository
) : ViewModelProvider.Factory {

    /**
     * Este método es llamado por el sistema para crear el ViewModel.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // 2. Comprueba si la clase que se pide es NotificationViewModel
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            // 3. Si lo es, crea y devuelve una nueva instancia, pasándole el repositorio.
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(repository) as T
        }
        // 4. Si se pide crear un ViewModel desconocido, lanza una excepción.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
