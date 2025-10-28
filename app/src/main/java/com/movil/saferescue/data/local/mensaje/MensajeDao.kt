package com.movil.saferescue.data.local.mensaje

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Clase de datos para el resultado del JOIN. Contiene el mensaje
 * completo y los datos específicos del remitente.
 */
data class MensajeConRemitente(
    @Embedded
    val mensaje: MensajeEntity, // El mensaje completo

    @ColumnInfo(name = "remitente_nombre")
    val nombreRemitente: String, // El nombre del usuario remitente

    @ColumnInfo(name = "remitente_foto_url")
    val fotoUrlRemitente: String? // La URL de la foto del remitente (opcional)
)

/**
 * Objeto de Acceso a Datos (DAO) para la entidad MensajeEntity.
 * Define los métodos para interactuar con la tabla 'mensajes' en la base de datos.
 */
@Dao
interface MensajeDao {

    /**
     * Inserta un mensaje. OnConflictStrategy.ABORT lanzará una excepción si el ID ya existe.
     * Retorna el nuevo 'rowId' de la fila insertada.
     * @param mensaje La entidad de mensaje a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMensaje(mensaje: MensajeEntity): Long

    /**
     * Obtiene todos los mensajes, ordenados por fecha descendente.
     * @return Un Flow que emite la lista de mensajes.
     */
    @Query("SELECT * FROM mensajes ORDER BY fechaSubida DESC")
    fun getAllMensajes(): Flow<List<MensajeEntity>>

    /**
     * Obtiene un mensaje específico por su ID.
     * @param mensajeId El ID del mensaje a buscar.
     */
    @Query("SELECT * FROM mensajes WHERE id = :mensajeId")
    suspend fun getMensajeById(mensajeId: Long): MensajeEntity?

    /**
     * Actualiza un mensaje existente.
     * @param mensaje La entidad de mensaje con los datos actualizados.
     */
    @Update
    suspend fun updateMensaje(mensaje: MensajeEntity)

    /**
     * Elimina un mensaje por su ID.
     * @param mensajeId El ID del mensaje a eliminar.
     */
    @Query("DELETE FROM mensajes WHERE id = :mensajeId")
    suspend fun deleteMensajeById(mensajeId: Long)

    /**
     * Elimina todos los mensajes de la tabla.
     */
    @Query("DELETE FROM mensajes")
    suspend fun deleteAllMensajes()

    /**
     * Obtiene un conteo en tiempo real de los mensajes no leídos.
     * @return Un Flow que emite el número de mensajes no leídos.
     */
    @Query("SELECT COUNT(*) FROM mensajes WHERE isRead = 0")
    fun getUnreadMensajesCount(): Flow<Int> // Cambiado de Int a Flow<Int> para ser reactivo

    /**
     * Marca todos los mensajes como leídos.
     */
    @Query("UPDATE mensajes SET isRead = 1 WHERE isRead = 0")
    suspend fun markAllAsRead()

    // --- FUNCIONES ESPECIALIZADAS POR TIPO ---

    /**
     * Obtiene un flujo de mensajes filtrados por su tipo.
     * @param tipoId El ID del tipo de mensaje a obtener (ej. 1 para "alerta").
     */
    @Query("SELECT * FROM mensajes WHERE tipo_mensaje_id = :tipoId ORDER BY fechaSubida DESC")
    fun getMensajesByTipoId(tipoId: Long): Flow<List<MensajeEntity>>

    /**
     * Elimina todos los mensajes de un tipo específico.
     * @param tipoId El ID del tipo de mensajes a eliminar.
     */
    @Query("DELETE FROM mensajes WHERE tipo_mensaje_id = :tipoId")
    suspend fun deleteAllByTipoId(tipoId: Long)

    /**
     * Obtiene el conteo de mensajes no leídos de un tipo específico.
     * @param tipoId El ID del tipo de mensaje a contar.
     */
    @Query("SELECT COUNT(*) FROM mensajes WHERE isRead = 0 AND tipo_mensaje_id = :tipoId")
    fun getUnreadCountByTipoId(tipoId: Long): Flow<Int>

    /**
     * Marca todos los mensajes de un tipo específico como leídos.
     * @param tipoId El ID del tipo de mensajes a marcar como leídos.
     */
    @Query("UPDATE mensajes SET isRead = 1 WHERE isRead = 0 AND tipo_mensaje_id = :tipoId")
    suspend fun markAllAsReadByTipoId(tipoId: Long)

    /**
     * Obtiene los mensajes junto con la información del remitente (nombre y foto)
     * usando un JOIN con las tablas de usuarios y fotos.
     * @param tipoId El ID correspondiente al tipo "mensaje de chat".
     * @return Un Flow que emite una lista de objetos MensajeConRemitente.
     */
    @Query("""
        SELECT 
            mensajes.*, 
            users.name as remitente_nombre,
            fotos.url as remitente_foto_url
        FROM mensajes
        JOIN users ON mensajes.remitente_id = users.id
        LEFT JOIN fotos ON users.foto_id = fotos.id
        WHERE mensajes.tipo_mensaje_id = :tipoId
        ORDER BY mensajes.fechaSubida ASC
    """)
    fun getMensajesConRemitente(tipoId: Long): Flow<List<MensajeConRemitente>>

    /**
     * Inserta una lista de mensajes. Si ya existen, los reemplaza.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mensajes: List<MensajeEntity>)
}
