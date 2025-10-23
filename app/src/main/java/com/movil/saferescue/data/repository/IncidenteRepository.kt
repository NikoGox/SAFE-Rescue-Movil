// Archivo: app/src/main/java/com/movil/saferescue/data/repository/IncidenteRepository.kt
package com.movil.saferescue.data.repository

// Se elimina la importación incorrecta de 'copy'

import com.movil.saferescue.data.local.foto.FotoDao
import com.movil.saferescue.data.local.foto.FotoEntity // Importación necesaria para la nueva función
import com.movil.saferescue.data.local.incidente.IncidenteDao
import com.movil.saferescue.data.local.incidente.IncidenteEntity
import com.movil.saferescue.data.local.incidente.IncidenteEstado // <<< CORRECCIÓN: Importar el enum
import com.movil.saferescue.data.local.incidente.IncidentWithDetails // <<< CORRECCIÓN: Importar desde el paquete correcto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Repositorio para gestionar las operaciones de datos de los incidentes.
 *
 * @param incidenteDao El DAO para acceder a los datos de los incidentes.
 * @param fotoDao El DAO para acceder a los datos de las fotos.
 */
class IncidenteRepository(
    private val incidenteDao: IncidenteDao,
    private val fotoDao: FotoDao
) {

    /**
     * Obtiene un flujo con todos los incidentes y los detalles de su foto asociada y usuario asignado.
     */
    fun getAllIncidentsWithDetails(): Flow<List<IncidentWithDetails>> {
        return incidenteDao.getAllIncidentsWithDetails()
    }

    /**
     * Obtiene un flujo para un solo incidente basado en su ID.
     */
    fun getIncidenteById(id: Long): Flow<IncidenteEntity?> {
        return incidenteDao.getIncidenteById(id)
    }

    /**
     * Inserta un nuevo incidente en la base de datos.
     */
    suspend fun insertIncidente(incidente: IncidenteEntity) {
        withContext(Dispatchers.IO) {
            incidenteDao.insertar(incidente)
        }
    }

    /**
     * Actualiza un incidente existente en la base de datos.
     */
    suspend fun updateIncidente(incidente: IncidenteEntity) {
        withContext(Dispatchers.IO) {
            incidenteDao.updateIncidente(incidente)
        }
    }

    /**
     * Elimina un incidente de la base de datos.
     */
    suspend fun deleteIncidente(incidente: IncidenteEntity) {
        withContext(Dispatchers.IO) {
            incidenteDao.deleteIncidente(incidente)
        }
    }

    /**
     * Marca un incidente como RESUELTO.
     */
    suspend fun closeIncident(incidenteId: Long) {
        // 1. Ejecutar toda la operación en el hilo de IO.
        withContext(Dispatchers.IO) {
            // 2. Obtener el valor actual y único del Flow usando .first().
            val incidenteActual = incidenteDao.getIncidenteById(incidenteId).first()

            // 3. Verificar si el incidente realmente existe.
            if (incidenteActual != null) {
                // 4. Crear una copia del objeto, modificando solo el estado.
                val incidenteCerrado = incidenteActual.copy(
                    estado = IncidenteEstado.RESUELTO.name,
                    // Opcional: podrías limpiar el 'asignadoA' si tiene sentido en tu lógica de negocio.
                    // asignadoA = null
                )
                // 5. Llamar al método 'updateIncidente' que sí existe en el DAO.
                incidenteDao.updateIncidente(incidenteCerrado)
            } else {
                // El manejo de excepciones es una buena práctica.
                throw Exception("No se pudo encontrar el incidente con ID: $incidenteId para cerrarlo.")
            }
        }
    }

    /**
     * Asigna un incidente a un usuario específico, cambiando su estado a 'ASIGNADO'.
     */
    suspend fun takeIncident(incidentId: Long, userId: Long) {
        withContext(Dispatchers.IO) {
            val incidente = incidenteDao.getIncidenteById(incidentId).first()
            incidente?.let {
                val updatedIncident = it.copy(
                    asignadoA = userId,
                    estado = IncidenteEstado.ASIGNADO.name // Ahora el compilador conoce 'IncidenteEstado'
                )
                incidenteDao.updateIncidente(updatedIncident)
            }
        }
    }

    /**
     * Actualiza el ID de la foto de un incidente existente.
     */
    suspend fun updateIncidentPhoto(incidentId: Long, newPhotoId: Long) {
        withContext(Dispatchers.IO) {
            val incidente = incidenteDao.getIncidenteById(incidentId).first()
            incidente?.let {
                val updatedIncident = it.copy(foto_id = newPhotoId)
                incidenteDao.updateIncidente(updatedIncident)
            }
        }
    }

    /**
     * Inserta una nueva foto y devuelve su ID autogenerado.
     */
    suspend fun insertFotoAndGetId(foto: FotoEntity): Long {
        return withContext(Dispatchers.IO) {
            fotoDao.insertFoto(foto)
        }
    }
}
