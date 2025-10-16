package com.movil.saferescue.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    isAuthenticated: Boolean,
    onOpenDrawer: () -> Unit,
    onGoLogin: () -> Unit,
    onGoRegister: () -> Unit,
    onGoNotifications: () -> Unit,
    onLogout: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
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

            if (isAuthenticated) {
                // Usuario autenticado
                IconButton(onClick = onGoNotifications) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notificaciones",
                        modifier = Modifier.size(38.dp)
                    )
                }
            } else {

                IconButton(onClick = onGoLogin) {
                    Icon(
                        Icons.Filled.Login,
                        contentDescription = "Login",
                        modifier = Modifier.size(iconSize)
                    )
                }
                IconButton(onClick = onGoRegister) {
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
