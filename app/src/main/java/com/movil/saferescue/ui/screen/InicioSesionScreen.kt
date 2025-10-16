package com.movil.saferescue.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movil.saferescue.R // Asegúrate de tener tu logo en res/drawable
import com.movil.saferescue.ui.theme.PrimaryBlue // Colores personalizados
import com.movil.saferescue.ui.theme.SecondaryRed
import com.movil.saferescue.ui.viewmodel.AuthViewModel
import com.movil.saferescue.ui.viewmodel.AuthViewModelFactory

/**
 * Contenedor del ViewModel (Wrapper).
 * Mantiene la lógica de estado y navegación.
 */
@Composable
fun LoginScreenVm(
    onLoginOkNavigateHome: () -> Unit,
    onGoRegister: () -> Unit,
    factory: AuthViewModelFactory
) {
    val vm: AuthViewModel = viewModel(factory = factory)
    val state by vm.login.collectAsStateWithLifecycle()

    // Navegación automática en caso de éxito
    LaunchedEffect(state.success) {
        // Si el estado de éxito es `true` (y solo entonces)...
        if (state.success) {
            // ...ejecutamos la navegación.
            onLoginOkNavigateHome()
            // Opcionalmente, puedes limpiar el estado aquí si es necesario para evitar
            // que se vuelva a ejecutar si la pantalla se recompone.
            // vm.clearLoginResult()
        }
    }

    // Delegamos a la UI presentacional, pasando todos los estados y eventos
    LoginScreen(
        identifier = state.identifier,
        pass = state.pass,
        identifierError = state.identifierError,
        passError = state.passError, // <-- 2. PASAMOS EL passError DESDE EL ESTADO
        canSubmit = state.canSubmit,
        isSubmitting = state.isSubmitting,
        errorMsg = state.errorMsg,
        onIdentifierChange = vm::onLoginIdentifierChange,
        onPassChange = vm::onLoginPassChange,
        onSubmit = vm::submitLogin,
        onGoRegister = onGoRegister
    )
}

/**
 * Pantalla de UI (Presentational).
 * Se enfoca únicamente en cómo se ve la pantalla.
 */
@Composable
private fun LoginScreen(
    identifier: String,
    pass: String,
    identifierError: String?,
    passError: String?, // <-- 1. AÑADIMOS passError A LA FIRMA DE LA FUNCIÓN
    canSubmit: Boolean,
    isSubmitting: Boolean,
    errorMsg: String?,
    onIdentifierChange: (String) -> Unit,
    onPassChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoRegister: () -> Unit
) {
    // --- NUEVO DISEÑO VISUAL ---
    var showPass by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Bienvenido a",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.Black
        )
        Spacer(Modifier.height(16.dp))

        // 1. Logo
        Image(
            painter = painterResource(id = R.drawable.sr_logo), // Reemplaza con tu logo
            contentDescription = "Logo",
            modifier = Modifier.size(150.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "SAFE Rescue",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold), // Montserrat/Inter Bold
            color = PrimaryBlue
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Accede a tu cuenta",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium), // Montserrat/Inter Medium
            color = Color.Gray
        )

        Spacer(Modifier.height(48.dp))

        // 2. Campo de Identificador (Email o Usuario)
        OutlinedTextField(
            value = identifier,
            onValueChange = onIdentifierChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email o Usuario") },
            leadingIcon = { Icon(Icons.Default.MailOutline, contentDescription = null) },
            isError = identifierError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )
        AnimatedVisibility(
            visible = identifierError != null,
            enter = fadeIn() + expandVertically(),
            modifier = Modifier.fillMaxWidth(),
            exit = fadeOut() + shrinkVertically()
        ) {
            if (identifierError != null) {
                Text(
                    text = identifierError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 3. Campo de Contraseña
        OutlinedTextField(
            value = pass,
            onValueChange = onPassChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Contraseña") },
            // isError AHORA FUNCIONA CORRECTAMENTE
            isError = passError != null,
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { showPass = !showPass }) {
                    Icon(
                        imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (showPass) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )
        AnimatedVisibility(
            visible = passError != null,
            enter = fadeIn() + expandVertically(),
            modifier = Modifier.fillMaxWidth(),
            exit = fadeOut() + shrinkVertically()
        ) {
            if (passError != null) {
                Text(
                    text = passError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 4. "Recordarme" y "Olvidé Contraseña"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                )
                Text("Recordarme", style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = { /* TODO: Lógica para "Olvidé Contraseña" */ }) {
                Text(
                    "¿Olvidaste tu contraseña?",
                    color = SecondaryRed,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
        Spacer(Modifier.height(32.dp))

        // 5. Botón de Iniciar Sesión
        Button(
            onClick = onSubmit,
            enabled = canSubmit && !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    "Iniciar Sesión",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    fontSize = 20.sp
                )
            }
        }

        // 6. Mensaje de Error global
        if (errorMsg != null) {
            Text(
                text = errorMsg,
                color = SecondaryRed,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // 7. Navegación a Registro
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("¿No tienes cuenta?")
            TextButton(onClick = onGoRegister) {
                Text("Regístrate", color = PrimaryBlue, fontWeight = FontWeight.Bold)
            }
        }
    }
}
