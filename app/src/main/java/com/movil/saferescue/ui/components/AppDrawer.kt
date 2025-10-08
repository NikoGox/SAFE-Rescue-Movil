package com.movil.saferescue.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.movil.saferescue.navigation.Route // Importamos nuestras rutas

// 1. Data Class para los ítems del menú
data class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val route: Route,
)

// 2. Composable del Drawer
@Composable
fun AppDrawer(
    currentRoute: String?,
    onNavigate: (Route) -> Unit, // Recibe la acción de navegación
    onCloseDrawer: () -> Unit, // Recibe la acción para cerrar el menú
    modifier: Modifier = Modifier
) {
    // Definimos los ítems que se mostrarán en el menú
    val items = defaultDrawerItems()

    ModalDrawerSheet(modifier = modifier) {
        // Encabezado del Drawer
        Text(
            "Menú SAFE Rescue",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleLarge
        )
        Divider() // Separador visual

        // Lista de ítems
        items.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.label) },
                // Determina si el ítem actual es la ruta seleccionada
                selected = currentRoute == item.route.path,
                onClick = {
                    onNavigate(item.route) // Navegamos
                    onCloseDrawer()       // Cerramos el menú
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                colors = NavigationDrawerItemDefaults.colors()
            )
        }
    }
}

// 3. Helper para construir la lista de ítems estándar
@Composable
fun defaultDrawerItems(): List<DrawerItem> = listOf(
    DrawerItem("Home", Icons.Filled.Home, Route.Home),
    DrawerItem("Login", Icons.Filled.AccountCircle, Route.Login),
    DrawerItem("Registro", Icons.Filled.Person, Route.Register)
)