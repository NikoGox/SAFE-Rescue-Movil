package com.movil.saferescue.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.saferescue.data.local.notification.NotificationEntity
// CAMBIO 1: Importar la nueva data class
import com.movil.saferescue.data.local.notification.NotificationWithSender
import com.movil.saferescue.data.repository.NotificationRepository
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
 * Representa el estado de la pantalla de notificaciones del sistema (Alertas).
 * No necesita cambios, ya que sigue trabajando con NotificationEntity.
 */
data class NotificationUiState(
    val notifications: List<NotificationEntity> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMsg: String? = null
)

// ----------------- VIEWMODEL -----------------

/**
 * ViewModel para gestionar la lógica y el estado de la pantalla de Notificaciones y de Chat.
 * Orquesta los datos provenientes del NotificationRepository para ambas vistas.
 */
class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    // --- LÓGICA PARA LA PANTALLA DE NOTIFICACIONES ---

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        observeSystemNotifications()
    }

    private fun observeSystemNotifications() {
        viewModelScope.launch {
            combine(
                repository.allSystemNotifications,
                repository.unreadSystemNotificationCount
            ) { notifications, unreadCount ->
                Pair(notifications, unreadCount)
            }
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMsg = "Error al cargar notificaciones: ${throwable.message}")
                    }
                }
                .collect { (notifications, unreadCount) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = notifications,
                            unreadCount = unreadCount
                        )
                    }
                }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllSystemNotificationsAsRead()
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch {
            repository.deleteAllSystemNotifications()
        }
    }

    // --- LÓGICA PARA LA PANTALLA DE CHAT ---

    /**
     * Expone directamente el flujo de mensajes enriquecidos (con datos del remitente).
     * La UI del chat recolectará este flujo.
     */
    // CAMBIO 2: El tipo del Flow ahora es NotificationWithSender
    val allChatMessages: Flow<List<NotificationWithSender>> = repository.allChatMessages
        .catch { throwable ->
            // Opcional: Manejar el error, por ejemplo, actualizando un StateFlow de error para el chat.
            _uiState.update { it.copy(errorMsg = "Error al cargar el chat: ${throwable.message}") }
        }

    /**
     * Delega la acción de enviar un mensaje al repositorio.
     * @param content El texto del mensaje a enviar.
     */
    fun sendMessage(content: String) {
        if (content.isNotBlank()) {
            // CAMBIO 3: La función sendMessage ahora necesita el ID del usuario actual.
            // En una app real, este ID vendría de una sesión de usuario guardada (ej. DataStore o SharedPreferences).
            // Por ahora, simularemos que el usuario actual siempre tiene el ID 3 (el "Ciudadano").
            // ¡Este ID DEBE ser uno que exista en tu base de datos!
            val currentUserId = 3L

            viewModelScope.launch {
                // Llamamos al método actualizado del repositorio
                repository.sendMessage(content, currentUserId)
            }
        }
    }


    // --- FUNCIONES COMUNES PARA AMBAS PANTALLAS ---

    /**
     * Elimina una notificación/mensaje específico por su ID.
     * Puede ser llamado desde ambas pantallas.
     */
    fun deleteNotification(notificationId: Long) {
        viewModelScope.launch {
            repository.deleteNotificationById(notificationId)
        }
    }

    /**
     * Limpia cualquier mensaje de error mostrado en la UI de Notificaciones.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMsg = null) }
    }
}
