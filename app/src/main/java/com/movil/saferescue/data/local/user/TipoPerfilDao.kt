package com.movil.saferescue.data.local.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface TipoPerfilDao {
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTipoPerfil(tipoPerfil: TipoPerfilEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tipoPerfiles: List<TipoPerfilEntity>)

}
