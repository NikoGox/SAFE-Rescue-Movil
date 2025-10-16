package com.movil.saferescue.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// --- CAMBIO ÚNICO: La pantalla ahora acepta el estado de autenticación ---
@Composable
fun HomeScreen(
    isAuthenticated: Boolean,
    onGoLogin: () -> Unit,
    onGoRegister: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isAuthenticated) {
            // Contenido para un usuario que ha iniciado sesión
            Text(
                text = "¡Bienvenido!\nEstás en la pantalla de Inicio.",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            // Aquí puedes añadir más contenido como una lista de incidentes, noticias, etc.
        } else {
            // Contenido para un usuario que NO ha iniciado sesión
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Por favor, inicia sesión para ver el contenido.",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onGoLogin) {
                    Text("Ir a Iniciar Sesión")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onGoRegister) {
                    Text("Ir a Registrarse")
                }
            }
        }
    }
}
