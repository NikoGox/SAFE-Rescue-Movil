// Archivo: app/src/main/java/com/movil/saferescue/data/local/incidente/IncidenteDao.kt
package com.movil.saferescue.data.local.incidente

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class IncidentWithDetails(
    @Embedded val incident: IncidenteEntity,
    @ColumnInfo(name = "photo_url") val photoUrl: String?,
    @ColumnInfo(name = "asignado_a_nombre") val asignadoANombre: String? // Nombre del usuario
)


/**
 * Data Access Object (DAO) para la entidad Incidente.
 * Define los métodos de acceso a la base de datos para la tabla 'incidente'.
 */
@Dao
interface IncidenteDao {

    /**
     * Inserta un nuevo incidente en la base de datos.
     * Si hay un conflicto, se reemplaza la entrada existente.
     *
     * @param incidente El objeto IncidenteEntity a insertar.
     * @return El ID del nuevo incidente insertado o actualizado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(incidente: IncidenteEntity): Long

    /**
     * Actualiza un incidente existente en la base de datos.
     */
    @Update
    suspend fun updateIncidente(incidente: IncidenteEntity)

    /**
     * Elimina un incidente de la base de datos.
     */
    @Delete
    suspend fun deleteIncidente(incidente: IncidenteEntity)

    /**
     * Obtiene un incidente específico por su ID.
     */
    @Query("SELECT * FROM incidente WHERE id = :id")
    fun getIncidenteById(id: Long): Flow<IncidenteEntity?>

    /**
     * Inserta una lista de incidentes. Útil para la inicialización de la base de datos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(incidentes: List<IncidenteEntity>)

    /**
     * Obtiene una lista de todos los incidentes con detalles adicionales (foto y nombre del asignado).
     * Esta es la consulta principal para mostrar la lista de incidentes en la UI.
     */
    @Transaction
    @Query("""
        SELECT 
            i.*, 
            f.url AS photo_url,
            u.name AS asignado_a_nombre
        FROM incidente AS i
        LEFT JOIN fotos AS f ON i.foto_id = f.id
        LEFT JOIN users AS u ON i.asignado_a_user_id = u.id
        ORDER BY i.fechaRegistro DESC
    """)
    fun getAllIncidentsWithDetails(): Flow<List<IncidentWithDetails>>
}
