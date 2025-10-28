package com.movil.saferescue.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.view.View
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.movil.saferescue.data.local.incidente.IncidenteEstado
import com.movil.saferescue.data.local.incidente.IncidentWithDetails
import com.movil.saferescue.ui.viewmodel.IncidenteViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.movil.saferescue.R
import java.util.Locale

@SuppressLint("MissingPermission")
@Suppress("UNUSED_PARAMETER")
@Composable
fun HomeScreen(
    isAuthenticated: Boolean,
    onGoLogin: () -> Unit,
    onGoRegister: () -> Unit,
    incidenteViewModel: IncidenteViewModel
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val incidentsState by incidenteViewModel.uiState.collectAsStateWithLifecycle()

    var selectedIncident by remember { mutableStateOf<IncidentWithDetails?>(null) }
    var userGeoPoint by remember { mutableStateOf<GeoPoint?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] != true && permissions[Manifest.permission.ACCESS_COARSE_LOCATION] != true) {
                Toast.makeText(context, "Se requieren permisos de ubicación para mostrar el mapa.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isAuthenticated) {
            val mapView = remember { MapView(context) }

            val activeColor = MaterialTheme.colorScheme.error
            val assignedColor = Color(0xFFFFC107)
            val resolvedColor = Color(0xFF4CAF50)
            val userLocationColor = MaterialTheme.colorScheme.primary

            AndroidView(
                factory = {
                    mapView.apply {
                        id = View.generateViewId()
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        setBuiltInZoomControls(false)
                        controller.setZoom(10.0)
                        controller.setCenter(GeoPoint(-33.44889, -70.669265))
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { map ->
                    map.overlays.clear()

                    userGeoPoint?.let {
                        val userMarker = Marker(map).apply {
                            position = it
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            // Usar drawable del proyecto para la persona (se tintará con el color de la marca)
                            val userIcon = ContextCompat.getDrawable(context, R.drawable.ic_user_person)!!
                            icon = userIcon.mutate().apply { DrawableCompat.setTint(this, userLocationColor.toArgb()) }
                            title = "Mi Ubicación"
                        }
                        map.overlays.add(userMarker)
                    }

                    incidentsState.incidentsWithDetails.forEach { incidentDetails ->
                        val incident = incidentDetails.incident
                        if (incident.latitud != null && incident.longitud != null) {
                            val marker = Marker(map).apply {
                                position = GeoPoint(incident.latitud, incident.longitud)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                // Usar drawable del proyecto para el pin (sólido y grande)
                                val pinDrawable = ContextCompat.getDrawable(context, R.drawable.ic_pin_marker)!!
                                val color = when (incident.estado) {
                                    IncidenteEstado.ASIGNADO.name -> assignedColor
                                    IncidenteEstado.RESUELTO.name -> resolvedColor
                                    else -> activeColor
                                }
                                icon = pinDrawable.mutate().apply { DrawableCompat.setTint(this, color.toArgb()) }
                                setOnMarkerClickListener { _, _ ->
                                    selectedIncident = incidentDetails
                                    true
                                }
                            }
                            map.overlays.add(marker)
                        }
                    }
                    map.invalidate()
                }
            )

            // Gesture absorbing overlay
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.1f)
                    .align(Alignment.CenterStart)
                    .pointerInput(Unit) { detectTapGestures { } }
            )

            // UI Controls (Zoom and My Location)
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val buttonColors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary, 
                    contentColor = Color.White
                )
                Button(onClick = { mapView.controller.zoomIn() }, shape = CircleShape, colors = buttonColors) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In")
                }
                Button(onClick = { mapView.controller.zoomOut() }, shape = CircleShape, colors = buttonColors) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            val newGeoPoint = GeoPoint(it.latitude, it.longitude)
                            userGeoPoint = newGeoPoint
                            mapView.controller.animateTo(newGeoPoint, 15.0, 1000L)
                        }
                    }
                }, shape = CircleShape, colors = buttonColors) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Mi Ubicación")
                }
            }

            // Bottom Sheet for Incident Details
            AnimatedVisibility(
                visible = selectedIncident != null,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                selectedIncident?.let {
                    IncidentDetailsSheet(incidentDetails = it, onClose = { selectedIncident = null })
                }
            }

        } else {
            // ... Login/Register UI
        }
    }
}

@Composable
fun IncidentDetailsSheet(
    incidentDetails: IncidentWithDetails,
    onClose: () -> Unit
) {
    val incident = incidentDetails.incident

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(incident.titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(listOfNotNull(incident.direccion, incident.comuna, incident.region).joinToString(", "), style = MaterialTheme.typography.bodyMedium)
            if (incidentDetails.asignadoANombre != null) {
                Text("Asignado a: ${incidentDetails.asignadoANombre}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            if (incident.latitud != null && incident.longitud != null) {
                Text(
                    text = "Lat: ${String.format(Locale.US, "%.5f", incident.latitud)}, Lon: ${String.format(Locale.US, "%.5f", incident.longitud)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
