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

    /**
     * Inserta una nueva foto. Si ya existe una con la misma URL, la reemplaza.
     * Devuelve el ID de la fila recién insertada.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoto(foto: FotoEntity): Long

    // Busca una foto por su ID.
    @Query("SELECT * FROM fotos WHERE id = :fotoId LIMIT 1")
    suspend fun findFotoById(fotoId: Long): FotoEntity?

    // Inserta una lista de fotos, reemplazando si hay conflictos.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fotos: List<FotoEntity>)

    /**
     * NUEVA FUNCIÓN: Elimina una foto por su ID.
     * Útil si el usuario elimina un incidente o si se quiere limpiar fotos antiguas.
     */
    @Query("DELETE FROM fotos WHERE id = :fotoId")
    suspend fun deleteFotoById(fotoId: Long)
}
