package com.movil.saferescue.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Crea un archivo de imagen temporal en el directorio de caché de la aplicación.
 * Este archivo se usará para que la cámara guarde la foto capturada.
 *
 * @param context El contexto de la aplicación.
 * @return Un objeto [File] que representa el archivo temporal creado.
 */
fun createTempImageFile(context: Context): File {
    // Crea un nombre de archivo único basado en la fecha y hora.
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    // Define el directorio donde se guardarán las imágenes (en la caché).
    val storageDir = File(context.cacheDir, "images").apply {
        if (!exists()) mkdirs() // Crea el directorio si no existe.
    }
    // Crea el archivo en el directorio.
    return File.createTempFile(
        "IMG_${timeStamp}_", /* prefijo */
        ".jpg",              /* sufijo */
        storageDir           /* directorio */
    )
}

/**
 * Obtiene una URI de contenido para un archivo específico, utilizando el FileProvider.
 * Esto es necesario para compartir el archivo de forma segura con otras apps (como la cámara).
 *
 * @param context El contexto de la aplicación.
 * @param file El archivo para el cual se necesita la URI.
 * @return La [Uri] de contenido para el archivo.
 */
fun getUriForFile(context: Context, file: File): Uri {
    // La autoridad debe coincidir con la declarada en el AndroidManifest.
    val authority = "${context.packageName}.fileProvider"
    return FileProvider.getUriForFile(context, authority, file)
}
