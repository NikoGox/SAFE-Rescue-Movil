package com.movil.saferescue.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movil.saferescue.R // Importación para acceder a R.drawable.logo_safe_rescue

// Colores base para la estética moderna
val PrimaryBlue = Color(0xFF1565C0) // Un azul profundo (similar a Inter)
val SecondaryRed = Color(0xFFD32F2F) // Un rojo para acentos de emergencia

// ----------------------------------------------------------------------------------
// COMPONENTE DE TEXT FIELD REUTILIZABLE
// ----------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardType: KeyboardOptions
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = PrimaryBlue) },
        keyboardOptions = keyboardType,
        visualTransformation = visualTransformation,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}



@Composable
fun InicioSesionScreen(
    onLoginClicked: () -> Unit // Función requerida para que el MainActivity la pueda llamar
) {
    // Definimos los estados para la interacción de la UI
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var rememberMe by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bienvenido a",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium), // Montserrat/Inter Medium
            color = Color.Black
        )
        Spacer(Modifier.height(32.dp))
        // Logo y Título
        // NOTA: Asegúrate de que tu imagen 'logo_safe_rescue' esté en res/drawable
        Image(
            painter = painterResource(id = R.drawable.sr_logo),
            contentDescription = "Logo de SAFE Rescue",
            modifier = Modifier.size(100.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "SAFE Rescue",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold), // Montserrat/Inter Bold
            color = PrimaryBlue
        )
        Spacer(Modifier.height(8.dp))

        Text(
            text = "Accede a tu cuenta",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium), // Montserrat/Inter Medium
            color = Color.Gray
        )
        Spacer(Modifier.height(32.dp))

        // Campos de Entrada
        LoginTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email / Usuario",
            icon = Icons.Default.MailOutline,
            keyboardType = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(Modifier.height(16.dp))
        LoginTextField(
            value = password,
            onValueChange = { password = it },
            label = "Contraseña",
            icon = Icons.Default.Lock,
            visualTransformation = PasswordVisualTransformation(),
            keyboardType = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(Modifier.height(8.dp))

        // Checkbox y Olvidé Contraseña
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it }, // Funcionalidad del Checkbox
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                )
                Text("Recordarme", style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = { println("Botón Olvidé Contraseña presionado") }) {
                Text(
                    "¿Olvidaste tu contraseña?",
                    color = SecondaryRed,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
        Spacer(Modifier.height(32.dp))

        // Botón de Iniciar Sesión (Funcionalidad Clicable)
        Button(
            onClick = onLoginClicked, // Llama a la función definida en MainActivity
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text(
                "Iniciar Sesión",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                fontSize = 20.sp
            )
        }
    }
}

// ----------------------------------------------------------------------------------
// PREVIEW
// ----------------------------------------------------------------------------------

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun InicioSesionPreview() {
    MaterialTheme {
        InicioSesionScreen(onLoginClicked = {})
    }
}
