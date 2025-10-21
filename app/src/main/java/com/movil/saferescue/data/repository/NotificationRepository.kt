package com.movil.saferescue.data.repository

import com.movil.saferescue.data.local.notification.NotificationDao
import com.movil.saferescue.data.local.notification.NotificationEntity
import com.movil.saferescue.data.local.notification.NotificationWithSender
import com.movil.saferescue.data.local.user.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repositorio para gestionar las operaciones de datos de las notificaciones.
 * Esta clase orquesta las interacciones con NotificationDao y UserDao para
 * encapsular la lógica de negocio.
 * Se instancia manualmente, sin usar inyección de dependencias.
 */
class NotificationRepository(
    private val notificationDao: NotificationDao,
    private val userDao: UserDao
) {

    // IDs para los tipos de mensaje (es una buena práctica definirlos como constantes)
    private val TIPO_ALERTA_ID = 1L
    private val TIPO_MENSAJE_ID = 2L

    // --- LÓGICA PARA LA PANTALLA DE NOTIFICACIONES (Alertas del sistema) ---

    /**
     * Obtiene un flujo solo con las notificaciones de tipo 'alerta'.
     */
    val allSystemNotifications: Flow<List<NotificationEntity>> = notificationDao.getNotificationsByTipoId(TIPO_ALERTA_ID)

    /**
     * Obtiene un flujo con el conteo de notificaciones no leídas de tipo 'alerta'.
     */
    val unreadSystemNotificationCount: Flow<Int> = notificationDao.getUnreadCountByTipoId(TIPO_ALERTA_ID)

    /**
     * Marca todas las notificaciones de tipo 'alerta' como leídas.
     */
    suspend fun markAllSystemNotificationsAsRead() {
        withContext(Dispatchers.IO) {
            notificationDao.markAllAsReadByTipoId(TIPO_ALERTA_ID)
        }
    }

    /**
     * Elimina todas las notificaciones de tipo 'alerta'.
     */
    suspend fun deleteAllSystemNotifications() {
        withContext(Dispatchers.IO) {
            notificationDao.deleteAllByTipoId(TIPO_ALERTA_ID)
        }
    }

    // --- LÓGICA PARA LA PANTALLA DE CHAT ---

    /**
     * Obtiene un flujo de mensajes con la información del remitente ya incluida.
     */
    val allChatMessages: Flow<List<NotificationWithSender>> = notificationDao.getMessagesWithSender(TIPO_MENSAJE_ID)

    /**
     * Guarda un mensaje enviado por el usuario y simula una respuesta del soporte.
     * @param content El texto del mensaje.
     * @param senderId El ID del usuario que envía el mensaje.
     */
    suspend fun sendMessage(content: String, senderId: Long) {
        withContext(Dispatchers.IO) {
            // 1. Guarda el mensaje del usuario
            val userMessage = NotificationEntity(
                titulo = "Mensaje Enviado",
                mensaje = content,
                fechaSubida = System.currentTimeMillis(),
                isRead = true,
                tipo_mensaje = TIPO_MENSAJE_ID,
                remitente_id = senderId
            )
            notificationDao.insertNotification(userMessage)

            // Simulación de retraso antes de la respuesta del bot
            kotlinx.coroutines.delay(1500)
            val supportUserId = userDao.getByUsername("Soporte")?.id ?: 1L

            val botResponse = NotificationEntity(
                titulo = "Nuevo Mensaje de Soporte",
                mensaje = "Hemos recibido tu mensaje. Un agente te atenderá pronto.",
                fechaSubida = System.currentTimeMillis(),
                isRead = false,
                tipo_mensaje = TIPO_MENSAJE_ID,
                remitente_id = supportUserId
            )
            notificationDao.insertNotification(botResponse)
        }
    }

    // --- FUNCIONES GENERALES (SIN FILTRO DE TIPO) ---

    /**
     * Elimina una notificación específica por su ID. Útil para ambas pantallas.
     */
    suspend fun deleteNotificationById(notificationId: Long) {
        withContext(Dispatchers.IO) {
            notificationDao.deleteNotificationById(notificationId)
        }
    }
}
