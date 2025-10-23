package com.movil.saferescue.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
// 1. IMPORTACIONES ACTUALIZADAS
import com.movil.saferescue.data.local.mensaje.MensajeEntity
import com.movil.saferescue.ui.components.InfoRowItem
import com.movil.saferescue.ui.viewmodel.AlertasUiState
import com.movil.saferescue.ui.viewmodel.MensajeViewModel
import com.movil.saferescue.ui.viewmodel.MensajeViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun NotificationScreenVm(
    // 2. EL FACTORY AHORA ES MensajeViewModelFactory
    mensajeViewModelFactory: MensajeViewModelFactory,
    onNavigateBack: () -> Unit
) {
    // 3. SE CREA UNA INSTANCIA DEL NUEVO MensajeViewModel
    val viewModel: MensajeViewModel = viewModel(factory = mensajeViewModelFactory)
    // 4. SE CONSUME EL NUEVO ESTADO AlertasUiState
    val uiState by viewModel.alertasUiState.collectAsStateWithLifecycle()

    NotificationScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        // 5. SE ENLAZAN LAS ACCIONES A LOS NUEVOS MÉTODOS DEL VIEWMODEL
        onMarkAllAsRead = viewModel::marcarAlertasComoLeidas,
        onDeleteAll = viewModel::eliminarAlertas,
        onDismissNotification = { mensaje -> viewModel.eliminarMensaje(mensaje.id) },
        onClearError = viewModel::limpiarError
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationScreen(
    // 6. El ESTADO AHORA ES DE TIPO AlertasUiState
    uiState: AlertasUiState,
    onNavigateBack: () -> Unit,
    onMarkAllAsRead: () -> Unit,
    onDeleteAll: () -> Unit,
    // La acción de descarte ahora recibe una MensajeEntity
    onDismissNotification: (MensajeEntity) -> Unit,
    onClearError: () -> Unit
) {
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alertas del Sistema") }, // Título actualizado para claridad
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // 7. LA LÓGICA DE LA UI USA LOS NOMBRES DEL NUEVO ESTADO
                    if (uiState.contadorNoLeidas > 0) { // Antes: uiState.unreadCount
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = { Text("Marcar todas como leídas") },
                            state = rememberTooltipState()
                        ) {
                            IconButton(onClick = onMarkAllAsRead) {
                                Icon(Icons.Default.DoneAll, contentDescription = "Marcar todas como leídas")
                            }
                        }
                    }
                    if (uiState.alertas.isNotEmpty()) { // Antes: uiState.notifications
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = { Text("Eliminar todas") },
                            state = rememberTooltipState()
                        ) {
                            IconButton(onClick = { showDeleteAllDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar todas")
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.alertas.isEmpty() -> { // Antes: uiState.notifications
                    Text(
                        text = "No tienes alertas", // Texto actualizado
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    NotificationList(
                        notifications = uiState.alertas, // Se pasa la nueva lista de alertas
                        onDismiss = onDismissNotification
                    )
                }
            }

            val snackbarHostState = remember { SnackbarHostState() }
            LaunchedEffect(uiState.errorMsg) {
                uiState.errorMsg?.let {
                    snackbarHostState.showSnackbar(message = it, actionLabel = "OK")
                    onClearError()
                }
            }
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar todas las alertas? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAll()
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun NotificationList(
    // 8. LA LISTA AHORA RECIBE UNA LISTA DE MensajeEntity
    notifications: List<MensajeEntity>,
    onDismiss: (MensajeEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = notifications,
            key = { notification -> notification.id }
        ) { notification ->
            // El componente InfoRowItem funciona sin cambios, ya que solo recibe datos primitivos.
            InfoRowItem(
                title = notification.titulo,
                subtitle = notification.mensaje,
                timestamp = formatDate(notification.fechaSubida),
                isUnread = !notification.isRead,
                onDeleteClick = { onDismiss(notification) }
            )
        }
    }
}
