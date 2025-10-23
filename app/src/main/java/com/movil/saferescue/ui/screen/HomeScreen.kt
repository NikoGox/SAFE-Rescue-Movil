package com.movil.saferescue.ui.screen

import android.R
import android.content.Context
import android.net.Uri
import android.widget.Button
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.Result.Companion.success


@Composable
fun HomeScreen(
    isAuthenticated: Boolean,
    onGoLogin: () -> Unit,
    onGoRegister: () -> Unit
) {
    val context = LocalContext.current

    //guardar la ultima foto tomada por la camara (String)
    var photoUriString by rememberSaveable { mutableStateOf<String?>(null) }
    //guardar la uri para cuando iniciemos la camara
    var pendingCaptueUri by remember { mutableStateOf<Uri?>(null) }
    //launcher para la camara
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract= ActivityResultContracts.TakePicture()
    ) { success->
        if(success) {
            //si la camara tomo la foto
            photoUriString = pendingCaptueUri.toString()
            Toast.makeText(context, "Foto capturada correctamente", Toast.LENGTH_SHORT).show()
        }else{
            pendingCaptueUri=null
            Toast.makeText(context,"No se tomo ninguna foto", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isAuthenticated) {
            // Contenido para un usuario que ha iniciado sesión
            Text(
                text = "¡Bienvenido!\nEstás en la pantalla de Inicio.",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            // Aquí puedes añadir más contenido como una lista de incidentes, noticias, etc.
        } else {
            // Contenido para un usuario que NO ha iniciado sesión
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Por favor, inicia sesión para ver el contenido.",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onGoLogin) {
                    Text("Ir a Iniciar Sesión")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onGoRegister) {
                    Text("Ir a Registrarse")
                }
            }
        }
    }
}
