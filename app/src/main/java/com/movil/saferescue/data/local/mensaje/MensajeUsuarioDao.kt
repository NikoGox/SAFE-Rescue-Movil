package com.movil.saferescue.data.local.mensaje

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MensajeUsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mensajes: List<MensajeUsuarioEntity>)

    @Query("SELECT * FROM mensajes_usuario WHERE user_id = :userId AND deleted = 0 ORDER BY fechaSubida DESC")
    fun getNotificationsForUser(userId: Long): Flow<List<MensajeUsuarioEntity>>

    @Query("UPDATE mensajes_usuario SET deleted = 1 WHERE id = :mensajeId")
    suspend fun markDeleted(mensajeId: Long)

    // Nueva función para marcar una notificación específica como leída
    @Query("UPDATE mensajes_usuario SET isRead = 1 WHERE id = :mensajeId")
    suspend fun markAsRead(mensajeId: Long)

    @Query("DELETE FROM mensajes_usuario WHERE id = :mensajeId")
    suspend fun deleteById(mensajeId: Long)

    @Query("UPDATE mensajes_usuario SET isRead = 1 WHERE user_id = :userId AND isRead = 0")
    suspend fun markAllAsReadForUser(userId: Long)

    @Query("SELECT COUNT(*) FROM mensajes_usuario WHERE user_id = :userId AND isRead = 0 AND deleted = 0")
    fun getUnreadCountForUser(userId: Long): Flow<Int>

    @Query("UPDATE mensajes_usuario SET deleted = 1 WHERE plantilla_id = :plantillaId")
    suspend fun markDeletedByPlantillaId(plantillaId: Long)

    @Query("UPDATE mensajes_usuario SET deleted = 1 WHERE plantilla_id IS NOT NULL")
    suspend fun markDeletedAllFromPlantillas()

    @Query("UPDATE mensajes_usuario SET isRead = 1 WHERE plantilla_id IS NOT NULL")
    suspend fun markAllAsReadFromPlantillas()
}
