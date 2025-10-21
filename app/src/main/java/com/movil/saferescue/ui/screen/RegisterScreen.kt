package com.movil.saferescue.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.movil.saferescue.R // Asegúrate de tener tu logo
import com.movil.saferescue.ui.theme.PrimaryBlue
import com.movil.saferescue.ui.viewmodel.AuthViewModel
import com.movil.saferescue.ui.viewmodel.AuthViewModelFactory

/**
 * Contenedor del ViewModel (Wrapper). Mantiene la lógica de estado y navegación.
 * Esta parte NO cambia, sigue conectando el ViewModel con la UI.
 */
@Composable
fun RegisterScreenVm(
    onRegisteredNavigateLogin: () -> Unit,
    onGoLogin: () -> Unit,
    factory: AuthViewModelFactory
) {
    val vm: AuthViewModel = viewModel(factory = factory)
    val state by vm.register.collectAsStateWithLifecycle()

    if (state.success) {
        LaunchedEffect(Unit) { // Usamos LaunchedEffect para navegación controlada
            vm.clearRegisterResult()
            onRegisteredNavigateLogin()
        }
    }

    RegisterScreen(
        // Pasamos todos los estados y eventos a la pantalla de UI
        name = state.name,
        email = state.email,
        phone = state.phone,
        pass = state.pass,
        confirm = state.confirm,
        username = state.username,
        run = state.run,
        dv = state.dv,
        fotoUrl = state.fotoUrl,

        nameError = state.nameError,
        emailError = state.emailError,
        phoneError = state.phoneError,
        passError = state.passError,
        confirmError = state.confirmError,
        runError = state.runError,
        dvError = state.dvError,
        usernameError = state.usernameError,
        fotoUrlError = state.fotoUrlError,

        canSubmit = state.canSubmit,
        isSubmitting = state.isSubmitting,
        errorMsg = state.errorMsg,

        onNameChange = vm::onNameChange,
        onEmailChange = vm::onRegisterEmailChange,
        onPhoneChange = vm::onPhoneChange,
        onPassChange = vm::onRegisterPassChange,
        onConfirmChange = vm::onConfirmChange,
        onUsernameChange = vm::onUsernameChange,
        onRunChange = vm::onRunChange,
        onDvChange = vm::onDvChange,
        onFotoUrlChange = vm::onFotoUrlChange,

        onSubmit = vm::submitRegister,
        onGoLogin = onGoLogin
    )
}


/**
 * Pantalla de UI (Presentational).
 * Rediseñada completamente para coincidir con el estilo del Login.
 */
@Composable
private fun RegisterScreen(
    name: String, email: String, phone: String, pass: String, confirm: String,
    username: String, run: String, dv: String, fotoUrl: String,
    nameError: String?, emailError: String?, phoneError: String?, passError: String?, confirmError: String?,
    usernameError: String?, runError: String?, dvError: String?, fotoUrlError: String?,
    canSubmit: Boolean, isSubmitting: Boolean, errorMsg: String?,
    onNameChange: (String) -> Unit, onEmailChange: (String) -> Unit, onPhoneChange: (String) -> Unit,
    onPassChange: (String) -> Unit, onConfirmChange: (String) -> Unit, onUsernameChange: (String) -> Unit,
    onRunChange: (String) -> Unit, onDvChange: (String) -> Unit, onFotoUrlChange: (String) -> Unit,
    onSubmit: () -> Unit, onGoLogin: () -> Unit
) {
    // --- NUEVO DISEÑO VISUAL PARA REGISTRO ---
    var showPass by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 26.dp)
            // Hacemos la columna deslizable para que quepa en todas las pantallas
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        // 1. Logo
        Image(
            painter = painterResource(id = R.drawable.sr_logo),
            contentDescription = "Logo",
            modifier = Modifier.size(120.dp) // Un poco más pequeño para dar espacio
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "SAFE Rescue",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold), // Montserrat/Inter Bold
            color = PrimaryBlue
        )

        Spacer(Modifier.height(10.dp))

        Text(
            "Crear una Cuenta",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium), // Montserrat/Inter Medium
            color = Color.Gray

        )

        Spacer(Modifier.height(24.dp))


        // --- CAMPOS DEL FORMULARIO ---

        // Nombre
        OutlinedTextField(value = name, onValueChange = onNameChange, modifier = Modifier.fillMaxWidth(), label = { Text("Nombre Completo") }, leadingIcon = { Icon(Icons.Default.Person, null) }, isError = nameError != null, singleLine = true)
        if (nameError != null) { Text(nameError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) }
        Spacer(Modifier.height(8.dp))

        // Email
        OutlinedTextField(value = email, onValueChange = onEmailChange, modifier = Modifier.fillMaxWidth(), label = { Text("Email") }, leadingIcon = { Icon(Icons.Default.MailOutline, null) }, isError = emailError != null, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true)
        if (emailError != null) { Text(emailError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) }
        Spacer(Modifier.height(8.dp))

        // Username
        OutlinedTextField(value = username, onValueChange = onUsernameChange, modifier = Modifier.fillMaxWidth(), label = { Text("Nombre de Usuario") }, leadingIcon = { Icon(Icons.Default.AlternateEmail, null) }, isError = usernameError != null, singleLine = true)
        if (usernameError != null) { Text(usernameError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) }
        Spacer(Modifier.height(8.dp))

        // Teléfono
        OutlinedTextField(value = phone, onValueChange = onPhoneChange, modifier = Modifier.fillMaxWidth(), label = { Text("Teléfono") }, leadingIcon = { Icon(Icons.Default.Phone, null) }, isError = phoneError != null, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
        if (phoneError != null) { Text(phoneError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) }
        Spacer(Modifier.height(8.dp))

        // RUN y DV en una fila
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            // RUN
            Column(Modifier.weight(0.75f)) {
                OutlinedTextField(value = run, onValueChange = onRunChange, modifier = Modifier.fillMaxWidth(), label = { Text("RUN") }, leadingIcon = { Icon(Icons.Default.CreditCard, null) }, isError = runError != null, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                if (runError != null) { Text(runError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) }
            }
            Spacer(Modifier.width(8.dp))
            Text("-", Modifier.padding(top = 18.dp), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.width(8.dp))
            // DV
            Column(Modifier.weight(0.25f)) {
                OutlinedTextField(value = dv, onValueChange = onDvChange, modifier = Modifier.fillMaxWidth(), label = { Text("DV") }, isError = dvError != null, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text), singleLine = true)
                if (dvError != null) { Text(dvError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) }
            }
        }
        Spacer(Modifier.height(8.dp))

        // URL de la Foto
        OutlinedTextField(value = fotoUrl, onValueChange = onFotoUrlChange, modifier = Modifier.fillMaxWidth(), label = { Text("URL de la Foto de Perfil (opcional)") }, leadingIcon = { Icon(Icons.Default.Link, null) }, isError = fotoUrlError != null, singleLine = true)
        if (fotoUrlError != null) { Text(fotoUrlError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) }
        Spacer(Modifier.height(8.dp))

        // Contraseña
        OutlinedTextField(value = pass, onValueChange = onPassChange, modifier = Modifier.fillMaxWidth(), label = { Text("Contraseña") }, leadingIcon = { Icon(Icons.Default.Lock, null) }, isError = passError != null, visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { showPass = !showPass }) { Icon(if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null) } }, singleLine = true)
        if (passError != null) { Text(passError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) }
        Spacer(Modifier.height(8.dp))

        // Confirmar Contraseña
        OutlinedTextField(value = confirm, onValueChange = onConfirmChange, modifier = Modifier.fillMaxWidth(), label = { Text("Confirmar Contraseña") }, leadingIcon = { Icon(Icons.Default.Lock, null) }, isError = confirmError != null, visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { showConfirm = !showConfirm }) { Icon(if (showConfirm) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null) } }, singleLine = true)
        if (confirmError != null) { Text(confirmError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) }
        Spacer(Modifier.height(32.dp))

        // Botón Registrarse
        Button(
            onClick = onSubmit,
            enabled = canSubmit && !isSubmitting,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    "Registrarse",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    fontSize = 20.sp
                )
            }
        }

        // Mensaje de error global
        if (errorMsg != null) {
            Text(text = errorMsg, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        // Navegación a Login
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("¿Ya tienes una cuenta?")
            TextButton(onClick = onGoLogin) {
                Text("Inicia Sesión", color = PrimaryBlue, fontWeight = FontWeight.Bold)
            }
        }
    }
}
