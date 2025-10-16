package com.movil.saferescue.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.movil.saferescue.data.local.foto.FotoDao // Cambiado el nombre del paquete
import com.movil.saferescue.data.local.foto.FotoEntity // Cambiado el nombre del paquete
import com.movil.saferescue.data.local.notification.NotificationDao
import com.movil.saferescue.data.local.notification.NotificationEntity
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
    entities = [UserEntity::class, TipoPerfilEntity::class, FotoEntity::class, NotificationEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun fotoDao(): FotoDao
    abstract fun tipoPerfilDao(): TipoPerfilDao

    abstract fun notificationDao(): NotificationDao

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
                                    val NotificationDao = database.notificationDao()
                                    val fotoDao = database.fotoDao()
                                    val tipoPerfilDao = database.tipoPerfilDao() // Obtenemos el nuevo DAO

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
                                        )

                                        )
                                    val seedTipoPerfil= listOf(
                                        TipoPerfilEntity(
                                            rol = "Administrador",
                                            detalle = "Tiene acceso a todo el sistema"
                                        ),
                                        TipoPerfilEntity(
                                            rol = "Bombero",
                                            detalle = "Tiene acceso a la parte publica del sistema"
                                        ),
                                        TipoPerfilEntity(
                                            rol = "Ciudadano",
                                            detalle = "Tiene acceso a la parte publica del sistema, con funciones limitadas"
                                        )

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
                                            run = "12345678",
                                            dv = "k",
                                            username = "Josesito",
                                            rol_id = 2,
                                            foto_id = 2
                                        ),
                                        UserEntity(
                                            name = "Maria Juana",
                                            email = "c@c.cl",
                                            phone = "12345678",
                                            password = "Maria123!",
                                            run = "12345678",
                                            dv = "k",
                                            username = "Mari",
                                            rol_id = 3,
                                            foto_id = 3
                                        )

                                    )
                                    val seedNotification= listOf(
                                        NotificationEntity(
                                            titulo = "Alerta de incendio",
                                            mensaje = "Se notifica de una nueva solicitud de asistencia",
                                            fechaSubida = System.currentTimeMillis() + 1000,
                                            isRead = false
                                        ),
                                        NotificationEntity(
                                            titulo = "Ticket de ayuda",
                                            mensaje = "Se solicita ayuda con el uso de una función de la aplicación",
                                            fechaSubida = System.currentTimeMillis() + 1000,
                                            isRead = false
                                        ),
                                        NotificationEntity(
                                            titulo = "Peligro de incendio",
                                            mensaje = "Se notifica de un incendio en la zona, tenga precaución",
                                            fechaSubida = System.currentTimeMillis() + 1000,
                                            isRead = false
                                        ),

                                    )
                                    // Lógica de inserción corregida
                                    if (userDao.count() == 0) {
                                        seedFoto.forEach { fotoDao.insert(it) }
                                        seedTipoPerfil.forEach { tipoPerfilDao.insertTipoPerfil(it) }
                                        seedUser.forEach { userDao.insertUsuario(it) }
                                        seedNotification.forEach { NotificationDao.insertNotification(it) }
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
