package com.movil.saferescue.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movil.saferescue.data.local.notification.NotificationEntity
import com.movil.saferescue.ui.viewmodel.NotificationUiState
import com.movil.saferescue.ui.viewmodel.NotificationViewModel
import com.movil.saferescue.ui.viewmodel.NotificationViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun NotificationScreenVm(
    notificationViewModelFactory: NotificationViewModelFactory,
    onNavigateBack: () -> Unit
) {
    val viewModel: NotificationViewModel = viewModel(factory = notificationViewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NotificationScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onMarkAllAsRead = viewModel::markAllAsRead,
        onDeleteAll = viewModel::deleteAllNotifications,
        onDismissNotification = { notification -> viewModel.deleteNotification(notification.id) },
        onClearError = viewModel::clearError
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationScreen(
    uiState: NotificationUiState,
    onNavigateBack: () -> Unit,
    onMarkAllAsRead: () -> Unit,
    onDeleteAll: () -> Unit,
    onDismissNotification: (NotificationEntity) -> Unit,
    onClearError: () -> Unit
) {
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        // Icono actualizado para usar la versión "automirrored"
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (uiState.unreadCount > 0) {
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
                    if (uiState.notifications.isNotEmpty()) {
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
                uiState.notifications.isEmpty() -> {
                    Text(
                        text = "No tienes notificaciones",
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    NotificationList(
                        notifications = uiState.notifications,
                        onDismiss = onDismissNotification
                    )
                }
            }

            val snackbarHostState = remember { SnackbarHostState() }
            LaunchedEffect(uiState.errorMsg) {
                uiState.errorMsg?.let {
                    snackbarHostState.showSnackbar(
                        message = it,
                        actionLabel = "OK"
                    )
                    onClearError()
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar todas las notificaciones? Esta acción no se puede deshacer.") },
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

// --- CAMBIOS PRINCIPALES A PARTIR DE AQUÍ ---

@Composable
private fun NotificationList(
    notifications: List<NotificationEntity>,
    onDismiss: (NotificationEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = notifications,
            key = { notification -> notification.id }
        ) { notification ->
            // Ya no hay SwipeToDismissBox. Llamamos directamente a NotificationItem.
            NotificationItem(
                notification = notification,
                onDeleteClick = { onDismiss(notification) } // Pasamos la acción de borrado
            )
        }
    }
}

@Composable
private fun NotificationItem(
    notification: NotificationEntity,
    onDeleteClick: () -> Unit // Nuevo parámetro para manejar el clic en el botón de eliminar
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp), // Ajuste de padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        )
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (notification.isRead) 0.dp else 16.dp)
            ) {
                Text(
                    text = notification.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatDate(notification.fechaSubida),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Botón de eliminar (papelera) añadido al final del Row
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar notificación",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
