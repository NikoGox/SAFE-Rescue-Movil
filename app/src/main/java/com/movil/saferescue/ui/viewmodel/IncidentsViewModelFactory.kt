package com.movil.saferescue.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.movil.saferescue.data.repository.IncidenteRepository

class IncidentsViewModelFactory(
    private val incidenteRepository: IncidenteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IncidentsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IncidentsViewModel(incidenteRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
