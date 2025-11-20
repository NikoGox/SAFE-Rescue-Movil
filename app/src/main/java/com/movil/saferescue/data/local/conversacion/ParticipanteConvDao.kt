package com.movil.saferescue.data.local.conversacion

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ParticipanteConvDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipante(participante: ParticipanteConvEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(participantes: List<ParticipanteConvEntity>)

    @Query("SELECT * FROM participante_conv WHERE id_conversacion = :conversacionId")
    suspend fun getParticipantesByConversacion(conversacionId: Long): List<ParticipanteConvEntity>
    
    @Query("SELECT id_conversacion FROM participante_conv WHERE id_usuario = :userId")
    suspend fun getConversationIdsForUser(userId: Long): List<Long>

    @Query("SELECT * FROM participante_conv WHERE id_conversacion = :conversacionId AND id_usuario != :userId LIMIT 1")
    suspend fun getOtherParticipant(conversacionId: Long, userId: Long): ParticipanteConvEntity?
}
