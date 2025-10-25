package com.movil.saferescue.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.movil.saferescue.data.repository.MensajeRepository
import com.movil.saferescue.data.repository.UserRepository

/**
 * Factory para crear instancias de MensajeViewModel.
 * Esto es necesario porque nuestro ViewModel tiene un constructor con dependencias (el repositorio),
 * y el sistema necesita una forma de saber cómo instanciarlo.
 */
class MensajeViewModelFactory(
    // 3. RENOMBRADO DEL REPOSITORIO
    private val repository: MensajeRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    /**
     * Este método es llamado por el sistema para crear el ViewModel.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // 4. Comprueba si la clase que se pide es MensajeViewModel
        if (modelClass.isAssignableFrom(MensajeViewModel::class.java)) {
            // 5. Si lo es, crea y devuelve una nueva instancia, pasándole el repositorio.
            @Suppress("UNCHECKED_CAST")
            return MensajeViewModel(repository, userRepository) as T
        }
        // 6. Si se pide crear un ViewModel desconocido, lanza una excepción.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
