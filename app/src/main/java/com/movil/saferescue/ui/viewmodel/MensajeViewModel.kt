package com.movil.saferescue.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.saferescue.data.local.mensaje.MensajeConRemitente
import com.movil.saferescue.data.local.notificacion.NotificacionEntity
import com.movil.saferescue.data.repository.ConversacionItem
import com.movil.saferescue.data.repository.MensajeRepository
import com.movil.saferescue.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AlertasUiState(
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

    // Estado para saber qué conversación está activa (null = viendo lista de chats)
    private val _activeConversationId = MutableStateFlow<Long?>(null)
    val activeConversationId: StateFlow<Long?> = _activeConversationId.asStateFlow()

    fun selectConversation(conversationId: Long?) {
        _activeConversationId.value = conversationId
    }

    // Lista de Conversaciones del usuario
    val userConversations: StateFlow<List<ConversacionItem>> = userRepository.loggedInUserId
        .flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList()) else repository.getConversacionesForUser(userId)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Mensajes de la conversación activa
    val activeChatMessages: StateFlow<List<MensajeConRemitente>> = _activeConversationId
        .flatMapLatest { convId ->
            if (convId == null) {
                flowOf(emptyList())
            } else {
                repository.getChatMessages(convId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        
    // Current User ID for UI logic (right/left alignment)
    val currentUserId: StateFlow<Long?> = userRepository.loggedInUserId
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun enviarMensajeDeChat(contenido: String) {
        if (contenido.isNotBlank()) {
             viewModelScope.launch {
                 val currentUserId = userRepository.loggedInUserId.firstOrNull() ?: return@launch
                 val currentConvId = _activeConversationId.value
                 
                 repository.enviarMensajeDeChat(contenido, currentUserId, currentConvId)
            }
        }
    }

    fun eliminarMensaje(mensajeId: Long) {
        viewModelScope.launch {
            repository.eliminarMensajePorId(mensajeId)
        }
    }
    
    // --- Notification Logic ---

    val userNotifications = userRepository.loggedInUserId
        .flatMapLatest { userId ->
            if (userId == null) {
                flowOf(emptyList<NotificacionEntity>())
            } else {
                repository.getNotificacionesForUser(userId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val userUnreadCount = userRepository.loggedInUserId
        .flatMapLatest { userId ->
            if (userId == null) flowOf(0) else repository.getUnreadNotificacionesCount(userId)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun deleteUserNotification(notificacionId: Long) {
        viewModelScope.launch { repository.deleteNotification(notificacionId) }
    }

    fun markNotificationAsRead(notificacionId: Long) {
        viewModelScope.launch { repository.markNotificationAsRead(notificacionId) }
    }

    fun createGlobalNotification(titulo: String, detalle: String) {
        viewModelScope.launch {
            val remitenteId = userRepository.loggedInUserId.value ?: 1L
            repository.crearAlertaGlobal(titulo, detalle, remitenteId)
        }
    }
    
    fun limpiarError() {
        _alertasUiState.value = _alertasUiState.value.copy(errorMsg = null)
    }

    // --- Permissions ---

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

    val isCurrentUserBombero: Flow<Boolean> = userRepository.loggedInUserId
        .flatMapLatest { userId ->
            if (userId == null) {
                flowOf(false)
            } else {
                flow {
                    val profile = userRepository.getLoggedInUser()
                    emit(profile?.rolId == 2L)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)
}
