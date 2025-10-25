package com.movil.saferescue.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.saferescue.data.local.mensaje.MensajeConRemitente
import com.movil.saferescue.data.local.mensaje.MensajeEntity
import com.movil.saferescue.data.local.mensaje.MensajeUsuarioEntity
import com.movil.saferescue.data.repository.MensajeRepository
import com.movil.saferescue.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AlertasUiState(
    val alertas: List<MensajeEntity> = emptyList(),
    val contadorNoLeidas: Int = 0,
    val isLoading: Boolean = true,
    val errorMsg: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class MensajeViewModel(
    private val repository: MensajeRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _alertasUiState = MutableStateFlow(AlertasUiState())
    val alertasUiState: StateFlow<AlertasUiState> = _alertasUiState.asStateFlow()

    init {
        observarAlertas()
    }

    private fun observarAlertas() {
        viewModelScope.launch {
            combine(
                repository.todasLasAlertas,
                repository.contadorAlertasNoLeidasGlobal
            ) { alertas, contador ->
                Pair(alertas, contador)
            }
                .onStart { _alertasUiState.value = _alertasUiState.value.copy(isLoading = true) }
                .catch { throwable ->
                    _alertasUiState.value = _alertasUiState.value.copy(isLoading = false, errorMsg = "Error al cargar alertas: ${throwable.message}")
                }
                .collect { (alertas, contador) ->
                    _alertasUiState.value = _alertasUiState.value.copy(
                        isLoading = false,
                        alertas = alertas,
                        contadorNoLeidas = contador
                    )
                }
        }
    }

    fun marcarAlertasComoLeidas() {
        viewModelScope.launch {
            repository.marcarTodasLasAlertasComoLeidas()
        }
    }

    fun eliminarAlertas() {
        viewModelScope.launch {
            repository.eliminarTodasLasAlertas()
        }
    }

    val todosLosMensajesDeChat: Flow<List<MensajeConRemitente>> = repository.todosLosMensajesDeChat
        .catch { throwable ->
            _alertasUiState.value = _alertasUiState.value.copy(errorMsg = "Error al cargar el chat: ${throwable.message}")
        }

    fun enviarMensajeDeChat(contenido: String) {
        if (contenido.isNotBlank()) {
            val currentUserId = 3L
            viewModelScope.launch {
                repository.enviarMensajeDeChat(contenido, currentUserId)
            }
        }
    }

    fun eliminarMensaje(mensajeId: Long) {
        viewModelScope.launch {
            repository.eliminarMensajePorId(mensajeId)
        }
    }

    fun limpiarError() {
        _alertasUiState.value = _alertasUiState.value.copy(errorMsg = null)
    }

    val userNotifications = userRepository.loggedInUserId
        .flatMapLatest { userId ->
            if (userId == null) {
                flowOf(emptyList<MensajeUsuarioEntity>())
            } else {
                repository.observeNotificationsForUser(userId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val userUnreadCount = userRepository.loggedInUserId
        .flatMapLatest { userId ->
            if (userId == null) flowOf(0) else repository.getUnreadCountForUser(userId)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun deleteUserNotification(mensajeUsuarioId: Long) {
        viewModelScope.launch { repository.deleteUserNotification(mensajeUsuarioId) }
    }

    fun markNotificationAsRead(mensajeId: Long) {
        viewModelScope.launch { repository.markNotificationAsRead(mensajeId) }
    }

    fun createGlobalNotification(titulo: String, detalle: String) {
        viewModelScope.launch {
            val remitenteId = userRepository.loggedInUserId.value ?: 1L
            repository.crearAlertaGlobal(titulo, detalle, remitenteId)
        }
    }

    val isCurrentUserAdmin: Flow<Boolean> = userRepository.loggedInUserId
        .flatMapLatest { userId ->
            if (userId == null) {
                flowOf(false)
            } else {
                flow {
                    val profile = userRepository.getLoggedInUser()
                    emit(profile?.rolId == 1L)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)
}
