// Archivo: app/src/main/java/com/movil/saferescue/ui/viewmodel/IncidentsViewModel.kt
package com.movil.saferescue.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.saferescue.data.local.foto.FotoEntity
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

// Estado para la lista principal de incidentes
data class IncidentsUiState(
    val isLoading: Boolean = true,
    val incidentsWithDetails: List<IncidentWithDetails> = emptyList(),
    val error: String? = null
)

// Estado para el diálogo de edición
data class EditIncidentState(
    val selectedIncident: IncidentWithDetails? = null,
    // El newImageUri ya no es necesario aquí, se pasa como parámetro
    val isSubmitting: Boolean = false,
    val error: String? = null
)

class IncidentsViewModel(
    private val repository: IncidenteRepository,
    private val userRepository: UserRepository, // Inyecta el repo de usuario
    private val applicationContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncidentsUiState())
    val uiState: StateFlow<IncidentsUiState> = _uiState.asStateFlow()

    private val _editState = MutableStateFlow(EditIncidentState())
    val editState: StateFlow<EditIncidentState> = _editState.asStateFlow()

    // Correcto: El ViewModel obtiene el ID del usuario del UserRepository.
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

    // --- ACCIONES DEL USUARIO ---

    // 1. Tomar un incidente
    fun onTakeIncidentClicked(incidentId: Long) {
        viewModelScope.launch {
            val userId = currentUserId.value // Obtiene el valor actual del Flow
            if (userId != null) {
                repository.takeIncident(incidentId, userId)
            } else {
                _uiState.update { it.copy(error = "Error de autenticación. Por favor, inicie sesión de nuevo.") }
            }
        }
    }

    // 2. Cerrar un incidente
    fun onCloseIncidentClicked(incidenteId: Long) {
        viewModelScope.launch {
            try {
                repository.closeIncident(incidenteId)
                // La UI se actualiza automáticamente gracias al Flow
            } catch (e: Exception) {
                Log.e("IncidentsViewModel", "Error al cerrar el incidente: ${e.message}")
                _uiState.update { it.copy(error = "Error al cerrar el incidente.") }
            }
        }
    }

    // 3. Abrir el diálogo para editar
    fun onEditIncidentClicked(incident: IncidentWithDetails) {
        _editState.value = EditIncidentState(selectedIncident = incident)
    }

    // 4. Cerrar el diálogo
    fun onDismissDialog() {
        _editState.value = EditIncidentState() // Resetea el estado para ocultar el diálogo
    }

    // --- INICIO DE LA CORRECCIÓN ---

    /**
     * 5. Guardar todos los cambios del diálogo de edición (título, detalle e imagen).
     *
     * @param newTitle El nuevo título del incidente.
     * @param newDetail El nuevo detalle del incidente.
     * @param newImageUri La nueva URI de la imagen (opcional).
     */
    fun onSaveChangesClicked(newTitle: String, newDetail: String, newImageUri: Uri?) {
        viewModelScope.launch {
            val currentEditState = _editState.value
            val incidentToUpdate = currentEditState.selectedIncident?.incident ?: return@launch

            _editState.update { it.copy(isSubmitting = true) }

            try {
                // Paso 1: Si hay una nueva imagen, guardarla y obtener su ID.
                // Si no, se mantiene el ID de la foto anterior.
                val newPhotoId = if (newImageUri != null) {
                    saveImageAndGetId(newImageUri)
                } else {
                    incidentToUpdate.foto_id
                }

                // Paso 2: Crear una copia del incidente con TODOS los datos actualizados.
                val updatedIncident = incidentToUpdate.copy(
                    titulo = newTitle,
                    detalle = newDetail,
                    foto_id = newPhotoId
                )

                // Paso 3: Llamar a un método genérico para actualizar el incidente en el repositorio.
                repository.updateIncidente(updatedIncident)

                // Paso 4: Cerrar el diálogo al terminar con éxito.
                onDismissDialog()

            } catch (e: Exception) {
                Log.e("IncidentsViewModel", "Error al guardar cambios: ${e.message}")
                _editState.update { it.copy(isSubmitting = false, error = "No se pudo guardar los cambios.") }
            }
        }
    }
    // --- FIN DE LA CORRECCIÓN ---


    /**
     * Guarda un archivo de imagen desde una URI, lo inserta en la BBDD y devuelve su ID.
     * Se hace 'private suspend' porque solo se usa dentro de este ViewModel.
     * Throws Exception on failure.
     */
    private suspend fun saveImageAndGetId(uri: Uri): Long {
        return withContext(Dispatchers.IO) {
            val fileName = "INCIDENT_${System.currentTimeMillis()}.jpg"
            val destinationFile = File(applicationContext.filesDir, fileName)

            // Usamos 'use' para garantizar que los streams se cierren automáticamente
            applicationContext.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            } ?: throw Exception("No se pudo abrir el stream para la URI: $uri")

            val newPhoto = FotoEntity(nombre = fileName, url = destinationFile.toURI().toString())
            repository.insertFotoAndGetId(newPhoto)
        }
    }

    /**
     * Limpia un mensaje de error una vez que ha sido mostrado en la UI.
     */
    fun onErrorShown() {
        _uiState.update { it.copy(error = null) }
        _editState.update { it.copy(error = null) }
    }

    fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}
