package com.movil.saferescue.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.saferescue.data.local.incidente.IncidenteEntity
import com.movil.saferescue.data.repository.IncidenteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- 1. NUEVO DATA CLASS PARA COMBINAR DATOS ---
data class IncidentWithDetails(
    val incident: IncidenteEntity,
    val photoUrl: String? // URL de la foto asociada
)

// --- 2. ACTUALIZAR EL ESTADO DE LA UI ---
data class IncidentsUiState(
    val isLoading: Boolean = true,
    val incidentsWithDetails: List<IncidentWithDetails> = emptyList(), // <-- Usamos la nueva clase
    val error: String? = null
)

class IncidentsViewModel(private val repository: IncidenteRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(IncidentsUiState())
    val uiState: StateFlow<IncidentsUiState> = _uiState.asStateFlow()

    init {
        loadIncidents()
    }

    private fun loadIncidents() {
        viewModelScope.launch {
            repository.getAllIncidentes()
                .catch { exception ->
                    _uiState.update { it.copy(isLoading = false, error = "Error al cargar incidentes: ${exception.message}") }
                }
                .collect { incidentList ->
                    // --- 3. LÓGICA PARA COMBINAR INCIDENTES CON FOTOS ---
                    val detailedList = incidentList.map { incident ->
                        // Por cada incidente, buscamos su URL de foto usando el repositorio
                        val url = repository.getFotoUrlById(incident.foto_id)
                        IncidentWithDetails(incident = incident, photoUrl = url)
                    }
                    _uiState.update { it.copy(isLoading = false, incidentsWithDetails = detailedList) }
                }
        }
    }

    fun onTakeIncidentClicked(incidentId: Long) {
        println("Incidente $incidentId tomado")
    }

    fun formatDate(timestamp: Long): String {
        return try {
            val date = Date(timestamp)
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            format.format(date)
        } catch (e: Exception) {
            "Fecha inválida"
        }
    }
}
