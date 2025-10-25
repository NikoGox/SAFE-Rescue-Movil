package com.movil.saferescue.data.repository

import com.movil.saferescue.data.local.mensaje.*
import com.movil.saferescue.data.local.user.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MensajeRepository(
    private val mensajeDao: MensajeDao,
    private val userDao: UserDao,
    private val mensajeUsuarioDao: MensajeUsuarioDao
) {

    private val TIPO_ALERTA_ID = 1L
    private val TIPO_MENSAJE_ID = 2L

    val todasLasAlertas: Flow<List<MensajeEntity>> = mensajeDao.getMensajesByTipoId(TIPO_ALERTA_ID)
    val contadorAlertasNoLeidasGlobal: Flow<Int> = mensajeDao.getUnreadCountByTipoId(TIPO_ALERTA_ID)
    val todosLosMensajesDeChat: Flow<List<MensajeConRemitente>> = mensajeDao.getMensajesConRemitente(TIPO_MENSAJE_ID)

    suspend fun marcarTodasLasAlertasComoLeidas() {
        withContext(Dispatchers.IO) {
            mensajeDao.markAllAsReadByTipoId(TIPO_ALERTA_ID)
            mensajeUsuarioDao.markAllAsReadFromPlantillas()
        }
    }

    suspend fun eliminarTodasLasAlertas() {
        withContext(Dispatchers.IO) {
            mensajeDao.deleteAllByTipoId(TIPO_ALERTA_ID)
            mensajeUsuarioDao.markDeletedAllFromPlantillas()
        }
    }

    suspend fun enviarMensajeDeChat(contenido: String, remitenteId: Long) {
        withContext(Dispatchers.IO) {
            val mensajeUsuario = MensajeEntity(
                titulo = "Mensaje Enviado",
                mensaje = contenido,
                fechaSubida = System.currentTimeMillis(),
                isRead = true,
                tipo_mensaje_id = TIPO_MENSAJE_ID,
                remitente_id = remitenteId
            )
            mensajeDao.insertMensaje(mensajeUsuario)

            kotlinx.coroutines.delay(1500)

            val soporteUserId = userDao.getByUsername("Soporte")?.id ?: 4L

            val respuestaBot = MensajeEntity(
                titulo = "Nuevo Mensaje de Soporte",
                mensaje = "Hemos recibido tu mensaje. Un agente te atenderá pronto.",
                fechaSubida = System.currentTimeMillis(),
                isRead = false,
                tipo_mensaje_id = TIPO_MENSAJE_ID,
                remitente_id = soporteUserId
            )
            mensajeDao.insertMensaje(respuestaBot)
        }
    }

    suspend fun eliminarMensajePorId(mensajeId: Long) {
        withContext(Dispatchers.IO) {
            mensajeDao.deleteMensajeById(mensajeId)
        }
    }

    fun observeNotificationsForUser(userId: Long): Flow<List<MensajeUsuarioEntity>> {
        return mensajeUsuarioDao.getNotificationsForUser(userId)
    }

    fun getUnreadCountForUser(userId: Long): Flow<Int> = mensajeUsuarioDao.getUnreadCountForUser(userId)

    suspend fun markNotificationAsRead(mensajeId: Long) {
        withContext(Dispatchers.IO) {
            mensajeUsuarioDao.markAsRead(mensajeId)
        }
    }

    suspend fun deleteUserNotification(mensajeUsuarioId: Long) {
        withContext(Dispatchers.IO) {
            mensajeUsuarioDao.markDeleted(mensajeUsuarioId)
        }
    }

    suspend fun crearAlertaGlobal(titulo: String, detalle: String, remitenteId: Long) {
        withContext(Dispatchers.IO) {
            val plantilla = MensajeEntity(
                titulo = titulo,
                mensaje = detalle,
                fechaSubida = System.currentTimeMillis(),
                isRead = false,
                tipo_mensaje_id = TIPO_ALERTA_ID,
                remitente_id = remitenteId
            )
            val plantillaId = mensajeDao.insertMensaje(plantilla)

            val usuarios = userDao.getAll()
            val mensajesUsuario = usuarios.map { user ->
                MensajeUsuarioEntity(
                    userId = user.id,
                    plantillaId = plantillaId,
                    titulo = titulo,
                    mensaje = detalle,
                    fechaSubida = System.currentTimeMillis(),
                    isRead = false,
                    deleted = false
                )
            }
            mensajeUsuarioDao.insertAll(mensajesUsuario)
        }
    }
}
