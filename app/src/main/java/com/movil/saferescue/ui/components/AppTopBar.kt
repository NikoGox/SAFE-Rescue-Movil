package com.movil.saferescue.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movil.saferescue.navigation.Route // <<< CORRECCIÓN: Importa tus rutas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    isAuthenticated: Boolean,
    onOpenDrawer: () -> Unit,
    onLogout: () -> Unit,
    // <<< CORRECCIÓN 1: Se reemplazan múltiples funciones por una sola >>>
    onNavigate: (route: String) -> Unit
) {
    val iconSize = 28.dp

    CenterAlignedTopAppBar(
        modifier = Modifier
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        title = {
            Text(
                text = "SAFE Rescue",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menú",
                    modifier = Modifier.size(iconSize)
                )
            }
        },
        actions = {
            // <<< CORRECCIÓN 2: La lógica se simplifica >>>
            if (isAuthenticated) {
                // Usuario autenticado
                IconButton(onClick = { onNavigate(Route.Notification.path) }) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notificaciones",
                        modifier = Modifier.size(38.dp) // Un tamaño mayor para notificaciones está bien
                    )
                }
                // Aquí podrías agregar un menú desplegable con más opciones, como el logout
                // IconButton(onClick = onLogout) { ... }
            } else {
                // Usuario no autenticado
                IconButton(onClick = { onNavigate(Route.Login.path) }) {
                    Icon(
                        Icons.Filled.Login,
                        contentDescription = "Login",
                        modifier = Modifier.size(iconSize)
                    )
                }
                IconButton(onClick = { onNavigate(Route.Register.path) }) {
                    Icon(
                        Icons.Filled.PersonAdd,
                        contentDescription = "Registro",
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }
    )
}
