package com.movil.saferescue.data.repository

import com.movil.saferescue.data.local.conversacion.ConversacionDao
import com.movil.saferescue.data.local.conversacion.ConversacionEntity
import com.movil.saferescue.data.local.conversacion.ParticipanteConvDao
import com.movil.saferescue.data.local.conversacion.ParticipanteConvEntity
import com.movil.saferescue.data.local.mensaje.MensajeConRemitente
import com.movil.saferescue.data.local.mensaje.MensajeDao
import com.movil.saferescue.data.local.mensaje.MensajeEntity
import com.movil.saferescue.data.local.notificacion.NotificacionDao
import com.movil.saferescue.data.local.notificacion.NotificacionEntity
import com.movil.saferescue.data.local.user.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

// Modelo de datos para la lista de conversaciones en UI
data class ConversacionItem(
    val id: Long,
    val nombre: String,
    val ultimoMensaje: String,
    val horaUltimoMensaje: Long,
    val cantidadNoLeidos: Int = 0 // Por implementar a futuro
)

class MensajeRepository(
    private val mensajeDao: MensajeDao,
    private val userDao: UserDao,
    private val notificacionDao: NotificacionDao,
    private val conversacionDao: ConversacionDao,
    private val participanteConvDao: ParticipanteConvDao
) {

    // --- Notificaciones (Replaces Alerts) ---

    fun getNotificacionesForUser(userId: Long): Flow<List<NotificacionEntity>> {
        return notificacionDao.getNotificacionesForUser(userId)
    }

    fun getUnreadNotificacionesCount(userId: Long): Flow<Int> {
        return notificacionDao.getUnreadCountForUser(userId)
    }

    suspend fun markNotificationAsRead(notificacionId: Long) {
        withContext(Dispatchers.IO) {
            notificacionDao.markAsRead(notificacionId)
        }
    }

    suspend fun deleteNotification(notificacionId: Long) {
        withContext(Dispatchers.IO) {
            notificacionDao.markDeleted(notificacionId)
        }
    }

    suspend fun crearAlertaGlobal(titulo: String, detalle: String, remitenteId: Long) {
        withContext(Dispatchers.IO) {
            val usuarios = userDao.getAll()
            val timestamp = System.currentTimeMillis()
            val notificaciones = usuarios.map { user ->
                NotificacionEntity(
                    fechaCreacion = timestamp,
                    titulo = titulo,
                    detalle = detalle,
                    usuarioReceptorId = user.id,
                    estadoId = 8L
                )
            }
            notificacionDao.insertAll(notificaciones)
        }
    }

    // --- Chat Logic ---

    private suspend fun getOrCreateSupportConversation(userId: Long): Long {
        val soporteUser = userDao.getByUsername("Soporte")
        val soporteId = soporteUser?.id ?: 5L 

        val conversationIds = participanteConvDao.getConversationIdsForUser(userId)
        if (conversationIds.isNotEmpty()) {
            return conversationIds.first()
        }

        val convId = conversacionDao.insertConversacion(
            ConversacionEntity(
                nombre = "Soporte Técnico",
                tipo = "SOPORTE",
                fechaCreacion = System.currentTimeMillis()
            )
        )
        participanteConvDao.insertParticipante(
            ParticipanteConvEntity(conversacionId = convId, usuarioId = userId, fechaInclusion = System.currentTimeMillis())
        )
        participanteConvDao.insertParticipante(
            ParticipanteConvEntity(conversacionId = convId, usuarioId = soporteId, fechaInclusion = System.currentTimeMillis())
        )
        return convId
    }
    
    /**
     * Obtiene la lista de conversaciones para un usuario, formateada para la UI.
     * Incluye el último mensaje y el nombre del otro participante.
     */
    fun getConversacionesForUser(userId: Long): Flow<List<ConversacionItem>> = flow {
        // Aseguramos que exista el chat de soporte al menos
        getOrCreateSupportConversation(userId)
        
        val conversacionesEnt = conversacionDao.getConversacionesForUser(userId)
        val items = conversacionesEnt.map { conv ->
            val lastMsg = mensajeDao.getLastMessage(conv.id)
            
            // Determinar nombre a mostrar: si es soporte, es fijo, si no buscamos al otro
            val nombreMostrar = if (conv.tipo == "SOPORTE") {
                "Soporte Técnico"
            } else {
                val otherPart = participanteConvDao.getOtherParticipant(conv.id, userId)
                val user = otherPart?.let { userDao.getUserById(it.usuarioId) }
                user?.name ?: conv.nombre // Fallback al nombre de la conversación
            }

            val mensajePreview = lastMsg?.mensaje?.take(30)?.let { if (it.length == 30) "$it..." else it } ?: "Sin mensajes"
            
            ConversacionItem(
                id = conv.id,
                nombre = nombreMostrar,
                ultimoMensaje = mensajePreview,
                horaUltimoMensaje = lastMsg?.fechaSubida ?: conv.fechaCreacion
            )
        }
        emit(items)
    }

    fun getChatMessages(conversacionId: Long): Flow<List<MensajeConRemitente>> {
        return mensajeDao.getMensajesConRemitentePorConversacion(conversacionId)
    }

    // Mantener compatibilidad por ahora, redirige a soporte
    fun getChatMessagesForUser(userId: Long): Flow<List<MensajeConRemitente>> = flow {
        val convId = getOrCreateSupportConversation(userId)
        mensajeDao.getMensajesConRemitentePorConversacion(convId).collect {
            emit(it)
        }
    }

    suspend fun enviarMensajeDeChat(contenido: String, remitenteId: Long, conversacionId: Long? = null) {
        withContext(Dispatchers.IO) {
            // Si no se da ID, asumimos soporte (legacy support)
            val targetConvId = conversacionId ?: getOrCreateSupportConversation(remitenteId)
            
            val mensaje = MensajeEntity(
                mensaje = contenido,
                fechaSubida = System.currentTimeMillis(),
                isRead = false,
                conversacionId = targetConvId,
                remitenteId = remitenteId
            )
            mensajeDao.insertMensaje(mensaje)

            // Bot response logic only for Support chat
            // Check if this conversation is Support type
            val conv = conversacionDao.getConversacionById(targetConvId)
            if (conv?.tipo == "SOPORTE") {
                kotlinx.coroutines.delay(1500)
                val soporteUser = userDao.getByUsername("Soporte")
                val soporteId = soporteUser?.id ?: 4L

                if (remitenteId != soporteId) {
                    val respuestaBot = MensajeEntity(
                        mensaje = "Hemos recibido tu mensaje. Un agente te atenderá pronto.",
                        fechaSubida = System.currentTimeMillis(),
                        isRead = false,
                        conversacionId = targetConvId,
                        remitenteId = soporteId
                    )
                    mensajeDao.insertMensaje(respuestaBot)
                }
            }
        }
    }

    suspend fun eliminarMensajePorId(mensajeId: Long) {
        withContext(Dispatchers.IO) {
            mensajeDao.deleteMensajeById(mensajeId)
        }
    }
}
