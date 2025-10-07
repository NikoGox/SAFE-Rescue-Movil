package com.movil.saferescue.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movil.saferescue.ui.theme.SRPrimaryBlue
import androidx.compose.foundation.layout.size // ⬅️ ¡Nueva Importación!

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onOpenDrawer: () -> Unit,
    onNotificationsClicked: () -> Unit
) {
    // Definimos el tamaño del icono (ej. 24.dp es estándar, puedes usar 28.dp si quieres que sean más grandes)
    val ICON_SIZE = 38.dp

    CenterAlignedTopAppBar(
        modifier = Modifier
            // Bordes inferiores redondeados de 16.dp
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            // El color de contenedor está usando tu color directo (SRPrimaryBlue)
            containerColor = SRPrimaryBlue,
            // Los iconos y texto serán blancos (onPrimary)
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            // Icono de Menú (Hamburguesa)
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menú Lateral",
                    // ✅ APLICANDO EL TAMAÑO AL ICONO DE NAVEGACIÓN
                    modifier = Modifier.size(ICON_SIZE)
                )
            }
        },
        actions = {
            // Icono de Notificaciones
            IconButton(onClick = onNotificationsClicked) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notificaciones",
                    // ✅ APLICANDO EL TAMAÑO AL ICONO DE ACCIÓN
                    modifier = Modifier.size(ICON_SIZE)
                )
            }
        }
    )
}