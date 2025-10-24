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
import com.movil.saferescue.R
import com.movil.saferescue.ui.theme.PrimaryBlue
import com.movil.saferescue.ui.theme.SecondaryRed
import com.movil.saferescue.ui.theme.uno_tres
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
    factory: AuthViewModelFactory,
    // Permitimos pasar la instancia desde NavGraph para garantizar un único ViewModel compartido
    authViewModelFromParent: AuthViewModel? = null
) {
    val vm: AuthViewModel = authViewModelFromParent ?: viewModel(factory = factory)
    val state by vm.login.collectAsStateWithLifecycle()
    val isAuthenticated by vm.isAuthenticated.collectAsStateWithLifecycle()
    var rememberMe by remember { mutableStateOf(false) }

    // Navegación automática solo cuando isAuthenticated cambia a true
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated == true) {
            onLoginOkNavigateHome()
        }
    }

    // Delegamos a la UI presentacional
    LoginScreen(
        identifier = state.identifier,
        pass = state.pass,
        identifierError = state.identifierError,
        passError = state.passError,
        canSubmit = state.canSubmit,
        isSubmitting = state.isSubmitting,
        errorMsg = state.errorMsg,
        rememberMe = rememberMe,
        onIdentifierChange = vm::onLoginIdentifierChange,
        onPassChange = vm::onLoginPassChange,
        onRememberMeChange = { rememberMe = it },
        onSubmit = { vm.submitLogin(rememberMe) },
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
    passError: String?,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    errorMsg: String?,
    rememberMe: Boolean,
    onIdentifierChange: (String) -> Unit,
    onPassChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onGoRegister: () -> Unit
) {
    var showPass by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            text = "Bienvenido a ",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
            color = Color.DarkGray
        )
        Spacer(Modifier.height(16.dp))

        // 1. Logo
        Image(
            painter = painterResource(id = R.drawable.sr_logo),
            contentDescription = "Logo",
            modifier = Modifier.size(150.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "SAFE Rescue",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), // Montserrat/Inter Bold
            color = uno_tres

        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Accede a tu cuenta",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium), // Montserrat/Inter Medium
            color = Color.DarkGray
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = onRememberMeChange,
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

        if (errorMsg != null) {
            Text(
                text = errorMsg,
                color = SecondaryRed,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

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

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Versión 1.6",
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
