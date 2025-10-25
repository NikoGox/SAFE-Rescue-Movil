package com.movil.saferescue.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movil.saferescue.ui.viewmodel.MensajeViewModel
import com.movil.saferescue.ui.viewmodel.MensajeViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNotificationScreen(
    mensajeViewModelFactory: MensajeViewModelFactory,
    onNavigateBack: () -> Unit
) {
    val viewModel: MensajeViewModel = viewModel(factory = mensajeViewModelFactory)
    var detail by remember { mutableStateOf("") }

    val alertTypes = listOf(
        "Alerta Ciudadana",
        "Alerta de Catástrofe",
        "Alerta de Incendio",
        "Alerta de Manifestaciones",
        "Alerta de Accidente",
        "Otro"
    )
    var selectedAlertType by remember { mutableStateOf(alertTypes[0]) }
    var customAlertType by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crear Nueva Notificación Global", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        // Selector de tipo de alerta
        ExposedDropdownMenuBox(
            expanded = isDropdownExpanded,
            onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedAlertType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de Alerta") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                alertTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            selectedAlertType = type
                            isDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Campo para tipo de alerta personalizado
        if (selectedAlertType == "Otro") {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customAlertType,
                onValueChange = { customAlertType = it },
                label = { Text("Especifique el tipo de alerta") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de detalle
        OutlinedTextField(
            value = detail,
            onValueChange = { detail = it },
            label = { Text("Detalle de la Notificación") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val title = if (selectedAlertType == "Otro") customAlertType else selectedAlertType
                viewModel.createGlobalNotification(title, detail)
                onNavigateBack()
            },
            enabled = detail.isNotBlank() && (selectedAlertType != "Otro" || customAlertType.isNotBlank()),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Crear y Enviar")
        }
    }
}
