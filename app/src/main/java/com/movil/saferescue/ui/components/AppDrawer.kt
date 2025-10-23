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
import com.movil.saferescue.navigation.Route // <<< CORRECCIÓN: Importa tus rutas

// 1. Un modelo de datos más completo para cada ítem del menú.
data class DrawerMenuItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    currentRoute: String, // Recibe la ruta actual para saber cuál resaltar.
    navigateTo: (String) -> Unit, // Una única función para navegar.
    onLogout: () -> Unit,
    isAuthenticated: Boolean // Todavía lo necesitamos para saber qué menú mostrar.
) {
    // 2. Definimos las listas de menús aquí mismo. Mucho más limpio.
    val menuItemsForAuthenticated = listOf(
        DrawerMenuItem(Route.Home.path, "Inicio", Icons.Default.Home),
        DrawerMenuItem(Route.Incidente.path, "Incidentes", Icons.Default.Report),
        DrawerMenuItem(Route.Chat.path, "Chat", Icons.Default.Chat),
        DrawerMenuItem(Route.Notification.path, "Notificaciones", Icons.Default.Notifications),
        DrawerMenuItem(Route.Profile.path, "Mi Perfil", Icons.Default.AccountCircle)
    )

    val menuItemsForGuest = listOf(
        DrawerMenuItem(Route.Login.path, "Iniciar Sesión", Icons.Default.Login),
        DrawerMenuItem(Route.Register.path, "Registrarse", Icons.Default.PersonAdd)
    )

    ModalDrawerSheet {
        // Encabezado (sin cambios)
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

        // 3. Lógica mucho más simple para mostrar los menús.
        val itemsToShow = if (isAuthenticated) menuItemsForAuthenticated else menuItemsForGuest

        itemsToShow.forEach { item ->
            NavigationDrawerItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                // El ítem se marca como seleccionado si su ruta coincide con la actual.
                selected = item.route == currentRoute,
                onClick = { navigateTo(item.route) } // La acción es siempre la misma: navegar.
            )
        }

        // El ítem de Logout se maneja por separado si el usuario está autenticado.
        if (isAuthenticated) {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Logout, contentDescription = "Cerrar Sesión") },
                label = { Text("Cerrar Sesión") },
                selected = false,
                onClick = onLogout // El logout es una acción especial.
            )
        }
    }
}
