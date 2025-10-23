package com.movil.saferescue.data.repository

// 1. IMPORTACIONES ACTUALIZADAS
import com.movil.saferescue.data.local.mensaje.MensajeDao
import com.movil.saferescue.data.local.mensaje.MensajeEntity
import com.movil.saferescue.data.local.mensaje.MensajeConRemitente
import com.movil.saferescue.data.local.user.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repositorio para gestionar las operaciones de datos de los mensajes.
 * Esta clase orquesta las interacciones con MensajeDao y UserDao para
 * encapsular la lógica de negocio.
 */
// 2. RENOMBRADO DE LA CLASE Y SUS PARÁMETROS
class MensajeRepository(
    private val mensajeDao: MensajeDao,
    private val userDao: UserDao
) {

    // IDs para los tipos de mensaje (es una buena práctica definirlos como constantes)
    private val TIPO_ALERTA_ID = 1L
    private val TIPO_MENSAJE_ID = 2L

    // --- LÓGICA PARA LA PANTALLA DE ALERTAS ---

    /**
     * Obtiene un flujo solo con los mensajes de tipo 'alerta'.
     */
    // 3. RENOMBRADO DE VARIABLES Y USO DE NUEVAS FUNCIONES DEL DAO
    val todasLasAlertas: Flow<List<MensajeEntity>> = mensajeDao.getMensajesByTipoId(TIPO_ALERTA_ID)

    /**
     * Obtiene un flujo con el conteo de alertas no leídas.
     */
    val contadorAlertasNoLeidas: Flow<Int> = mensajeDao.getUnreadCountByTipoId(TIPO_ALERTA_ID)

    /**
     * Marca todas las alertas como leídas.
     */
    suspend fun marcarTodasLasAlertasComoLeidas() {
        withContext(Dispatchers.IO) {
            mensajeDao.markAllAsReadByTipoId(TIPO_ALERTA_ID)
        }
    }

    /**
     * Elimina todas las alertas del sistema.
     */
    suspend fun eliminarTodasLasAlertas() {
        withContext(Dispatchers.IO) {
            mensajeDao.deleteAllByTipoId(TIPO_ALERTA_ID)
        }
    }

    // --- LÓGICA PARA LA PANTALLA DE CHAT ---

    /**
     * Obtiene un flujo de mensajes de chat con la información del remitente ya incluida.
     */
    val todosLosMensajesDeChat: Flow<List<MensajeConRemitente>> = mensajeDao.getMensajesConRemitente(TIPO_MENSAJE_ID)

    /**
     * Guarda un mensaje enviado por el usuario y simula una respuesta del soporte.
     * @param contenido El texto del mensaje.
     * @param remitenteId El ID del usuario que envía el mensaje.
     */
    suspend fun enviarMensajeDeChat(contenido: String, remitenteId: Long) {
        withContext(Dispatchers.IO) {
            // 4. LÓGICA DE CREACIÓN ACTUALIZADA A MensajeEntity
            // 1. Guarda el mensaje del usuario
            val mensajeUsuario = MensajeEntity(
                titulo = "Mensaje Enviado",
                mensaje = contenido,
                fechaSubida = System.currentTimeMillis(),
                isRead = true, // El mensaje propio se marca como leído
                tipo_mensaje_id = TIPO_MENSAJE_ID,
                remitente_id = remitenteId
            )
            mensajeDao.insertMensaje(mensajeUsuario)

            // Simulación de retraso antes de la respuesta del bot
            kotlinx.coroutines.delay(1500)

            // Busca el usuario de soporte por su nombre de usuario
            val soporteUserId = userDao.getByUsername("Soporte")?.id ?: 4L // Fallback al ID 4 si no se encuentra

            val respuestaBot = MensajeEntity(
                titulo = "Nuevo Mensaje de Soporte",
                mensaje = "Hemos recibido tu mensaje. Un agente te atenderá pronto.",
                fechaSubida = System.currentTimeMillis(),
                isRead = false, // La respuesta del bot no está leída
                tipo_mensaje_id = TIPO_MENSAJE_ID,
                remitente_id = soporteUserId
            )
            mensajeDao.insertMensaje(respuestaBot)
        }
    }
    /**
     * Elimina un mensaje específico por su ID. Útil para ambas pantallas.
     */
    suspend fun eliminarMensajePorId(mensajeId: Long) {
        withContext(Dispatchers.IO) {
            mensajeDao.deleteMensajeById(mensajeId)
        }
    }
}
