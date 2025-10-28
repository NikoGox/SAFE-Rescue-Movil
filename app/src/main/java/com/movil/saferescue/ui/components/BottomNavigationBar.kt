package com.movil.saferescue.ui.components

import android.net.http.SslCertificate.saveState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import com.movil.saferescue.navigation.Route
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.TextStyle
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.movil.saferescue.ui.theme.SRPrimaryBlue


data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

val navItems = listOf(
    BottomNavItem("Incidentes", Icons.Filled.Warning, Route.Incidente.path),
    BottomNavItem("Inicio", Icons.Filled.Home, Route.Home.path),
    BottomNavItem("Chat", Icons.Filled.MailOutline, Route.Chat.path)
)

@Composable
fun SRBottomNavigationBar(
    currentRoute: String?,
    onItemClick: (String) -> Unit
) {
    val ICON_SIZE = 30.dp
    val TEXT_FONT_SIZE = 18.sp

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