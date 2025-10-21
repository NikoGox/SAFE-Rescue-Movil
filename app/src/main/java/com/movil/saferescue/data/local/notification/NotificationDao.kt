package com.movil.saferescue.data.local.notification

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


data class NotificationWithSender(
    @Embedded
    val notification: NotificationEntity, // La notificación completa

    @ColumnInfo(name = "remitente_nombre")
    val senderName: String, // El nombre del usuario remitente

    @ColumnInfo(name = "remitente_foto_url")
    val senderPhotoUrl: String? // La URL de la foto del remitente (opcional)
)


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
     * Obtiene todas las notificaciones, ordenadas por fecha descendente. (Uso general, si se necesita)
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
     * Obtiene un conteo en tiempo real de las notificaciones no leídas. (Uso general)
     * Ideal para mostrar un badge o contador en la UI.
     * @return Un Flow que emite el número de notificaciones no leídas.
     */
    @Query("SELECT COUNT(*) FROM notificaciones WHERE isRead = 0")
    fun getUnreadNotificationCount(): Int

    /**
     * Marca todas las notificaciones como leídas. (Uso general)
     */
    @Query("UPDATE notificaciones SET isRead = 1 WHERE isRead = 0")
    suspend fun markAllAsRead()

    // --- NUEVAS FUNCIONES ESPECIALIZADAS POR TIPO ---

    /**
     * Obtiene un flujo de notificaciones filtradas por su tipo de mensaje ID.
     * @param tipoId El ID del tipo de notificación a obtener (ej. 1 para "alerta").
     */
    // CORRECCIÓN 1: Se usa la columna 'tipo_mensaje_id' y se recibe un Long.
    @Query("SELECT * FROM notificaciones WHERE tipo_mensaje_id = :tipoId ORDER BY fechaSubida DESC")
    fun getNotificationsByTipoId(tipoId: Long): Flow<List<NotificationEntity>>

    /**
     * Elimina todas las notificaciones de un tipo específico.
     * @param tipoId El ID del tipo de notificaciones a eliminar.
     */
    @Query("DELETE FROM notificaciones WHERE tipo_mensaje_id = :tipoId")
    suspend fun deleteAllByTipoId(tipoId: Long)

    /**
     * Obtiene el conteo de notificaciones no leídas de un tipo específico.
     * @param tipoId El ID del tipo de notificación a contar.
     */
    @Query("SELECT COUNT(*) FROM notificaciones WHERE isRead = 0 AND tipo_mensaje_id = :tipoId")
    fun getUnreadCountByTipoId(tipoId: Long): Flow<Int>

    /**
     * Marca todas las notificaciones de un tipo específico como leídas.
     * @param tipoId El ID del tipo de notificaciones a marcar como leídas.
     */
    @Query("UPDATE notificaciones SET isRead = 1 WHERE isRead = 0 AND tipo_mensaje_id = :tipoId")
    suspend fun markAllAsReadByTipoId(tipoId: Long)

    // --- NUEVA FUNCIÓN CON JOIN PARA EL CHAT ---

    /**
     * Obtiene los mensajes (notificaciones de tipo mensaje) junto con la información
     * del remitente (nombre y URL de la foto) usando un JOIN con las tablas de usuarios y fotos.
     * @param tipoId El ID correspondiente al tipo "mensaje".
     * @return Un Flow que emite una lista de objetos NotificationWithSender.
     */
    // CORRECCIÓN 2: Se seleccionan las columnas explícitamente para evitar ambigüedades.
    @Query("""
        SELECT 
            notificaciones.*, 
            users.name as remitente_nombre,
            fotos.url as remitente_foto_url
        FROM notificaciones
        JOIN users ON notificaciones.remitente_id = users.id
        LEFT JOIN fotos ON users.foto_id = fotos.id
        WHERE notificaciones.tipo_mensaje_id = :tipoId
        ORDER BY notificaciones.fechaSubida ASC
    """)
    fun getMessagesWithSender(tipoId: Long): Flow<List<NotificationWithSender>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notificaciones: List<NotificationEntity>)


}
