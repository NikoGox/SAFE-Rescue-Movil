package com.movil.saferescue.ui.screen

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.movil.saferescue.R
import com.movil.saferescue.data.local.incidente.IncidenteEstado
import com.movil.saferescue.data.local.incidente.IncidentWithDetails
import com.movil.saferescue.ui.components.ImagePickerDialog
import com.movil.saferescue.ui.theme.SRBackgroundWhite
import com.movil.saferescue.ui.viewmodel.IncidenteViewModel

@Composable
fun IncidenteScreen(
    viewModel: IncidenteViewModel,
    isAdmin: Boolean,
    isBombero: Boolean
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val editState by viewModel.editState.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()

    if (editState.selectedIncident != null) {
        EditIncidentDialog(
            viewModel = viewModel,
            onDismiss = viewModel::onDismissDialog,
            isAdmin = isAdmin
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (state.incidentsWithDetails.isEmpty()) {
            Text("No hay incidentes reportados.", modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.incidentsWithDetails) { incidentDetails ->
                    IncidentCard(
                        incidentDetails = incidentDetails,
                        currentUserId = currentUserId,
                        formatDate = viewModel::formatDate,
                        onTakeIncident = { viewModel.onTakeIncidentClicked(incidentDetails.incident.id) },
                        onCloseIncident = { viewModel.onCloseIncidentClicked(incidentDetails.incident.id) },
                        onEditIncident = { viewModel.onEditIncidentClicked(incidentDetails) },
                        isAdmin = isAdmin,
                        isBombero = isBombero
                    )
                }
            }
        }
    }
}

@Composable
fun IncidentCard(
    incidentDetails: IncidentWithDetails,
    currentUserId: Long?,
    formatDate: (Long) -> String,
    onTakeIncident: () -> Unit,
    onCloseIncident: () -> Unit,
    onEditIncident: () -> Unit,
    isAdmin: Boolean,
    isBombero: Boolean
) {
    val incident = incidentDetails.incident
    val canBeTaken = incident.estado == IncidenteEstado.ACTIVO.name && isBombero
    val canBeClosed = incident.estado == IncidenteEstado.ASIGNADO.name && incident.asignadoA == currentUserId
    val isResolved = incident.estado == IncidenteEstado.RESUELTO.name
    val isAssignedToOther = incident.estado == IncidenteEstado.ASIGNADO.name && incident.asignadoA != currentUserId

    val fullAddress = listOfNotNull(
        incidentDetails.incident.direccion,
        incidentDetails.incident.comuna,
        incidentDetails.incident.region
    ).joinToString(", ").ifEmpty { "Dirección no encontrada" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            AsyncImage(
                model = incidentDetails.photoUrl,
                error = painterResource(id = R.drawable.default_incident),
                contentDescription = "Imagen del Incidente",
                modifier = Modifier.fillMaxWidth().height(250.dp),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = when (incident.estado) {
                        IncidenteEstado.ASIGNADO.name -> Color(0xFFFFA726)
                        IncidenteEstado.RESUELTO.name -> Color(0xFF4CAF50)
                        else -> MaterialTheme.colorScheme.error
                    }
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Estado",
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = incident.estado.replaceFirstChar { it.uppercase() },
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(incident.titulo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(text = fullAddress, style = MaterialTheme.typography.labelMedium)
                if (incident.latitud != null && incident.longitud != null) {
                    Text(
                        text = "Lat: ${incident.latitud}, Lon: ${incident.longitud}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Reportado el: ${formatDate(incident.fechaRegistro)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(incident.detalle, style = MaterialTheme.typography.bodyMedium)
                if (incidentDetails.asignadoANombre != null) {
                    Text(
                        "Asignado a: ${incidentDetails.asignadoANombre}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        when {
                            canBeTaken -> {
                                Button(onClick = onTakeIncident, modifier = Modifier.fillMaxWidth()) {
                                    Text("Tomar incidente")
                                }
                            }
                            canBeClosed -> {
                                Button(
                                    onClick = onCloseIncident,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                ) {
                                    Text("Cerrar Incidente")
                                }
                            }
                            isAssignedToOther && isBombero -> {
                                Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                                    Text("Ya asignado")
                                }
                            }
                            isResolved -> {
                                Text(
                                    "Incidente Resuelto",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }

                    if (!isResolved && isAdmin) {
                        OutlinedButton(onClick = onEditIncident) {
                            Text("Editar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditIncidentDialog(
    viewModel: IncidenteViewModel,
    onDismiss: () -> Unit,
    isAdmin: Boolean
) {
    val editState by viewModel.editState.collectAsStateWithLifecycle()
    val incident = editState.selectedIncident?.incident ?: return
    var titulo by remember { mutableStateOf(incident.titulo) }
    var detalle by remember { mutableStateOf(incident.detalle) }
    var bomberoUsername by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageDialog by remember { mutableStateOf(false) }

    if (showImageDialog) {
        ImagePickerDialog(
            showDialog = true,
            onDismissRequest = { showImageDialog = false },
            onImageSelected = { uri ->
                imageUri = uri
                showImageDialog = false
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SRBackgroundWhite)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Editar Incidente", style = MaterialTheme.typography.headlineSmall)
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = detalle,
                    onValueChange = { detalle = it },
                    label = { Text("Detalle") },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )

                if (isAdmin) {
                    OutlinedTextField(
                        value = bomberoUsername,
                        onValueChange = { bomberoUsername = it },
                        label = { Text("Asignar a Bombero (username)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(onClick = { showImageDialog = true }) {
                    Text(if (imageUri == null) "Cambiar Imagen" else "Imagen Seleccionada ✓")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.onSaveChangesClicked(titulo, detalle, imageUri, bomberoUsername.takeIf { it.isNotBlank() })
                        },
                        enabled = titulo.isNotBlank() && detalle.isNotBlank()
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
