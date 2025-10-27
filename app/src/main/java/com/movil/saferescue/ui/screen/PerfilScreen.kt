package com.movil.saferescue.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.movil.saferescue.ui.components.ImagePickerDialog
import com.movil.saferescue.ui.viewmodel.ProfileViewModel
import com.movil.saferescue.ui.viewmodel.ProfileViewModelFactory

@Composable
fun ProfileScreen(
    factory: ProfileViewModelFactory
) {
    val vm: ProfileViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
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
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(enabled = state.isEditing) {
                                if (state.isEditing) vm.onImagePickerClick()
                            },
                        contentScale = ContentScale.Crop
                    )

                    IconButton(
                        onClick = {
                            if (state.isEditing) vm.cancelEdit() else vm.toggleEditMode()
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 12.dp, end = 12.dp)
                            .size(64.dp)
                    ) {
                        Icon(
                            imageVector = if (state.isEditing) Icons.Default.Close else Icons.Default.Edit,
                            contentDescription = if (state.isEditing) "Cancelar edición" else "Editar perfil",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(state.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(state.email, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(Modifier.height(24.dp))
                HorizontalDivider()

                // --- CAMPOS DE INFORMACIÓN UNIFICADOS ---
                ProfileTextField(label = "Rol", value = state.rol, onValueChange = {}, leadingIcon = Icons.Default.Work, isEditing = state.isEditing, isMutable = false)
                ProfileTextField(label = "Nombre Completo", value = state.name, onValueChange = vm::onNameChange, error = state.nameError, leadingIcon = Icons.Default.Person, isEditing = state.isEditing, isMutable = true)
                ProfileTextField(label = "Nombre de Usuario", value = state.username, onValueChange = {}, leadingIcon = Icons.Default.AlternateEmail, isEditing = state.isEditing, isMutable = false)
                ProfileTextField(label = "Teléfono", value = state.phone, onValueChange = vm::onPhoneChange, error = state.phoneError, leadingIcon = Icons.Default.Phone, keyboardType = KeyboardType.Phone, isEditing = state.isEditing, isMutable = true)

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    ProfileTextField(modifier = Modifier.weight(1f), label = "RUN", value = state.run, onValueChange = {}, isError = state.runAndDvError != null, keyboardType = KeyboardType.Number, isEditing = state.isEditing, isMutable = false)
                    Text("-", Modifier.padding(horizontal = 8.dp, vertical = 24.dp))
                    ProfileTextField(modifier = Modifier.width(70.dp), label = "DV", value = state.dv, onValueChange = {}, isError = state.runAndDvError != null, isEditing = state.isEditing, isMutable = false)
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

                Spacer(Modifier.height(24.dp))

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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (state.isImagePickerDialogVisible) {
        ImagePickerDialog(
            showDialog = true,
            onDismissRequest = { vm.onImagePickerDismiss() },
            onImageSelected = { uri -> vm.onImageUriSelected(uri) }
        )
    }
}

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditing: Boolean,
    isMutable: Boolean,
    modifier: Modifier = Modifier,
    error: String? = null,
    isError: Boolean = false,
    leadingIcon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val isEnabled = isEditing && isMutable

    // Define los colores para el estado "deshabilitado" (visualización) para que sea consistente
    val fieldColors = OutlinedTextFieldDefaults.colors(
        disabledTextColor = MaterialTheme.colorScheme.onSurface,
        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            enabled = isEnabled,
            readOnly = !isEnabled, // El campo es de solo lectura si no está habilitado
            singleLine = true,
            isError = error != null || isError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = fieldColors // Aplica los colores personalizados (afectan al estado deshabilitado)
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
