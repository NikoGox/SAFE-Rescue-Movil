package com.movil.saferescue.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movil.saferescue.data.local.incidente.IncidenteEstado
import com.movil.saferescue.data.local.incidente.IncidentWithDetails
import com.movil.saferescue.ui.viewmodel.IncidenteViewModel

@Composable
fun IncidentesAsignadosScreen(viewModel: IncidenteViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()

    val assignedIncidents = state.incidentsWithDetails.filter { 
        it.incident.asignadoA == currentUserId && it.incident.estado == IncidenteEstado.ASIGNADO.name
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (assignedIncidents.isEmpty()) {
            Text("No tienes incidentes asignados.", modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(assignedIncidents) { incidentDetails ->
                    AssignedIncidentCard(
                        incidentDetails = incidentDetails,
                        onCloseIncident = { viewModel.onCloseIncidentClicked(incidentDetails.incident.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AssignedIncidentCard(
    incidentDetails: IncidentWithDetails,
    onCloseIncident: () -> Unit
) {
    val incident = incidentDetails.incident
    val fullAddress = listOfNotNull(
        incident.direccion,
        incident.comuna,
        incident.region
    ).joinToString(", ").ifEmpty { "Dirección no encontrada" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(incident.titulo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(fullAddress, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = onCloseIncident,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Cerrar Incidente")
                }
            }
        }
    }
}