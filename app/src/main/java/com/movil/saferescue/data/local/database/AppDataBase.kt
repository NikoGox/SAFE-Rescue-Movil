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
import com.movil.saferescue.data.local.notification.NotificationDao
import com.movil.saferescue.data.local.notification.NotificationEntity
import com.movil.saferescue.data.local.notification.TipoMensajeDao
import com.movil.saferescue.data.local.notification.TipoMensajeEntity
import com.movil.saferescue.data.local.user.TipoPerfilDao
import com.movil.saferescue.data.local.user.TipoPerfilEntity
import com.movil.saferescue.data.local.user.UserDao
import com.movil.saferescue.data.local.user.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        TipoPerfilEntity::class,
        NotificationEntity::class,
        TipoMensajeEntity::class,
        FotoEntity::class,
        IncidenteEntity::class
    ],
    version = 12,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // 2. DECLARACIÓN DE TODOS LOS DAOs
    abstract fun userDao(): UserDao
    abstract fun tipoPerfilDao(): TipoPerfilDao
    abstract fun notificationDao(): NotificationDao
    abstract fun tipoMensajeDao(): TipoMensajeDao
    abstract fun fotoDao(): FotoDao
    abstract fun incidenteDao(): IncidenteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "saferescue_database"
                )
                    .addCallback(DatabaseCallback(context))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // 5. CALLBACK PARA EJECUTAR CÓDIGO CUANDO LA BD SE CREA
    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(database: AppDatabase) {
            // Se obtienen todos los DAOs desde la instancia de la BD
            val userDao = database.userDao()
            val tipoPerfilDao = database.tipoPerfilDao()
            val fotoDao = database.fotoDao()
            val tipoMensajeDao = database.tipoMensajeDao()
            val notificationDao = database.notificationDao()
            val incidenteDao = database.incidenteDao()

            // ---- Lógica de Inserción (Seeding) ----
            // Se insertan primero las entidades que no tienen dependencias

            val seedFoto = listOf(
                FotoEntity(url = "https://picsum.photos/id/1/200", fechaSubida = System.currentTimeMillis() + 1000),
                FotoEntity(url = "https://picsum.photos/id/2/200", fechaSubida = System.currentTimeMillis() + 1000),
                FotoEntity(url = "https://picsum.photos/id/3/200", fechaSubida = System.currentTimeMillis() + 1000),
                FotoEntity(url = "https://i.blogs.es/7b4a6b/incendio-valparaiso/1366_2000.jpg", fechaSubida = System.currentTimeMillis() + 1000),
                FotoEntity(url = "https://www.cambio21.cl/resizer/p_E9bJv1q_qB2g8r1hX29vX4kM0=/1200x630/filters:format(jpg):quality(70)/cloudfront-us-east-1.images.arcpublishing.com/copesa/JCDCYW6ECRCTLPOWCKKRP3KA5A.jpg", fechaSubida = System.currentTimeMillis() + 1000),
                FotoEntity(url = "https://www.cooperativa.cl/noticias/site/artic/20230202/imag/foto_0000000220230202181635.jpg", fechaSubida = System.currentTimeMillis() + 1000)
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


            // Ahora se insertan las entidades que dependen de las anteriores
            val seedUser = listOf(
                UserEntity(name = "Admin", email = "a@a.cl", phone = "12345678", password = "Admin123!", run = "13333333", dv = "3", username = "Admin", rol_id = 1, foto_id = 1),
                UserEntity(name = "Jose Milan", email = "b@b.cl", phone = "456891011", password = "Jose123!", run = "2356789", dv = "0", username = "Josesito", rol_id = 2, foto_id = 2),
                UserEntity(name = "Maria Juana", email = "c@c.cl", phone = "789101112", password = "Maria123!", run = "11222333", dv = "4", username = "Mari", rol_id = 3, foto_id = 3),
                UserEntity(name = "Agente de Soporte", email = "soporte@safe.cl", phone = "44444444", password = "Soporte123!", run = "9666666", dv = "K", username = "Soporte", rol_id = 4, foto_id = 1)
            )
            userDao.insertAll(seedUser)

            val seedIncidente = listOf(
                IncidenteEntity(titulo = "Incendio en Colegio", detalle = "Se reporta un incendio de rápida propagación en el colegio La Cruz. Se requiere asistencia inmediata de todas las unidades disponibles.", foto_id = 4),
                IncidenteEntity(titulo = "Rescate en Estructura Colapsada", detalle = "Estructura de casa antigua colapsó en el centro de Santiago. Posibles personas atrapadas en el interior. Se solicita equipo USAR.", foto_id = 6),
                IncidenteEntity(titulo = "Accidente Vehicular Múltiple", detalle = "Colisión de 3 vehículos en la Ruta 68, km 90. Se reportan heridos y derrame de combustible. Precaución al transitar.", foto_id = 5)
            )
            incidenteDao.insertAll(seedIncidente)

            // Finalmente, se insertan las notificaciones
            val seedNotification = listOf(
                // 6. CORRECCIÓN de nombres de campos: 'tipoId' y 'remitenteId'
                NotificationEntity(titulo = "Alerta de incendio", mensaje = "Se notifica de una nueva solicitud de asistencia", fechaSubida = System.currentTimeMillis(), isRead = false, tipo_mensaje = 1, remitente_id = 2),
                NotificationEntity(titulo = "Bienvenida", mensaje = "¿En qué podemos ayudarte?", fechaSubida = System.currentTimeMillis() + 1000, isRead = false, tipo_mensaje = 2, remitente_id = 4)
            )
            notificationDao.insertAll(seedNotification)
        }
    }
}
