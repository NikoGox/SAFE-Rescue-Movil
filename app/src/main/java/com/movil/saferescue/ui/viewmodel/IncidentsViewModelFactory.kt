package com.movil.saferescue.ui.viewmodel

import android.content.Context // <<< CORRECCIÓN 1: Importar Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.movil.saferescue.data.local.foto.FotoDao // <<< CORRECCIÓN 2: Importar FotoDao
import com.movil.saferescue.data.repository.IncidenteRepository
import com.movil.saferescue.data.repository.UserRepository

class IncidentsViewModelFactory(
    private val incidenteRepository: IncidenteRepository,
    private val userRepository: UserRepository,
    private val applicationContext: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IncidentsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IncidentsViewModel(incidenteRepository, userRepository, applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

