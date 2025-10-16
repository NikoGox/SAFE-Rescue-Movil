package com.movil.saferescue.data.local.notification

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Objeto de Acceso a Datos (DAO) para la entidad NotificationEntity.
 * Define los métodos para interactuar con la tabla 'notificaciones' en la base de datos.
 */
@Dao
interface NotificationDao {

    /**
     * Inserta una notificación. OnConflictStrategy.ABORT lanzará una excepción si el ID ya existe.
     * Retorna el nuevo 'rowId' de la fila insertada.
     * @param notification La entidad de notificación a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNotification(notification: NotificationEntity): Long

    /**
     * Obtiene todas las notificaciones, ordenadas por fecha descendente.
     * Usar Flow permite que la UI se actualice automáticamente cuando los datos cambian.
     * @return Un Flow que emite la lista de notificaciones.
     */
    @Query("SELECT * FROM notificaciones ORDER BY fechaSubida DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    /**
     * Obtiene una notificación específica por su ID. El ID debe ser Long para coincidir con la entidad.
     * @param notificationId El ID de la notificación a buscar.
     */
    @Query("SELECT * FROM notificaciones WHERE id = :notificationId")
    suspend fun getNotificationById(notificationId: Long): NotificationEntity?

    /**
     * Actualiza una notificación existente.
     * @param notification La entidad de notificación con los datos actualizados.
     */
    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    /**
     * Elimina una notificación por su ID. El ID debe ser Long.
     * @param notificationId El ID de la notificación a eliminar.
     */
    @Query("DELETE FROM notificaciones WHERE id = :notificationId")
    suspend fun deleteNotificationById(notificationId: Long)

    /**
     * Elimina todas las notificaciones de la tabla.
     */
    @Query("DELETE FROM notificaciones")
    suspend fun deleteAllNotifications()

    /**
     * Obtiene un conteo en tiempo real de las notificaciones no leídas.
     * Ideal para mostrar un badge o contador en la UI.
     * @return Un Flow que emite el número de notificaciones no leídas.
     */
    @Query("SELECT COUNT(*) FROM notificaciones WHERE isRead = 0")
    fun getUnreadNotificationCount(): Flow<Int>

    /**
     * Marca todas las notificaciones como leídas.
     */
    @Query("UPDATE notificaciones SET isRead = 1 WHERE isRead = 0")
    suspend fun markAllAsRead()
}
