package com.movil.saferescue.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// 1. IMPORTACIONES ACTUALIZADAS
import com.movil.saferescue.data.local.mensaje.MensajeEntity
import com.movil.saferescue.data.local.mensaje.MensajeConRemitente
import com.movil.saferescue.data.repository.MensajeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ----------------- ESTADO DE LA UI (observable con StateFlow) -----------------

/**
 * Representa el estado de la pantalla de Alertas del sistema.
 */
// 2. RENOMBRADO DE LA DATA CLASS
data class AlertasUiState(
    val alertas: List<MensajeEntity> = emptyList(),
    val contadorNoLeidas: Int = 0,
    val isLoading: Boolean = true,
    val errorMsg: String? = null
)

// ----------------- VIEWMODEL -----------------

/**
 * ViewModel para gestionar la lógica y el estado de la pantalla de Alertas y de Chat.
 */
class MensajeViewModel(
    private val repository: MensajeRepository
) : ViewModel() {
    private val _alertasUiState = MutableStateFlow(AlertasUiState())
    val alertasUiState: StateFlow<AlertasUiState> = _alertasUiState.asStateFlow()

    init {
        // 6. RENOMBRADO DE LA FUNCIÓN DE INICIO
        observarAlertas()
    }

    private fun observarAlertas() {
        viewModelScope.launch {
            combine(
                // 7. USO DE LAS PROPIEDADES RENOMBRADAS DEL REPOSITORIO
                repository.todasLasAlertas,
                repository.contadorAlertasNoLeidas
            ) { alertas, contador ->
                Pair(alertas, contador)
            }
                .onStart { _alertasUiState.update { it.copy(isLoading = true) } }
                .catch { throwable ->
                    _alertasUiState.update {
                        it.copy(isLoading = false, errorMsg = "Error al cargar alertas: ${throwable.message}")
                    }
                }
                .collect { (alertas, contador) ->
                    _alertasUiState.update {
                        it.copy(
                            isLoading = false,
                            alertas = alertas,
                            contadorNoLeidas = contador
                        )
                    }
                }
        }
    }

    fun marcarAlertasComoLeidas() {
        viewModelScope.launch {
            // 8. LLAMADA A LA FUNCIÓN RENOMBRADA DEL REPOSITORIO
            repository.marcarTodasLasAlertasComoLeidas()
        }
    }

    fun eliminarAlertas() {
        viewModelScope.launch {
            repository.eliminarTodasLasAlertas()
        }
    }

    // --- LÓGICA PARA LA PANTALLA DE CHAT ---

    /**
     * Expone directamente el flujo de mensajes enriquecidos (con datos del remitente).
     */
    // 9. RENOMBRADO DEL FLOW y uso de la propiedad correcta del repo
    val todosLosMensajesDeChat: Flow<List<MensajeConRemitente>> = repository.todosLosMensajesDeChat
        .catch { throwable ->
            // La lógica para manejar errores se mantiene intacta
            _alertasUiState.update { it.copy(errorMsg = "Error al cargar el chat: ${throwable.message}") }
        }

    /**
     * Delega la acción de enviar un mensaje al repositorio.
     */
    // 10. RENOMBRADO DE LA FUNCIÓN
    fun enviarMensajeDeChat(contenido: String) {
        if (contenido.isNotBlank()) {
            val currentUserId = 3L // La lógica de simulación no se toca
            viewModelScope.launch {
                repository.enviarMensajeDeChat(contenido, currentUserId)
            }
        }
    }

    // --- FUNCIONES COMUNES PARA AMBAS PANTALLAS ---

    /**
     * Elimina un mensaje/alerta específico por su ID.
     */
    // 11. RENOMBRADO DE LA FUNCIÓN COMÚN
    fun eliminarMensaje(mensajeId: Long) {
        viewModelScope.launch {
            repository.eliminarMensajePorId(mensajeId)
        }
    }

    /**
     * Limpia cualquier mensaje de error mostrado.
     */
    fun limpiarError() {
        _alertasUiState.update { it.copy(errorMsg = null) }
    }
}
