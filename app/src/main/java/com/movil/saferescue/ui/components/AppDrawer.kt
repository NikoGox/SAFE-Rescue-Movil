package com.movil.saferescue.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.movil.saferescue.R // Asegúrate de tener tu logo

// La data class DrawerItem y la función defaultDrawerItems no se usan en este
// Composable, pero las dejamos por si las necesitas en el futuro.
// Su definición está correcta.
data class DrawerItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    isAuthenticated: Boolean,
    onCloseDrawer: () -> Unit,
    onHome: () -> Unit,
    onGoProfile: () -> Unit,
    onGoLogin: () -> Unit,
    onGoRegister: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet {
        // Encabezado (sin cambios, ya estaba bien)
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

        // --- INICIO DE LAS CORRECCIONES ---

        // 1. Elemento "Inicio" corregido y completo
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            selected = false, // Lo ponemos en false ya que no manejamos la selección
            onClick = {
                onHome()
                onCloseDrawer()
            }
        )

        Spacer(Modifier.height(8.dp))

        // 2. Elementos condicionales corregidos
        if (isAuthenticated) {
            // Usuario autenticado
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Mi Perfil") },
                label = { Text("Mi Perfil") },
                selected = false,
                onClick = {
                    onGoProfile()
                    onCloseDrawer()
                }
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp)) // Mejor separación visual
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Logout, contentDescription = "Cerrar Sesión") },
                label = { Text("Cerrar Sesión") },
                selected = false,
                onClick = {
                    onLogout()
                    onCloseDrawer()
                }
            )
        } else {
            // Usuario no autenticado
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Login, contentDescription = "Iniciar Sesión") },
                label = { Text("Iniciar Sesión") },
                selected = false,
                onClick = {
                    onGoLogin()
                    onCloseDrawer()
                }
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Registrarse") },
                label = { Text("Registrarse") },
                selected = false,
                onClick = {
                    onGoRegister()
                    onCloseDrawer()
                }
            )
        }
        // --- FIN DE LAS CORRECCIONES ---
    }
}

// Esta función auxiliar no se está usando en el AppDrawer de arriba, pero
// la dejamos por si acaso. Su definición es correcta.
@Composable
fun defaultDrawerItems(
    onHome: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit
): List<DrawerItem> = listOf(
    DrawerItem(label = "Home", icon = Icons.Filled.Home, onClick = onHome),
    DrawerItem(label = "Login", icon = Icons.Filled.AccountCircle, onClick = onLogin),
    DrawerItem(label = "Registro", icon = Icons.Filled.Person, onClick = onRegister)
)
