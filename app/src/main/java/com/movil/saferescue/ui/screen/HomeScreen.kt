package com.movil.saferescue.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.movil.saferescue.ui.components.AppTopBar
import com.movil.saferescue.ui.components.SRBottomNavigationBar

@Composable
fun HomeScreen(
    onGoLogin: () -> Unit,
    onGoRegister: () -> Unit,
    onOpenDrawer: () -> Unit
){
    Scaffold(
        topBar = {
            AppTopBar(
                title = "SAFE Rescue",
                onOpenDrawer = onOpenDrawer,
                onNotificationsClicked = { /* TODO: Mostrar Notificaciones */ }
            )
        },
        bottomBar = {
            SRBottomNavigationBar(
                currentRoute = "home_route",
                onItemClick = { route -> /* TODO: Navegar a la ruta */ }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ){
            Column(horizontalAlignment = Alignment.CenterHorizontally){

                Text(
                    text = "Apartado en construcción",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                TextButton(
                    onClick = onGoLogin,
                    modifier = Modifier.padding(top = 26.dp)
                ) {
                    Text(
                        "Volver a Iniciar Sesión",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(onGoLogin = {}, onGoRegister = {}, onOpenDrawer = {})
    }
}
