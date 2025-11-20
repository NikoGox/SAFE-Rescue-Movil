package com.movil.saferescue.data.local.conversacion

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ConversacionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversacion(conversacion: ConversacionEntity): Long

    @Query("SELECT * FROM conversacion WHERE id_conversacion = :id")
    suspend fun getConversacionById(id: Long): ConversacionEntity?

    @Query("""
        SELECT c.* FROM conversacion c
        INNER JOIN participante_conv p ON c.id_conversacion = p.id_conversacion
        WHERE p.id_usuario = :userId
    """)
    suspend fun getConversacionesForUser(userId: Long): List<ConversacionEntity>
}
