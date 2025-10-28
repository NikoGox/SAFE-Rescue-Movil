package com.movil.saferescue.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
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
import com.movil.saferescue.data.local.mensaje.MensajeConRemitente
import com.movil.saferescue.ui.viewmodel.MensajeViewModel
import com.movil.saferescue.ui.viewmodel.MensajeViewModelFactory

@Composable
fun ChatScreenVm(
    mensajeViewModelFactory: MensajeViewModelFactory,
    onNavigateBack: () -> Unit
) {
    val viewModel: MensajeViewModel = viewModel(factory = mensajeViewModelFactory)
    ChatScreen(viewModel = viewModel)
}

@Composable
private fun ChatScreen(
    viewModel: MensajeViewModel
) {
    val mensajes by viewModel.todosLosMensajesDeChat.collectAsStateWithLifecycle(initialValue = emptyList())
    val listState = rememberLazyListState()
    val currentUserId = 3L

    LaunchedEffect(mensajes) {
        if (mensajes.isNotEmpty()) {
            listState.animateScrollToItem(mensajes.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f) // Ocupa todo el espacio disponible
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = mensajes,
                key = { mensajeConRemitente -> mensajeConRemitente.mensaje.id }
            ) { mensajeConRemitente ->
                MessageBubble(
                    mensaje = mensajeConRemitente,
                    isFromCurrentUser = mensajeConRemitente.mensaje.remitente_id == currentUserId
                )
            }
        }
        MessageInput(
            onSendMessage = { text ->
                viewModel.enviarMensajeDeChat(text)
            }
        )
    }
}

@Composable
private fun MessageBubble(
    mensaje: MensajeConRemitente,
    isFromCurrentUser: Boolean
) {
    val alignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
    val bubbleShape = if (isFromCurrentUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }
    val backgroundColor = if (isFromCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isFromCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (!isFromCurrentUser) {
            Text(
                text = mensaje.nombreRemitente,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
            )
        }

        Box(
            modifier = Modifier
                .clip(bubbleShape)
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = mensaje.mensaje.mensaje,
                color = textColor
            )
        }
    }
}

@Composable
private fun MessageInput(onSendMessage: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
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
