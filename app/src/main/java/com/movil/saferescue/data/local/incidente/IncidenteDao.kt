package com.movil.saferescue.data.local.incidente

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para la entidad Incidente.
 * Define los métodos de acceso a la base de datos para la tabla 'incidente'.
 */
@Dao
interface IncidenteDao {

    /**
     * Inserta un nuevo incidente en la base de datos.
     * Si el incidente ya existe, la operación se cancela gracias a OnConflictStrategy.ABORT.
     * Es una función 'suspend' porque debe ser llamada desde una corrutina.
     *
     * @param incidente El objeto IncidenteEntity a insertar.
     * @return El ID del nuevo incidente insertado.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertIncidente(incidente: IncidenteEntity): Long

    /**
     * Actualiza un incidente existente en la base de datos.
     * Room utiliza la clave primaria (id) del objeto para encontrar la fila a actualizar.
     *
     * @param incidente El objeto IncidenteEntity con los datos actualizados.
     */
    @Update
    suspend fun updateIncidente(incidente: IncidenteEntity)

    /**
     * Elimina un incidente de la base de datos.
     * Room utiliza la clave primaria (id) para encontrar y eliminar la fila.
     *
     * @param incidente El objeto IncidenteEntity a eliminar.
     */
    @Delete
    suspend fun deleteIncidente(incidente: IncidenteEntity)

    /**
     * Obtiene un incidente específico por su ID.
     * Devuelve un Flow, lo que permite a la UI observar cambios en este incidente en tiempo real.
     *
     * @param id El ID del incidente a buscar.
     * @return Un Flow que emite el IncidenteEntity o null si no se encuentra.
     */
    @Query("SELECT * FROM incidente WHERE id = :id")
    fun getIncidenteById(id: Long): Flow<IncidenteEntity?>

    /**
     * Obtiene todos los incidentes de la base de datos, ordenados por fecha de registro descendente.
     * Devuelve un Flow<List<IncidenteEntity>>, lo que permite a la UI actualizarse
     * automáticamente cuando se añadan, modifiquen o eliminen incidentes.
     *
     * @return Un Flow que emite la lista completa de todos los incidentes.
     */
    @Query("SELECT * FROM incidente ORDER BY fechaRegistro DESC")
    fun getAllIncidentes(): Flow<List<IncidenteEntity>>

}
