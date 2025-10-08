package com.movil.saferescue.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // Importación necesaria para sp
import androidx.compose.ui.Modifier // Importación necesaria para Modifier
import androidx.compose.foundation.layout.size // Importación necesaria para Modifier.size
import androidx.compose.ui.text.TextStyle // ⬅️ Opcional, pero bueno para la consistencia
import com.movil.saferescue.ui.theme.SRPrimaryBlue // Asumiendo que has definido este color

// Definición de los ítems de la barra para facilitar el mapeo
data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

val navItems = listOf(
    BottomNavItem("Incidentes", Icons.Filled.Warning, "incidents_route"),
    BottomNavItem("Inicio", Icons.Filled.Home, "home_route"),
    BottomNavItem("Chat", Icons.Filled.MailOutline, "chat_route")
)

@Composable
fun SRBottomNavigationBar(
    currentRoute: String?,
    onItemClick: (String) -> Unit
) {
    val ICON_SIZE = 30.dp
    val TEXT_FONT_SIZE = 18.sp // Define el tamaño de la fuente aquí

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 4.dp
    ) {
        navItems.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(ICON_SIZE)
                    )
                },
                label = {
                    Text(
                        // 🟢 CORRECCIÓN CLAVE: Usamos el parámetro fontSize dentro de Text
                        text = item.title,
                        fontSize = TEXT_FONT_SIZE
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.White,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    selectedIconColor = SRPrimaryBlue,
                    selectedTextColor = SRPrimaryBlue
                )
            )
        }
    }
}