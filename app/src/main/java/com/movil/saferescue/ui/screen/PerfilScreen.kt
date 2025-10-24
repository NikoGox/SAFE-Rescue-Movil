package com.movil.saferescue.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.movil.saferescue.R
import com.movil.saferescue.ui.viewmodel.ProfileViewModel
import com.movil.saferescue.ui.viewmodel.ProfileViewModelFactory

@Composable
fun ProfileScreen(
    factory: ProfileViewModelFactory
) {
    val vm: ProfileViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Efecto para mostrar mensajes de éxito o error como Snackbars
    LaunchedEffect(state.successMsg, state.errorMsg) {
        state.successMsg?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearMessages()
        }
        state.errorMsg?.let {
            snackbarHostState.showSnackbar(it, withDismissAction = true)
            vm.clearMessages()
        }
    }

    // <<< CORRECCIÓN: Se elimina el Scaffold y se usa un Box como contenedor principal >>>
    // Esto permite que el Snackbar flote por encima del contenido.
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            // El contenido principal de la pantalla va dentro de una columna que puede hacer scroll
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()) // Permite el scroll vertical
                    .padding(horizontal = 16.dp), // Mantenemos el padding horizontal
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Botón para activar/desactivar modo edición
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = {
                            if (state.isEditing) {
                                vm.cancelEdit()
                            } else {
                                vm.toggleEditMode()
                            }
                        }
                    ) {
                        Icon(if (state.isEditing) Icons.Default.Close else Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(if (state.isEditing) "Cancelar" else "Editar")
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 2. Imagen de Perfil
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(state.fotoUrl)
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(id = R.drawable.perfil_default),
                    error = painterResource(id = R.drawable.perfil_default),
                    contentDescription = "Foto de Perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.height(8.dp))
                Text(state.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(state.email, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(Modifier.height(24.dp))
                Divider()

                // --- CAMPOS DE INFORMACIÓN ---
                ProfileInfoRow(icon = Icons.Default.Work, label = "Rol", value = state.rol)
                ProfileTextField(label = "Nombre Completo", value = state.name, onValueChange = vm::onNameChange, enabled = state.isEditing, error = state.nameError, leadingIcon = Icons.Default.Person)
                ProfileTextField(label = "Nombre de Usuario", value = state.username, onValueChange = vm::onUsernameChange, enabled = state.isEditing, error = state.usernameError, leadingIcon = Icons.Default.AlternateEmail)
                ProfileTextField(label = "Teléfono", value = state.phone, onValueChange = vm::onPhoneChange, enabled = state.isEditing, error = state.phoneError, leadingIcon = Icons.Default.Phone, keyboardType = KeyboardType.Phone)

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    ProfileTextField(modifier = Modifier.weight(1f), label = "RUN", value = state.run, onValueChange = vm::onRunChange, enabled = state.isEditing, isError = state.runAndDvError != null, keyboardType = KeyboardType.Number)
                    Text("-", Modifier.padding(horizontal = 8.dp, vertical = 24.dp))
                    ProfileTextField(modifier = Modifier.width(70.dp), label = "DV", value = state.dv, onValueChange = vm::onDvChange, enabled = state.isEditing, isError = state.runAndDvError != null)
                }

                AnimatedVisibility(visible = state.runAndDvError != null) {
                    Text(
                        text = state.runAndDvError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp)
                    )
                }

                ProfileInfoRow(icon = Icons.Default.Image, label = "URL de Foto", value = state.fotoUrl)

                Spacer(Modifier.height(24.dp))

                // Botón de Guardar
                AnimatedVisibility(visible = state.isEditing) {
                    Button(
                        onClick = vm::saveChanges,
                        enabled = !state.isSubmitting && state.canSave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (state.isSubmitting) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("Guardar Cambios")
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        // <<< CORRECCIÓN: El SnackbarHost se coloca en el Box para que flote sobre todo lo demás >>>
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter) // Se alinea en la parte inferior central
        )
    }
}

// Los componentes ProfileTextField y ProfileInfoRow no necesitan cambios.
// ... (El resto de tu código para ProfileTextField y ProfileInfoRow va aquí)

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    error: String? = null,
    isError: Boolean = false, // Para control manual
    leadingIcon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            enabled = enabled,
            singleLine = true,
            isError = error != null || isError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
        AnimatedVisibility(visible = error != null) {
            Text(
                text = error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        enabled = false,
        readOnly = true,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    )
}
