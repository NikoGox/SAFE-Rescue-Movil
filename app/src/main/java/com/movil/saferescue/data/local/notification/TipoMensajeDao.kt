package com.movil.saferescue.data.local.notification

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface TipoMensajeDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTipoMensaje(tipoMensaje:TipoMensajeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tipoMensajes: List<TipoMensajeEntity>)

}
