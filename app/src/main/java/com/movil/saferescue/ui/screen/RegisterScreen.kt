package com.movil.saferescue.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movil.saferescue.ui.components.LoginTextField // Reutilizamos este componente
import com.movil.saferescue.ui.theme.PrimaryBlue

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onGoLogin: () -> Unit
) {
    val primaryBlue = PrimaryBlue

    // --- Estados para almacenar los datos del formulario ---
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Crear Nueva Cuenta",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // --- Campo de Email ---
        LoginTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            icon = Icons.Filled.MailOutline,
            keyboardType = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(Modifier.height(16.dp))

        // --- Campo de Contraseña ---
        LoginTextField(
            value = password,
            onValueChange = { password = it },
            label = "Contraseña",
            icon = Icons.Filled.Lock,
            visualTransformation = PasswordVisualTransformation(),
            keyboardType = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(Modifier.height(32.dp))

        // --- Botón de Registro ---
        Button(
            // Al hacer clic, navega a HomeScreen
            onClick = onRegistered,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
        ) {
            Text(
                "Registrarme",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                fontSize = 20.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        // --- Botón para volver a Login ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("¿Ya tienes cuenta?")
            // Se usa TextButton para mejor consistencia visual
            TextButton(onClick = onGoLogin) {
                Text("Iniciar Sesión", color = primaryBlue)
            }
        }
    }
}

// Preview actualizada
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun RegisterScreenPreview() {
    MaterialTheme {
        RegisterScreen(onRegistered = {}, onGoLogin = {})
    }
}