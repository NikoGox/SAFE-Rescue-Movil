package com.movil.saferescue.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movil.saferescue.R
import com.movil.saferescue.ui.viewmodel.ProfileViewModel
import com.movil.saferescue.ui.viewmodel.ProfileViewModelFactory

@Composable
fun ProfileScreen(factory: ProfileViewModelFactory) {
    val vm: ProfileViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsState()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botón para activar/desactivar modo edición
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = vm::toggleEditMode) {
                Icon(if (state.isEditing) Icons.Default.Close else Icons.Default.Edit, contentDescription = "Editar")
                Spacer(Modifier.width(4.dp))
                Text(if (state.isEditing) "Cancelar" else "Editar Perfil")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Imagen de Perfil
        Image(
            painter = painterResource(id = R.drawable.sr_logo), // Reemplazar con coil/glide para state.fotoUrl
            contentDescription = "Foto de Perfil",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(8.dp))
        Text(state.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(state.email, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(24.dp))
        Divider()
        Spacer(Modifier.height(16.dp))

        // --- CAMPOS DEL PERFIL ---

        // Rol (No editable)
        ProfileInfoRow(icon = Icons.Default.Work, label = "Rol", value = state.rol, enabled = false)

        // Nombre (Editable)
        ProfileTextField(label = "Nombre Completo", value = state.name, onValueChange = vm::onNameChange, enabled = state.isEditing)

        // Nombre de Usuario (Editable)
        ProfileTextField(label = "Nombre de Usuario", value = state.username, onValueChange = vm::onUsernameChange, enabled = state.isEditing)

        // Teléfono (Editable)
        ProfileTextField(label = "Teléfono", value = state.phone, onValueChange = vm::onPhoneChange, enabled = state.isEditing)

        // RUN y DV (Editable)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                ProfileTextField(label = "RUN", value = state.run, onValueChange = vm::onRunChange, enabled = state.isEditing)
            }
            Text("-", Modifier.padding(horizontal = 8.dp))
            Box(modifier = Modifier.width(70.dp)) {
                ProfileTextField(label = "DV", value = state.dv, onValueChange = vm::onDvChange, enabled = state.isEditing)
            }
        }

        // URL Foto (Editable)
        ProfileTextField(label = "URL de Foto", value = state.fotoUrl, onValueChange = vm::onFotoUrlChange, enabled = state.isEditing)

        Spacer(Modifier.height(24.dp))

        // Botón de Guardar
        if (state.isEditing) {
            Button(
                onClick = vm::saveChanges,
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if(state.isSubmitting) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                else Text("Guardar Cambios")
            }
        }

        // Mensajes de éxito o error
        state.successMsg?.let {
            Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
        }
        state.errorMsg?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

// Componente para campos de texto reutilizable
@Composable
private fun ProfileTextField(label: String, value: String, onValueChange: (String) -> Unit, enabled: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        enabled = enabled,
        singleLine = true
    )
}

// Componente para mostrar información no editable
@Composable
private fun ProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, enabled: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        enabled = enabled,
        readOnly = true
    )
}
