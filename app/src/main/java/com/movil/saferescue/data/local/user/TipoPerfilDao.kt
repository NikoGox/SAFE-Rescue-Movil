package com.movil.saferescue.data.local.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy


@Dao
interface TipoPerfilDao {
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTipoPerfil(tipoPerfil: TipoPerfilEntity)
}
