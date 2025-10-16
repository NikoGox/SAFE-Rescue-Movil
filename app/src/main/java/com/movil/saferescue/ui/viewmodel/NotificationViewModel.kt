package com.movil.saferescue.ui.viewmodel // O el paquete que corresponda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.saferescue.data.local.notification.NotificationEntity
import com.movil.saferescue.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ----------------- ESTADO DE LA UI (observable con StateFlow) -----------------

/**
 * Representa el estado de la pantalla de notificaciones.
 *
 * @param notifications Lista de notificaciones a mostrar.
 * @param unreadCount Conteo de notificaciones no leídas, para mostrar en un "badge".
 * @param isLoading Indica si se están cargando las notificaciones iniciales.
 * @param errorMsg Mensaje de error general para mostrar en la UI.
 */
data class NotificationUiState(
    val notifications: List<NotificationEntity> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMsg: String? = null
)

// ----------------- VIEWMODEL -----------------

/**
 * ViewModel para gestionar la lógica y el estado de la pantalla de notificaciones.
 */
class NotificationViewModel (
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    // StateFlow privado y mutable para gestionar el estado internamente.
    private val _uiState = MutableStateFlow(NotificationUiState())
    // StateFlow público e inmutable para que la UI lo observe.
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        // Al inicializar el ViewModel, comenzamos a observar las notificaciones y el conteo.
        observeAllNotifications()
        observeUnreadCount()
    }

    /**
     * Se suscribe al flujo de notificaciones del repositorio para mantener la UI
     * actualizada en tiempo real.
     */
    private fun observeAllNotifications() {
        viewModelScope.launch {
            notificationRepository.getAllNotifications()
                .onStart {
                    // Al comenzar la recolección, indicamos que estamos cargando.
                    _uiState.update { it.copy(isLoading = true) }
                }
                .catch { throwable ->
                    // Si ocurre un error en el flujo, lo mostramos en el estado.
                    _uiState.update {
                        it.copy(isLoading = false, errorMsg = "Error al cargar notificaciones")
                    }
                }
                .collect { notifications ->
                    // Cuando recibimos una nueva lista de notificaciones, actualizamos el estado.
                    _uiState.update {
                        it.copy(isLoading = false, notifications = notifications)
                    }
                }
        }
    }

    /**
     * Se suscribe al flujo del conteo de notificaciones no leídas.
     */
    private fun observeUnreadCount() {
        viewModelScope.launch {
            notificationRepository.getUnreadNotificationCount()
                .catch { _ -> /* Opcional: manejar error de conteo */ }
                .collect { count ->
                    _uiState.update { it.copy(unreadCount = count) }
                }
        }
    }

    /**
     * Marca una notificación específica como leída.
     * Llamado desde la UI cuando el usuario interactúa con una notificación.
     */
    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            // Llamamos al repositorio sin esperar un resultado explícito,
            // ya que el Flow actualizará la UI automáticamente.
            notificationRepository.markNotificationAsRead(notificationId)
        }
    }

    /**
     * Elimina una notificación específica.
     * Llamado desde la UI, por ejemplo, al deslizar para eliminar.
     */
    fun deleteNotification(notificationId: Long) {
        viewModelScope.launch {
            notificationRepository.deleteNotificationById(notificationId)
        }
    }

    /**
     * Elimina todas las notificaciones.
     * Llamado desde una opción en el menú, por ejemplo.
     */
    fun deleteAllNotifications() {
        viewModelScope.launch {
            notificationRepository.deleteAllNotifications()
        }
    }

    /**
     * Marca todas las notificaciones como leídas.
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllNotificationsAsRead()
        }
    }

    /**
     * Limpia cualquier mensaje de error mostrado en la UI.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMsg = null) }
    }
}
