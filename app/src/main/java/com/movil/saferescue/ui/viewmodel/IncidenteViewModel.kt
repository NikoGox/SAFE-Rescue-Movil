package com.movil.saferescue.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.saferescue.data.local.foto.FotoEntity
import com.movil.saferescue.data.local.incidente.IncidenteEntity
import com.movil.saferescue.data.local.incidente.IncidenteEstado
import com.movil.saferescue.data.local.incidente.IncidentWithDetails
import com.movil.saferescue.data.repository.IncidenteRepository
import com.movil.saferescue.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class IncidentsUiState(
    val isLoading: Boolean = true,
    val incidentsWithDetails: List<IncidentWithDetails> = emptyList(),
    val error: String? = null
)

data class EditIncidentState(
    val selectedIncident: IncidentWithDetails? = null,
    val isSubmitting: Boolean = false,
    val error: String? = null
)

data class CreateIncidentState(
    val isCreating: Boolean = false,
    val createSuccess: Boolean = false,
    val error: String? = null
)

class IncidenteViewModel(
    private val repository: IncidenteRepository,
    private val userRepository: UserRepository, 
    private val applicationContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncidentsUiState())
    val uiState: StateFlow<IncidentsUiState> = _uiState.asStateFlow()

    private val _editState = MutableStateFlow(EditIncidentState())
    val editState: StateFlow<EditIncidentState> = _editState.asStateFlow()

    private val _createState = MutableStateFlow(CreateIncidentState())
    val createState: StateFlow<CreateIncidentState> = _createState.asStateFlow()

    val currentUserId: StateFlow<Long?> = userRepository.loggedInUserId

    init {
        loadIncidents()
    }

    private fun loadIncidents() {
        viewModelScope.launch {
            repository.getAllIncidentsWithDetails().collect { incidents ->
                _uiState.update { it.copy(isLoading = false, incidentsWithDetails = incidents) }
            }
        }
    }

    fun crearIncidente(
        titulo: String,
        detalle: String,
        imageUri: Uri?,
        latitud: String,
        longitud: String,
        comuna: String,
        region: String,
        direccion: String
    ) {
        if (titulo.length !in 10..30) {
            _createState.update { it.copy(error = "El título debe tener entre 10 y 30 caracteres.") }
            return
        }
        if (detalle.length !in 10..100) {
            _createState.update { it.copy(error = "El detalle debe tener entre 10 y 100 caracteres.") }
            return
        }
        val lat = latitud.toDoubleOrNull()
        if (latitud.isNotBlank() && (lat == null || latitud.length > 15)) {
            _createState.update { it.copy(error = "La latitud ingresada no es válida.") }
            return
        }
        val lon = longitud.toDoubleOrNull()
        if (longitud.isNotBlank() && (lon == null || longitud.length > 15)) {
            _createState.update { it.copy(error = "La longitud ingresada no es válida.") }
            return
        }
        if (region.isEmpty()) {
            _createState.update { it.copy(error = "Debe seleccionar una región.") }
            return
        }
        if (comuna.isNotBlank() && comuna.length !in 2..20) {
            _createState.update { it.copy(error = "La comuna debe tener entre 2 y 20 caracteres.") }
            return
        }
        if (direccion.isNotBlank() && direccion.length !in 5..30) {
            _createState.update { it.copy(error = "La dirección debe tener entre 5 y 30 caracteres.") }
            return
        }

        viewModelScope.launch {
            _createState.update { it.copy(isCreating = true, createSuccess = false, error = null) }
            try {
                val photoId = imageUri?.let { saveImageAndGetId(it) }
                val incidente = IncidenteEntity(
                    titulo = titulo,
                    detalle = detalle,
                    foto_id = photoId,
                    latitud = lat,
                    longitud = lon,
                    comuna = comuna.takeIf { it.isNotBlank() },
                    region = region.takeIf { it.isNotBlank() },
                    direccion = direccion.takeIf { it.isNotBlank() }
                )
                repository.insertIncidente(incidente)
                _createState.update { it.copy(isCreating = false, createSuccess = true) }
            } catch (e: Exception) {
                _createState.update { it.copy(isCreating = false, error = "Error al crear el incidente: ${e.message}") }
            }
        }
    }

    fun onCreationHandled() {
        _createState.value = CreateIncidentState()
    }

    fun onTakeIncidentClicked(incidentId: Long) {
        viewModelScope.launch {
            val userId = currentUserId.value
            if (userId != null) {
                repository.takeIncident(incidentId, userId)
            } else {
                _uiState.update { it.copy(error = "Error de autenticación. Por favor, inicie sesión de nuevo.") }
            }
        }
    }

    fun onCloseIncidentClicked(incidenteId: Long) {
        viewModelScope.launch {
            try {
                repository.closeIncident(incidenteId)
            } catch (e: Exception) {
                Log.e("IncidenteViewModel", "Error al cerrar el incidente: ${e.message}")
                _uiState.update { it.copy(error = "Error al cerrar el incidente.") }
            }
        }
    }

    fun onEditIncidentClicked(incident: IncidentWithDetails) {
        _editState.value = EditIncidentState(selectedIncident = incident)
    }

    fun onDismissDialog() {
        _editState.value = EditIncidentState()
    }

    fun onSaveChangesClicked(newTitle: String, newDetail: String, newImageUri: Uri?, bomberoUsername: String?) {
        viewModelScope.launch {
            val currentEditState = _editState.value
            val incidentToUpdate = currentEditState.selectedIncident?.incident ?: return@launch

            _editState.update { it.copy(isSubmitting = true) }

            try {
                val bomberoId = bomberoUsername?.let { username ->
                    val user = userRepository.getUserByUsername(username)
                    if (user?.rol_id == 2L) { 
                        user.id
                    } else {
                        null
                    }
                }

                val newPhotoId = if (newImageUri != null) {
                    saveImageAndGetId(newImageUri)
                } else {
                    incidentToUpdate.foto_id
                }

                val updatedIncident = incidentToUpdate.copy(
                    titulo = newTitle,
                    detalle = newDetail,
                    foto_id = newPhotoId,
                    asignadoA = bomberoId ?: incidentToUpdate.asignadoA, 
                    estado = if (bomberoId != null) IncidenteEstado.ASIGNADO.name else incidentToUpdate.estado
                )

                repository.updateIncidente(updatedIncident)

                onDismissDialog()

            } catch (e: Exception) {
                Log.e("IncidenteViewModel", "Error al guardar cambios: ${e.message}")
                _editState.update { it.copy(isSubmitting = false, error = "No se pudo guardar los cambios.") }
            }
        }
    }

    private suspend fun saveImageAndGetId(uri: Uri): Long {
        return withContext(Dispatchers.IO) {
            val fileName = "INCIDENT_${System.currentTimeMillis()}.jpg"
            val destinationFile = File(applicationContext.filesDir, fileName)

            applicationContext.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            } ?: throw Exception("No se pudo abrir el stream para la URI: $uri")

            val newPhoto = FotoEntity(nombre = fileName, url = destinationFile.toURI().toString())
            repository.insertFotoAndGetId(newPhoto)
        }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(error = null) }
        _editState.update { it.copy(error = null) }
    }

    fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}