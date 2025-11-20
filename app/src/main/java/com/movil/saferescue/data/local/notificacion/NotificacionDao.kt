package com.movil.saferescue.data.local.notificacion

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificacionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificacion(notificacion: NotificacionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notificaciones: List<NotificacionEntity>)

    // 10L = ELIMINADA. Traemos todo lo que no esté eliminado.
    @Query("SELECT * FROM notificacion WHERE id_usuario_receptor = :userId AND id_estado != 10 ORDER BY fecha_creacion DESC")
    fun getNotificacionesForUser(userId: Long): Flow<List<NotificacionEntity>>

    // 8L = RECIBIDO (No leída).
    @Query("SELECT COUNT(*) FROM notificacion WHERE id_usuario_receptor = :userId AND id_estado = 8")
    fun getUnreadCountForUser(userId: Long): Flow<Int>

    // 9L = VISTO (Leída).
    @Query("UPDATE notificacion SET id_estado = 9 WHERE id_notificacion = :notificacionId")
    suspend fun markAsRead(notificacionId: Long)

    // 10L = ELIMINADA.
    @Query("UPDATE notificacion SET id_estado = 10 WHERE id_notificacion = :notificacionId")
    suspend fun markDeleted(notificacionId: Long)

    @Query("UPDATE notificacion SET id_estado = 10 WHERE id_usuario_receptor = :userId")
    suspend fun markAllDeletedForUser(userId: Long)

    @Query("UPDATE notificacion SET id_estado = 9 WHERE id_usuario_receptor = :userId AND id_estado = 8")
    suspend fun markAllAsReadForUser(userId: Long)
    
    @Query("SELECT * FROM notificacion ORDER BY fecha_creacion DESC")
    fun getAllNotificaciones(): Flow<List<NotificacionEntity>>
}
