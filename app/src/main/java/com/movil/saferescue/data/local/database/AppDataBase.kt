package com.movil.saferescue.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.movil.saferescue.data.local.foto.FotoDao
import com.movil.saferescue.data.local.foto.FotoEntity
import com.movil.saferescue.data.local.incidente.IncidenteDao
import com.movil.saferescue.data.local.incidente.IncidenteEntity
import com.movil.saferescue.data.local.incidente.IncidenteEstado
import com.movil.saferescue.data.local.mensaje.MensajeDao
import com.movil.saferescue.data.local.mensaje.MensajeEntity
import com.movil.saferescue.data.local.mensaje.MensajeUsuarioDao
import com.movil.saferescue.data.local.mensaje.MensajeUsuarioEntity
import com.movil.saferescue.data.local.mensaje.TipoMensajeDao
import com.movil.saferescue.data.local.mensaje.TipoMensajeEntity
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
        MensajeUsuarioEntity::class,
        TipoMensajeEntity::class,
        FotoEntity::class,
        IncidenteEntity::class
    ],
    version = 23, // Incrementar versión para aplicar cambios
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun tipoPerfilDao(): TipoPerfilDao
    abstract fun mensajeDao(): MensajeDao
    abstract fun mensajeUsuarioDao(): MensajeUsuarioDao
    abstract fun tipoMensajeDao(): TipoMensajeDao
    abstract fun fotoDao(): FotoDao
    abstract fun incidenteDao(): IncidenteDao

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
            val tipoMensajeDao = database.tipoMensajeDao()
            val mensajeDao = database.mensajeDao()
            val incidenteDao = database.incidenteDao()

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

            val seedTiposMensaje = listOf(
                TipoMensajeEntity(tipo = "alerta", detalle = "Notificación de sistema"), // ID 1
                TipoMensajeEntity(tipo = "mensaje", detalle = "Mensaje de chat")       // ID 2
            )
            tipoMensajeDao.insertAll(seedTiposMensaje)

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
                IncidenteEntity(titulo = "Accidente Vehicular Múltiple", detalle = "Colisión de 3 vehículos en la Ruta 68...", foto_id = 5, estado = IncidenteEstado.ASIGNADO.name, asignadoA = 2L, latitud = -33.25, longitud = -71.21, comuna = "Curacaví", region = "Metropolitana", direccion = "Ruta 68, km 50") // Asignado a Jose Milan (ID 2)
            )
            incidenteDao.insertAll(seedIncidente)

            val seedMensajes = listOf(
                MensajeEntity(titulo = "Alerta de incendio", mensaje = "Se notifica de una nueva solicitud de asistencia", fechaSubida = System.currentTimeMillis(), isRead = false, tipo_mensaje_id = 1, remitente_id = 2),
                MensajeEntity(titulo = "Bienvenida", mensaje = "¿En qué podemos ayudarte?", fechaSubida = System.currentTimeMillis() + 1000, isRead = false, tipo_mensaje_id = 2, remitente_id = 4)
            )
            mensajeDao.insertAll(seedMensajes)
        }
    }
}
