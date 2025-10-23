// ui/components/ImagePicker.kt
package com.movil.saferescue.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Crea un URI de archivo temporal para que la cámara guarde la imagen.
 */
fun createTempUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFile = File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        context.cacheDir // Usamos el directorio cache definido en file_paths.xml
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider", // Debe coincidir con el 'authorities' del Manifest
        imageFile
    )
}

/**
 * Un Composable que muestra un diálogo para elegir entre cámara y galería.
 *
 * @param showDialog Controla la visibilidad del diálogo.
 * @param onDismissRequest Se llama cuando el diálogo se cierra.
 * @param onImageSelected Se llama con la URI de la imagen seleccionada (o null si falla).
 */
@Composable
fun ImagePickerDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onImageSelected: (Uri?) -> Unit
) {
    val context = LocalContext.current
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    // --- LANZADORES DE ACTIVIDADES ---

    // 1. Lanzador para la galería (Photo Picker moderno)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            onDismissRequest()
            onImageSelected(uri)
        }
    )

    // 2. Lanzador para la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success: Boolean ->
            onDismissRequest()
            if (success) {
                // Si la foto se tomó correctamente, usamos la URI temporal que creamos
                onImageSelected(tempImageUri)
            } else {
                onImageSelected(null) // Falló la captura
            }
        }
    )

    // 3. Lanzador para pedir el permiso de la cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                // Permiso concedido, ahora lanzamos la cámara
                tempImageUri = createTempUri(context)
                cameraLauncher.launch(tempImageUri)
            } else {
                // Permiso denegado
                onDismissRequest()
                // Aquí podrías mostrar un Snackbar informando al usuario
            }
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text("Seleccionar Imagen") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            // Lanzar el selector de fotos moderno
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text("Elegir de la Galería")
                    }
                    TextButton(
                        onClick = {
                            // Pedir permiso para la cámara antes de lanzarla
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text("Tomar una Foto")
                    }
                }
            },
            confirmButton = {} // No necesitamos botones de confirmación
        )
    }
}
