package com.movil.saferescue.ui.screen

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.movil.saferescue.ui.components.ImagePickerDialog
import com.movil.saferescue.ui.viewmodel.IncidenteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearIncidenteScreen(
    incidenteViewModel: IncidenteViewModel,
    onIncidentCreated: () -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var detalle by remember { mutableStateOf("") }
    var comuna by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val createState by incidenteViewModel.createState.collectAsStateWithLifecycle()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            incidenteViewModel.requestLastLocation()
        } else {
            Toast.makeText(context, "Se requieren permisos de ubicación", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(createState) {
        if (createState.createSuccess) {
            Toast.makeText(context, "Incidente creado con éxito", Toast.LENGTH_SHORT).show()
            incidenteViewModel.onCreationHandled()
            onIncidentCreated()
        }
        if (createState.error != null) {
            Toast.makeText(context, createState.error, Toast.LENGTH_LONG).show()
            incidenteViewModel.onCreationHandled()
        }
    }

    if (showImageDialog) {
        ImagePickerDialog(
            showDialog = showImageDialog,
            onDismissRequest = { showImageDialog = false },
            onImageSelected = { uri ->
                imageUri = uri
                showImageDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable { showImageDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Imagen del incidente",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = "Añadir foto", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("Añadir Imagen", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Detalles del Incidente", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título del Incidente") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = detalle, onValueChange = { detalle = it }, label = { Text("Descripción detallada") }, modifier = Modifier.fillMaxWidth().height(120.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        val regions = listOf("Arica y Parinacota", "Tarapacá", "Antofagasta", "Atacama", "Coquimbo", "Valparaíso", "Metropolitana", "O'Higgins", "Maule", "Ñuble", "Biobío", "La Araucanía", "Los Ríos", "Los Lagos", "Aysén", "Magallanes")
        var selectedRegion by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }

        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Ubicación", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedRegion,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Región") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Abrir") },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        regions.forEach { region ->
                            DropdownMenuItem(text = { Text(region) }, onClick = { selectedRegion = region; expanded = false })
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = comuna, onValueChange = { comuna = it }, label = { Text("Comuna") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = direccion, onValueChange = { direccion = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { 
                        locationPermissionLauncher.launch(arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Obtener ubicación")
                    Spacer(Modifier.width(8.dp))
                    Text("Obtener Ubicación")
                }

                Spacer(Modifier.height(8.dp))

                if (createState.latitud != null && createState.longitud != null) {
                    Text("Latitud: ${createState.latitud}")
                    Text("Longitud: ${createState.longitud}")
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                incidenteViewModel.crearIncidente(titulo, detalle, imageUri, comuna, selectedRegion, direccion)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !createState.isCreating,
            shape = CircleShape
        ) {
            if (createState.isCreating) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Crear Incidente", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}