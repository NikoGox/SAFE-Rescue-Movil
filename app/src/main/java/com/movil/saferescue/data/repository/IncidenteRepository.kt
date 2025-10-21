package com.movil.saferescue.data.repository

import com.movil.saferescue.data.local.foto.FotoDao
import com.movil.saferescue.data.local.incidente.IncidenteDao
import com.movil.saferescue.data.local.incidente.IncidenteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repositorio para gestionar las operaciones de datos de los incidentes.
 * Actúa como una capa de abstracción entre los ViewModels y la fuente de datos (IncidenteDao).
 *
 * @param incidenteDao El DAO para acceder a los datos de los incidentes en la base de datos local.
 * @param fotoDao El DAO para acceder a los datos de las fotos.
 */
class IncidenteRepository(
    private val incidenteDao: IncidenteDao,
    private val fotoDao: FotoDao // <-- 1. AÑADIR fotoDao AL CONSTRUCTOR
) {

    /**
     * Obtiene un flujo con todos los incidentes de la base de datos, ordenados por fecha.
     * El Flow se actualizará automáticamente cuando los datos cambien.
     *
     * @return Un Flow que emite una lista de todos los IncidenteEntity.
     */
    fun getAllIncidentes(): Flow<List<IncidenteEntity>> {
        return incidenteDao.getAllIncidentes()
    }

    /**
     * Obtiene la URL de una foto de forma síncrona a partir de su ID.
     * Utiliza el método `findFotoById` del FotoDao y extrae la URL.
     *
     * @param fotoId El ID de la foto a buscar.
     * @return La URL de la foto como String, o null si el ID es nulo o la foto no se encuentra.
     */
    suspend fun getFotoUrlById(fotoId: Long?): String? {
        if (fotoId == null) return null
        return withContext(Dispatchers.IO) {
            // Llama a la función que SÍ existe en tu DAO...
            val fotoEntity = fotoDao.findFotoById(fotoId)
            // ...y devuelve la propiedad 'url' del resultado.
            fotoEntity?.url
        }
    }


    /**
     * Obtiene un flujo para un solo incidente basado en su ID.
     * Ideal para observar los detalles de un incidente específico.
     *
     * @param id El ID del incidente a obtener.
     * @return Un Flow que emite el IncidenteEntity correspondiente o null si no se encuentra.
     */
    fun getIncidenteById(id: Long): Flow<IncidenteEntity?> {
        return incidenteDao.getIncidenteById(id)
    }

    /**
     * Inserta un nuevo incidente en la base de datos.
     * Esta operación se ejecuta en el hilo de I/O para no bloquear la UI.
     *
     * @param incidente El IncidenteEntity a insertar.
     * @return Result<Long> que contiene el ID del nuevo incidente si tiene éxito, o una excepción si falla.
     */
    suspend fun insertIncidente(incidente: IncidenteEntity): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val newId = incidenteDao.insertIncidente(incidente)
                Result.success(newId)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Actualiza un incidente existente en la base de datos.
     * Esta operación se ejecuta en el hilo de I/O.
     *
     * @param incidente El IncidenteEntity con los datos actualizados.
     * @return Result<Unit> que indica éxito o fallo.
     */
    suspend fun updateIncidente(incidente: IncidenteEntity): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                incidenteDao.updateIncidente(incidente)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Elimina un incidente de la base de datos.
     * Esta operación se ejecuta en el hilo de I/O.
     *
     * @param incidente El IncidenteEntity a eliminar.
     * @return Result<Unit> que indica éxito o fallo.
     */
    suspend fun deleteIncidente(incidente: IncidenteEntity): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                incidenteDao.deleteIncidente(incidente)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
