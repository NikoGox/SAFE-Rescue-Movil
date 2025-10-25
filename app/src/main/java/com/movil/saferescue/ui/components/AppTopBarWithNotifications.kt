package com.movil.saferescue.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movil.saferescue.ui.viewmodel.MensajeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBarWithNotifications(
    viewModel: MensajeViewModel,
    isAuthenticated: Boolean,
    isChatScreen: Boolean,
    isPanelOpen: Boolean,
    onOpenDrawer: () -> Unit,
    onNavigateBack: () -> Unit,
    onTogglePanel: () -> Unit
) {
    val unreadCount by viewModel.userUnreadCount.collectAsStateWithLifecycle()

    TopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("SAFE Rescue", textAlign = TextAlign.Center)
            }
        },
        navigationIcon = {
            when {
                isChatScreen -> {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
                isPanelOpen -> {
                    IconButton(onClick = onTogglePanel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cerrar notificaciones")
                    }
                }
                isAuthenticated -> {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                    }
                }
            }
        },
        actions = {
            if (isAuthenticated) {
                IconButton(onClick = onTogglePanel) {
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge { Text(text = unreadCount.toString()) }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notificaciones"
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}
