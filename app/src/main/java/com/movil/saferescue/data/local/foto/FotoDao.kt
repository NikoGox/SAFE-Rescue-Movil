package com.movil.saferescue.data.local.foto

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FotoDao {
    // Busca una foto por su URL. Devuelve la entidad completa para obtener su ID.
    @Query("SELECT * FROM fotos WHERE url = :url LIMIT 1")
    suspend fun getByUrl(url: String): FotoEntity?

    // Inserta una nueva foto. Si la URL ya existe, no hace nada (gracias a 'unique = true').
    // OnConflictStrategy.IGNORE devuelve -1 si no se inserta, por eso no lo usamos aquí.
    @Insert
    suspend fun insert(foto: FotoEntity): Long

    @Query("SELECT * FROM fotos WHERE id = :fotoId LIMIT 1")
    suspend fun findFotoById(fotoId: Long): FotoEntity?
}
