package com.movil.saferescue.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold // ✅ Necesario para TopBar y BottomBar
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.movil.saferescue.ui.components.AppTopBar // Importar el componente
import com.movil.saferescue.ui.components.SRBottomNavigationBar // Importar el componente

@Composable
fun HomeScreen(
    onGoLogin: () -> Unit, // Para el botón de "Volver a Login"
    onGoRegister: () -> Unit // No se usa aquí directamente, pero se mantiene para la firma
){
    // ⚠️ ELIMINADA la línea: val primaryBlue = PrimaryBlue

    // ✅ Usamos Scaffold para estructurar la pantalla con barras y contenido
    Scaffold(
        topBar = {
            AppTopBar(
                title = "SAFE Rescue",
                onOpenDrawer = { /* TODO: Abrir Drawer */ },
                onNotificationsClicked = { /* TODO: Mostrar Notificaciones */ }
            )
        },
        bottomBar = {
            SRBottomNavigationBar(
                currentRoute = "home_route", // Asumimos que esta es la ruta actual
                onItemClick = { route -> /* TODO: Navegar a la ruta */ }
            )
        }
    ) { paddingValues ->
        // ✅ CORRECCIÓN: Usar el color de fondo del tema
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues), // Aplicar el padding de las barras
            contentAlignment = Alignment.Center
        ){
            Column(horizontalAlignment = Alignment.CenterHorizontally){

                Text(
                    text = "Apartado en construcción",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // ✅ MINI BOTÓN PARA VOLVER A LOGIN
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
        HomeScreen(onGoLogin = {}, onGoRegister = {})
    }
}