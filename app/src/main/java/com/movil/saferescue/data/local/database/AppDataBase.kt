package com.movil.saferescue.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.movil.saferescue.data.local.foto.FotoDao // Cambiado el nombre del paquete
import com.movil.saferescue.data.local.foto.FotoEntity // Cambiado el nombre del paquete
import com.movil.saferescue.data.local.incidente.IncidenteDao
import com.movil.saferescue.data.local.incidente.IncidenteEntity
import com.movil.saferescue.data.local.notification.NotificationDao
import com.movil.saferescue.data.local.notification.NotificationEntity
import com.movil.saferescue.data.local.notification.TipoMensajeDao
import com.movil.saferescue.data.local.notification.TipoMensajeEntity
import com.movil.saferescue.data.local.user.TipoPerfilDao // <-- 1. IMPORTAR EL NUEVO DAO
import com.movil.saferescue.data.local.user.TipoPerfilEntity
import com.movil.saferescue.data.local.user.UserDao
import com.movil.saferescue.data.local.user.UserEntity
import com.movil.saferescue.navigation.Route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.System
import kotlin.Long
import kotlin.String

@Database(
    entities = [UserEntity::class, TipoPerfilEntity::class, FotoEntity::class, NotificationEntity::class,
        IncidenteEntity::class, TipoMensajeEntity::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun fotoDao(): FotoDao
    abstract fun tipoPerfilDao(): TipoPerfilDao

    abstract fun notificationDao(): NotificationDao
    abstract fun tipoMensajeDao(): TipoMensajeDao
    abstract fun incidenteDao(): IncidenteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val BD_NAME = "safe_rescue.db"

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    BD_NAME
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // La instancia aún no está asignada a INSTANCE aquí, así que la pasamos a la corrutina
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    // 3. USA LA INSTANCIA PASADA, NO VUELVAS A LLAMAR A getInstance()
                                    val userDao = database.userDao()
                                    val notificationDao = database.notificationDao()
                                    val fotoDao = database.fotoDao()
                                    val tipoPerfilDao = database.tipoPerfilDao() // Obtenemos el nuevo DAO
                                    val incidenteDao = database.incidenteDao()
                                    val tipoMensajeDao = database.tipoMensajeDao()

                                    val seedTiposMensaje = listOf(
                                        TipoMensajeEntity(tipo = "alerta", detalle = "Notificación del sistema"), // id 1
                                        TipoMensajeEntity(tipo = "mensaje", detalle = "Mensaje de chat")  // id 2
                                    )

                                    //creamos una semilla para el precargado
                                    val seedFoto= listOf(
                                        FotoEntity(
                                            fechaSubida = System.currentTimeMillis() + 1000,
                                            url="data:image/png;base64,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"
                                        ),
                                        FotoEntity(
                                            fechaSubida = System.currentTimeMillis() + 1000,
                                            url="https://img.freepik.com/foto-gratis/hombre-guapo-feliz-barba_74855-2827.jpg?semt=ais_hybrid&w=740&q=80"
                                        ),
                                        FotoEntity(
                                            fechaSubida = System.currentTimeMillis() + 1000,
                                            url="https://media.istockphoto.com/id/1326417862/es/foto/mujer-joven-riendo-mientras-se-relaja-en-casa.jpg?s=612x612&w=0&k=20&c=BQHE9M8b6hixE_TB1XzuvxobnyD4ylKMTprVbrhPxOU="
                                        ),
                                        FotoEntity(
                                            fechaSubida = System.currentTimeMillis() + 1000,
                                            url="https://media.telemundo51.com/2024/02/TLMD-lluvias-bolivia.jpg?quality=85&strip=all&resize=1200%2C675"
                                        ),
                                        FotoEntity(
                                            fechaSubida = System.currentTimeMillis() + 1000,
                                            url="https://www.cbs.cl/wp-content/uploads/2025/04/3a-ALARMA-DE-INCENDIO-RECOLETA-1.jpg"
                                        ),
                                        FotoEntity(
                                            fechaSubida = System.currentTimeMillis() + 1000,
                                            url="data:image/jpeg;base64,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"
                                        )

                                        )
                                    val seedTipoPerfil = listOf(
                                        TipoPerfilEntity(
                                            rol = "Administrador",
                                            detalle = "Acceso total"), // Tendrá ID 1
                                        TipoPerfilEntity(
                                            rol = "Bombero",
                                            detalle = "Acceso a funciones de emergencia"), // Tendrá ID 2
                                        TipoPerfilEntity(
                                            rol = "Ciudadano",
                                            detalle = "Acceso limitado"), // Tendrá ID 3
                                        TipoPerfilEntity(
                                            rol = "Soporte",
                                            detalle = "Agente de soporte para chat") // Tendrá ID 4
                                    )
                                    val seedUser= listOf(
                                        UserEntity(
                                            name = "Administrador",
                                            email = "a@a.cl",
                                            phone = "12345678",
                                            password = "Admin123!",
                                            run = "12345678",
                                            dv = "k",
                                            username = "Admin",
                                            rol_id = 1,
                                            foto_id = 1

                                        ),
                                        UserEntity(
                                            name = "Jose Milan",
                                            email = "b@b.cl",
                                            phone = "12345678",
                                            password = "Jose123!",
                                            run = "2356789",
                                            dv = "0",
                                            username = "Josesito",
                                            rol_id = 2,
                                            foto_id = 2
                                        ),
                                        UserEntity(
                                            name = "Maria Juana",
                                            email = "c@c.cl",
                                            phone = "12345678",
                                            password = "Maria123!",
                                            run = "11222333",
                                            dv = "4",
                                            username = "Mari",
                                            rol_id = 3,
                                            foto_id = 3
                                        ),
                                        UserEntity(
                                            name = "Agente de Soporte",
                                            email = "soporte@safe.cl",
                                            phone = "444",
                                            password = "Soporte123!",
                                            run = "4-4",
                                            dv = "4",
                                            username = "Soporte",
                                            foto_id = 1,
                                            rol_id = 4) // ID 4
                                    )
                                    val seedNotification= listOf(
                                        NotificationEntity(
                                            titulo = "Alerta de incendio",
                                            mensaje = "Se notifica de una nueva solicitud de asistencia",
                                            fechaSubida = System.currentTimeMillis() + 1000,
                                            isRead = false,
                                            tipo_mensaje = 1,
                                            remitente_id = 1
                                        ),
                                        NotificationEntity(
                                            titulo = "Ticket de ayuda",
                                            mensaje = "Se solicita ayuda con el uso de una función de la aplicación",
                                            fechaSubida = System.currentTimeMillis() + 1000,
                                            isRead = false,
                                            tipo_mensaje = 1,
                                            remitente_id = 2
                                        ),
                                        NotificationEntity(
                                            titulo = "Peligro de incendio",
                                            mensaje = "Se notifica de un incendio en la zona, tenga precaución",
                                            fechaSubida = System.currentTimeMillis() + 1000,
                                            isRead = false,
                                            tipo_mensaje = 2,
                                            remitente_id = 3
                                        ),
                                        NotificationEntity(titulo = "Alerta de incendio", mensaje = "Nueva solicitud de asistencia", fechaSubida = System.currentTimeMillis(), isRead = false, tipo_mensaje =  1, remitente_id = 2),
                                        NotificationEntity(titulo = "Bienvenida", mensaje = "¿En qué podemos ayudarte?", fechaSubida = System.currentTimeMillis() + 1000, isRead = false, tipo_mensaje = 2, remitente_id = 4)


                                    )
                                    val seedIncidente = listOf(
                                    IncidenteEntity(
                                        titulo = "Incendio en Colegio",
                                        detalle = "Se reporta un incendio de rápida propagación en el colegio La Cruz. Se requiere asistencia inmediata de todas las unidades disponibles.",
                                        foto_id = 4
                                    ),
                                    IncidenteEntity(
                                        titulo = "Rescate en Estructura Colapsada",
                                        detalle = "Estructura de casa antigua colapsó en el centro de Santiago. Posibles personas atrapadas en el interior. Se solicita equipo USAR.",
                                        foto_id = 6
                                    ),
                                    IncidenteEntity(
                                        titulo = "Accidente Vehicular Múltiple",
                                        detalle = "Colisión de 3 vehículos en la Ruta 68, km 90. Se reportan heridos y derrame de combustible. Precaución al transitar.",
                                        foto_id = 5
                                    )

                                )

                                    if (userDao.count() == 0) {
                                        seedFoto.forEach { fotoDao.insert(it) }
                                        seedTipoPerfil.forEach { tipoPerfilDao.insertTipoPerfil(it) }
                                        seedUser.forEach { userDao.insertUsuario(it) }
                                        seedNotification.forEach { notificationDao.insertNotification(it) }
                                        seedIncidente.forEach { incidenteDao.insertIncidente(it) } // <-- Añadir inserción
                                    }
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
