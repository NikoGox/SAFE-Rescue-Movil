package com.movil.saferescue.data.local.mensaje

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class MensajeConRemitente(
    @Embedded
    val mensaje: MensajeEntity,

    @ColumnInfo(name = "remitente_nombre")
    val nombreRemitente: String,

    @ColumnInfo(name = "remitente_foto_url")
    val fotoUrlRemitente: String?
)

@Dao
interface MensajeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMensaje(mensaje: MensajeEntity): Long

    @Query("SELECT * FROM mensaje ORDER BY fecha_creacion DESC")
    fun getAllMensajes(): Flow<List<MensajeEntity>>

    @Query("SELECT * FROM mensaje WHERE id_mensaje = :mensajeId")
    suspend fun getMensajeById(mensajeId: Long): MensajeEntity?

    @Update
    suspend fun updateMensaje(mensaje: MensajeEntity)

    @Query("DELETE FROM mensaje WHERE id_mensaje = :mensajeId")
    suspend fun deleteMensajeById(mensajeId: Long)

    @Query("DELETE FROM mensaje")
    suspend fun deleteAllMensajes()

    @Query("""
        SELECT 
            m.*, 
            u.name as remitente_nombre,
            f.url as remitente_foto_url
        FROM mensaje m
        JOIN users u ON m.id_usuario_emisor = u.id
        LEFT JOIN fotos f ON u.foto_id = f.id
        WHERE m.id_conversacion = :conversacionId
        ORDER BY m.fecha_creacion ASC
    """)
    fun getMensajesConRemitentePorConversacion(conversacionId: Long): Flow<List<MensajeConRemitente>>
    
    // Helper to get last message of a conversation if needed
    @Query("SELECT * FROM mensaje WHERE id_conversacion = :conversacionId ORDER BY fecha_creacion DESC LIMIT 1")
    suspend fun getLastMessage(conversacionId: Long): MensajeEntity?
}
