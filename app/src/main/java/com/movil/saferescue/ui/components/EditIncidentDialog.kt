package com.movil.saferescue.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movil.saferescue.ui.theme.SRBackgroundWhite
import com.movil.saferescue.ui.viewmodel.IncidenteViewModel

@Composable
fun EditIncidentDialog(
    viewModel: IncidenteViewModel,
    onDismiss: () -> Unit,
    isAdmin: Boolean
) {
    val editState by viewModel.editState.collectAsStateWithLifecycle()
    val incident = editState.selectedIncident?.incident ?: return
    var titulo by remember { mutableStateOf(incident.titulo) }
    var detalle by remember { mutableStateOf(incident.detalle) }
    var bomberoUsername by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageDialog by remember { mutableStateOf(false) }

    if (showImageDialog) {
        ImagePickerDialog(
            showDialog = true,
            onDismissRequest = { showImageDialog = false },
            onImageSelected = { uri ->
                imageUri = uri
                showImageDialog = false
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SRBackgroundWhite)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Editar Incidente", style = MaterialTheme.typography.headlineSmall)
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = detalle,
                    onValueChange = { detalle = it },
                    label = { Text("Detalle") },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )

                if (isAdmin) {
                    OutlinedTextField(
                        value = bomberoUsername,
                        onValueChange = { bomberoUsername = it },
                        label = { Text("Asignar a Bombero (username)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(onClick = { showImageDialog = true }) {
                    Text(if (imageUri == null) "Cambiar Imagen" else "Imagen Seleccionada ✓")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.onSaveChangesClicked(titulo, detalle, imageUri, bomberoUsername.takeIf { it.isNotBlank() })
                        },
                        enabled = titulo.isNotBlank() && detalle.isNotBlank()
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}