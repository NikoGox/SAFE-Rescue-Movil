package com.movil.saferescue.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.movil.saferescue.data.local.conversacion.ConversacionDao
import com.movil.saferescue.data.local.conversacion.ConversacionEntity
import com.movil.saferescue.data.local.conversacion.ParticipanteConvDao
import com.movil.saferescue.data.local.conversacion.ParticipanteConvEntity
import com.movil.saferescue.data.local.estado.EstadoDao
import com.movil.saferescue.data.local.estado.EstadoEntity
import com.movil.saferescue.data.local.foto.FotoDao
import com.movil.saferescue.data.local.foto.FotoEntity
import com.movil.saferescue.data.local.incidente.IncidenteDao
import com.movil.saferescue.data.local.incidente.IncidenteEntity
import com.movil.saferescue.data.local.incidente.IncidenteEstado
import com.movil.saferescue.data.local.mensaje.MensajeDao
import com.movil.saferescue.data.local.mensaje.MensajeEntity
import com.movil.saferescue.data.local.notificacion.NotificacionDao
import com.movil.saferescue.data.local.notificacion.NotificacionEntity
import com.movil.saferescue.data.local.user.TipoPerfilDao
import com.movil.saferescue.data.local.user.TipoPerfilEntity
import com.movil.saferescue.data.local.user.UserDao
import com.movil.saferescue.data.local.user.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

@Database(
    entities = [
        UserEntity::class,
        TipoPerfilEntity::class,
        MensajeEntity::class,
        FotoEntity::class,
        IncidenteEntity::class,
        ConversacionEntity::class,
        ParticipanteConvEntity::class,
        NotificacionEntity::class,
        EstadoEntity::class
    ],
    version = 26, // Incrementado a 26 para actualizar seeds
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun tipoPerfilDao(): TipoPerfilDao
    abstract fun mensajeDao(): MensajeDao
    abstract fun fotoDao(): FotoDao
    abstract fun incidenteDao(): IncidenteDao
    abstract fun conversacionDao(): ConversacionDao
    abstract fun participanteConvDao(): ParticipanteConvDao
    abstract fun notificacionDao(): NotificacionDao
    abstract fun estadoDao(): EstadoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "saferescue_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(database: AppDatabase) {
            val userDao = database.userDao()
            val tipoPerfilDao = database.tipoPerfilDao()
            val fotoDao = database.fotoDao()
            val incidenteDao = database.incidenteDao()
            val conversacionDao = database.conversacionDao()
            val participanteConvDao = database.participanteConvDao()
            val mensajeDao = database.mensajeDao()
            val notificacionDao = database.notificacionDao()
            val estadoDao = database.estadoDao()

            // Seed de Estados actualizado según JSON real + Estado 10 (Eliminada)
            val seedEstados = listOf(
                EstadoEntity(id = 1, nombre = "Activo", descripcion = null),
                EstadoEntity(id = 2, nombre = "Baneado", descripcion = null),
                EstadoEntity(id = 3, nombre = "Inactivo", descripcion = null),
                EstadoEntity(id = 4, nombre = "En Proceso", descripcion = null),
                EstadoEntity(id = 5, nombre = "Localizado", descripcion = null),
                EstadoEntity(id = 6, nombre = "Cerrado", descripcion = null),
                EstadoEntity(id = 7, nombre = "Enviado", descripcion = null),
                EstadoEntity(id = 8, nombre = "Recibido", descripcion = "Notificación recibida"),
                EstadoEntity(id = 9, nombre = "Visto", descripcion = "Notificación leída"),
                EstadoEntity(id = 10, nombre = "Eliminado", descripcion = "Notificación eliminada")
            )
            estadoDao.insertAll(seedEstados)

            val seedFoto = listOf(
                FotoEntity(nombre = "picsum_1.jpg", url = "https://picsum.photos/id/1/200"),
                FotoEntity(nombre = "picsum_2.jpg", url = "https://picsum.photos/id/2/200"),
                FotoEntity(nombre = "picsum_3.jpg", url = "https://picsum.photos/id/3/200"),
                FotoEntity(nombre = "incendio_valparaiso.jpg", url = "https://i.blogs.es/7b4a6b/incendio-valparaiso/1366_2000.jpg"),
                FotoEntity(nombre = "accidente_multiple.jpg", url = "https://www.cambio21.cl/resizer/p_E9bJv1q_qB2g8r1hX29vX4kM0=/1200x630/filters:format(jpg):quality(70)/cloudfront-us-east-1.images.arcpublishing.com/copesa/JCDCYW6ECRCTLPOWCKKRP3KA5A.jpg"),
                FotoEntity(nombre = "rescate_estructura.jpg", url = "https://www.cooperativa.cl/noticias/site/artic/20230202/imag/foto_0000000220230202181635.jpg")
            )
            fotoDao.insertAll(seedFoto)

            val seedTipoPerfil = listOf(
                TipoPerfilEntity(rol = "Administrador", detalle = "Acceso total"), // ID 1
                TipoPerfilEntity(rol = "Bombero", detalle = "Acceso emergencia"),   // ID 2
                TipoPerfilEntity(rol = "Ciudadano", detalle = "Acceso limitado"),    // ID 3
                TipoPerfilEntity(rol = "Soporte", detalle = "Agente de chat")      // ID 4
            )
            tipoPerfilDao.insertAll(seedTipoPerfil)

            val seedUser = listOf(
                UserEntity(name = "Admin", email = "a@a.cl", phone = "12345678", password = BCrypt.hashpw("Admin123!", BCrypt.gensalt()), run = "13333333", dv = "3", username = "Admin", rol_id = 1, foto_id = 1),
                UserEntity(name = "Jose Milan", email = "b@b.cl", phone = "456891011", password = BCrypt.hashpw("Admin123!", BCrypt.gensalt()) , run = "20356789", dv = "0", username = "Josesito", rol_id = 2, foto_id = 2),
                UserEntity(name = "Ruben P", email = "k@k.cl", phone = "22345678", password = BCrypt.hashpw("Admin123!", BCrypt.gensalt()), run = "14444444", dv = "4", username = "nikogox", rol_id = 3, foto_id = 1),
                UserEntity(name = "Maria Gonzales", email = "c@c.cl", phone = "789101112", password = BCrypt.hashpw("Admin123!", BCrypt.gensalt()), run = "11222333", dv = "4", username = "Mari", rol_id = 3, foto_id = 3),
                UserEntity(name = "Agente de Soporte", email = "soporte@safe.cl", phone = "44444444", password = BCrypt.hashpw("Admin123!", BCrypt.gensalt()), run = "9666666", dv = "K", username = "Soporte", rol_id = 4, foto_id = 1)
            )
            userDao.insertAll(seedUser)

            val seedIncidente = listOf(
                IncidenteEntity(titulo = "Incendio en Colegio", detalle = "Se reporta un incendio de rápida propagación...", foto_id = 4, estado = IncidenteEstado.ACTIVO.name, asignadoA = null, latitud = -33.04, longitud = -71.61, comuna = "Valparaíso", region = "Valparaíso", direccion = "Av. Argentina 123"),
                IncidenteEntity(titulo = "Rescate en Estructura Colapsada", detalle = "Estructura de casa antigua colapsó...", foto_id = 6, estado = IncidenteEstado.ACTIVO.name, asignadoA = null, latitud = -33.44, longitud = -70.65, comuna = "Santiago", region = "Metropolitana", direccion = "Calle Falsa 123"),
                IncidenteEntity(titulo = "Accidente Vehicular Múltiple", detalle = "Colisión de 3 vehículos en la Ruta 68...", foto_id = 5, estado = IncidenteEstado.ASIGNADO.name, asignadoA = 2L, latitud = -33.25, longitud = -71.21, comuna = "Curacaví", region = "Metropolitana", direccion = "Ruta 68, km 50")
            )
            incidenteDao.insertAll(seedIncidente)

            // Initial Chat Setup (Support Bot)
            val convId = conversacionDao.insertConversacion(
                ConversacionEntity(nombre = "Soporte General", tipo = "SOPORTE", fechaCreacion = System.currentTimeMillis())
            )
            
            participanteConvDao.insertParticipante(ParticipanteConvEntity(conversacionId = convId, usuarioId = 3, fechaInclusion = System.currentTimeMillis()))
            participanteConvDao.insertParticipante(ParticipanteConvEntity(conversacionId = convId, usuarioId = 5, fechaInclusion = System.currentTimeMillis()))

            mensajeDao.insertMensaje(
                MensajeEntity(
                    mensaje = "¿En qué podemos ayudarte?",
                    fechaSubida = System.currentTimeMillis(),
                    conversacionId = convId,
                    remitenteId = 5, // Soporte
                    isRead = false
                )
            )

            // Initial Notification - Usando estado 8 (Recibido/No leído)
            notificacionDao.insertNotificacion(
                NotificacionEntity(
                    titulo = "Bienvenida",
                    detalle = "Bienvenido a SAFE Rescue.",
                    fechaCreacion = System.currentTimeMillis(),
                    usuarioReceptorId = 3,
                    estadoId = 8L // 8 = Recibido (No leído)
                )
            )
        }
    }
}
