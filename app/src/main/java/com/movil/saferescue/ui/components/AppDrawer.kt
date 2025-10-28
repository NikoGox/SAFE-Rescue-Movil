package com.movil.saferescue.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.movil.saferescue.R
import com.movil.saferescue.navigation.Route

data class DrawerMenuItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    currentRoute: String,
    navigateTo: (String) -> Unit,
    onLogout: () -> Unit,
    isAuthenticated: Boolean,
    isAdmin: Boolean,
    isBombero: Boolean
) {
    val menuItemsForAuthenticated = listOf(
        DrawerMenuItem(Route.Home.path, "Inicio", Icons.Default.Home),
        DrawerMenuItem(Route.Profile.path, "Mi Perfil", Icons.Default.AccountCircle)
    )

    val menuItemsForGuest = listOf(
        DrawerMenuItem(Route.Login.path, "Iniciar Sesión", Icons.Default.Login),
        DrawerMenuItem(Route.Register.path, "Registrarse", Icons.Default.PersonAdd)
    )

    ModalDrawerSheet {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.sr_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(80.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "SAFE Rescue",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Divider()
        Spacer(Modifier.height(12.dp))

        val itemsToShow = if (isAuthenticated) menuItemsForAuthenticated else menuItemsForGuest

        itemsToShow.forEach { item ->
            NavigationDrawerItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = item.route == currentRoute,
                onClick = { navigateTo(item.route) }
            )
        }

        if (isAuthenticated) {
            if (isAdmin) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "Crear Incidente") },
                    label = { Text("Crear Incidente") },
                    selected = currentRoute == Route.CrearIncidente.path,
                    onClick = { navigateTo(Route.CrearIncidente.path) }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.AddAlert, contentDescription = "Crear Notificación") },
                    label = { Text("Crear Notificación") },
                    selected = currentRoute == Route.Notification.path,
                    onClick = { navigateTo(Route.Notification.path) }
                )
            }

            if (isBombero) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "Mis Incidentes") },
                    label = { Text("Mis Incidentes") },
                    selected = currentRoute == Route.IncidentesAsignados.path,
                    onClick = { navigateTo(Route.IncidentesAsignados.path) }
                )
            }

            // Opción de cerrar sesión
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Logout, contentDescription = "Cerrar Sesión") },
                label = { Text("Cerrar Sesión") },
                selected = false,
                onClick = onLogout
            )
        }
    }
}
