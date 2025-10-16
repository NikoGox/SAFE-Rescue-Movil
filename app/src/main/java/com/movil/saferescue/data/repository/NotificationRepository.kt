package com.movil.saferescue.data.repository // O el paquete que corresponda

import com.movil.saferescue.data.local.notification.NotificationDao
import com.movil.saferescue.data.local.notification.NotificationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repositorio para gestionar las operaciones de datos de las notificaciones.
 * Siguiendo el patrón de UserRepository, este repositorio orquesta las interacciones
 * con NotificationDao y encapsula la lógica de negocio relacionada con las notificaciones.
 */
class NotificationRepository (
    private val notificationDao: NotificationDao
) {

    /**
     * Obtiene un flujo con todas las notificaciones desde la base de datos, ordenadas por fecha.
     * El uso de Flow permite que la UI observe los cambios en tiempo real.
     */
    fun getAllNotifications(): Flow<List<NotificationEntity>> {
        // Las operaciones de Flow de Room se ejecutan en su propio hilo, no es necesario withContext.
        return notificationDao.getAllNotifications()
    }

    /**
     * Obtiene un flujo con el conteo de notificaciones no leídas.
     * Ideal para mostrar un contador o "badge" en la UI.
     */
    fun getUnreadNotificationCount(): Flow<Int> {
        return notificationDao.getUnreadNotificationCount()
    }

    /**
     * Inserta una nueva notificación en la base de datos.
     * Se ejecuta dentro de un contexto de IO.
     * @param notification La entidad de notificación a insertar.
     * @return Un objeto Result que contiene el ID de la nueva notificación si tiene éxito,
     * o una excepción si falla.
     */
    suspend fun insertNotification(notification: NotificationEntity): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val newId = notificationDao.insertNotification(notification)
                Result.success(newId)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Busca y obtiene una notificación específica por su ID.
     * @param notificationId El ID de la notificación a buscar.
     * @return La NotificationEntity si se encuentra, o null si no existe.
     */
    suspend fun getNotificationById(notificationId: Long): NotificationEntity? {
        return withContext(Dispatchers.IO) {
            notificationDao.getNotificationById(notificationId)
        }
    }

    /**
     * Marca una notificación específica como leída actualizando su entidad.
     * @param notificationId El ID de la notificación a marcar como leída.
     * @return Un Result que indica si la operación fue exitosa.
     */
    suspend fun markNotificationAsRead(notificationId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val notification = notificationDao.getNotificationById(notificationId)
                if (notification != null) {
                    // Actualizamos el objeto y lo pasamos al método de actualización del DAO
                    val updatedNotification = notification.copy(isRead = true)
                    notificationDao.updateNotification(updatedNotification)
                    Result.success(Unit)
                } else {
                    Result.failure(IllegalArgumentException("Notificación con ID $notificationId no encontrada"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Marca todas las notificaciones no leídas como leídas.
     */
    suspend fun markAllNotificationsAsRead() {
        withContext(Dispatchers.IO) {
            notificationDao.markAllAsRead()
        }
    }

    /**
     * Elimina una notificación específica por su ID.
     */
    suspend fun deleteNotificationById(notificationId: Long) {
        withContext(Dispatchers.IO) {
            notificationDao.deleteNotificationById(notificationId)
        }
    }

    /**
     * Elimina todas las notificaciones de la base de datos.
     */
    suspend fun deleteAllNotifications() {
        withContext(Dispatchers.IO) {
            notificationDao.deleteAllNotifications()
        }
    }
}
