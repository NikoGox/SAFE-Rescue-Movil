package com.movil.saferescue.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.movil.saferescue.R
import com.movil.saferescue.data.local.incidente.IncidenteEntity
import com.movil.saferescue.ui.viewmodel.IncidentWithDetails
import com.movil.saferescue.ui.viewmodel.IncidentsViewModel
import com.movil.saferescue.ui.viewmodel.IncidentsViewModelFactory

@Composable
fun IncidentsScreen(
    factory: IncidentsViewModelFactory
) {
    val vm: IncidentsViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (state.error != null) {
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center)
            )
            // --- 4. ACTUALIZAR LA COMPROBACIÓN ---
        } else if (state.incidentsWithDetails.isEmpty()) {
            Text(
                text = "No hay incidentes reportados.",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- 5. ITERAR SOBRE LA NUEVA LISTA ---
                items(state.incidentsWithDetails) { incidentWithDetails ->
                    IncidentCard(
                        incidentDetails = incidentWithDetails, // <-- Pasar el objeto completo
                        formatDate = vm::formatDate,
                        onTakeIncident = { vm.onTakeIncidentClicked(incidentWithDetails.incident.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun IncidentCard(
    incidentDetails: IncidentWithDetails,
    formatDate: (Long) -> String,
    onTakeIncident: () -> Unit
) {
    val incident = incidentDetails.incident
    val photoUrl = incidentDetails.photoUrl

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, Color(0xFFADD8E6)), // Borde azul claro
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // --- Fila de Estado ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Estado",
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = "Localizado", // Puedes hacer esto dinámico en el futuro
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Título, Lugar y Fecha ---
            Text(
                text = incident.titulo,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            // Simulación de lugar y comuna
            Text(
                text = "Parque natural San Carlos de Apoquindo\nLas Condes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatDate(incident.fechaRegistro),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Image(
                painter = painterResource(id = R.drawable.default_incident), // Reemplazar con coil/glide para state.fotoUrl
                contentDescription = "Foto de incidente",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            // --- Imagen del Incidente ---
            /**
             * AsyncImage(
                model = photoUrl, // <-- ¡AQUÍ ESTÁ LA MAGIA! Se usa la URL dinámica.
                placeholder = painterResource(id = R.drawable.sr_logo),
                error = painterResource(id = R.drawable.perfil_default), // Reemplaza por un recurso de error para incidentes si tienes
                contentDescription = "Imagen del Incidente",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
             **/

            Spacer(modifier = Modifier.height(12.dp))

            // --- Detalle del Incidente ---
            Text(
                text = incident.detalle,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Botón de Acción ---
            Button(
                onClick = onTakeIncident,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50), // Verde similar al de la imagen
                    contentColor = Color.White
                )
            ) {
                Text("Tomar incidente", fontWeight = FontWeight.Bold)
            }
        }
    }
}
