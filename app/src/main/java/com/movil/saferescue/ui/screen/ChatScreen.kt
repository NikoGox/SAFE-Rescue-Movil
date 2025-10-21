package com.movil.saferescue.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
// CAMBIO 1: Importar la nueva data class enriquecida
import com.movil.saferescue.data.local.notification.NotificationWithSender
import com.movil.saferescue.ui.viewmodel.NotificationViewModel
import com.movil.saferescue.ui.viewmodel.NotificationViewModelFactory

/**
 * El Composable de nivel superior que ahora recibe la NotificationViewModelFactory.
 * No necesita cambios.
 */
@Composable
fun ChatScreenVm(
    notificationViewModelFactory: NotificationViewModelFactory,
    onNavigateBack: () -> Unit
) {
    val viewModel: NotificationViewModel = viewModel(factory = notificationViewModelFactory)

    ChatScreen(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack
    )
}

/**
 * El Composable que define la estructura y la UI de la pantalla de Chat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreen(
    viewModel: NotificationViewModel,
    onNavigateBack: () -> Unit
) {
    // CAMBIO 2: El flujo ahora emite una lista de 'NotificationWithSender'
    val messages by viewModel.allChatMessages.collectAsStateWithLifecycle(initialValue = emptyList())
    val listState = rememberLazyListState()

    // CAMBIO 3: Simular el ID del usuario actual (debe coincidir con el del ViewModel)
    // En una app real, este ID vendría del estado de la sesión.
    val currentUserId = 3L

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat de Soporte") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            MessageInput(
                onSendMessage = { text ->
                    // No hay cambios aquí, el ViewModel se encarga de todo.
                    viewModel.sendMessage(text)
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // CAMBIO 4: Iterar sobre la lista de 'NotificationWithSender'
            items(
                items = messages,
                key = { messageWithSender -> messageWithSender.notification.id } // La key es el ID de la notificación
            ) { messageWithSender ->
                MessageBubble(
                    message = messageWithSender,
                    // Pasamos si el mensaje es del usuario actual, comparando IDs
                    isFromCurrentUser = messageWithSender.notification.remitente_id == currentUserId
                )
            }
        }
    }
}

/**
 * El Composable que representa una "burbuja" de mensaje individual.
 */
@Composable
private fun MessageBubble(
    // CAMBIO 5: La burbuja ahora recibe la data class enriquecida y un booleano
    message: NotificationWithSender,
    isFromCurrentUser: Boolean
) {
    val alignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
    val backgroundColor = if (isFromCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isFromCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    // Usamos una Columna para alinear el nombre del remitente y el mensaje
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        // CAMBIO 6: Mostrar el nombre del remitente si NO es el usuario actual
        if (!isFromCurrentUser) {
            Text(
                text = message.senderName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
            )
        }

        Box(
            modifier = Modifier
                // El ancho de la burbuja es relativo al ancho de la pantalla
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.notification.mensaje, // El texto del mensaje está dentro del objeto notificación
                color = textColor
            )
        }
    }
}


/**
 * El Composable para la barra de entrada de texto.
 * No necesita ningún cambio.
 */
@Composable
private fun MessageInput(onSendMessage: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Escribe un mensaje...") },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (text.isNotBlank()) {
                        onSendMessage(text)
                        text = ""
                        keyboardController?.hide()
                    }
                }
            )
        )

        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onSendMessage(text)
                    text = ""
                    keyboardController?.hide()
                }
            },
            enabled = text.isNotBlank()
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Enviar",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
