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
// 1. IMPORTACIONES ACTUALIZADAS
import com.movil.saferescue.data.local.mensaje.MensajeConRemitente
import com.movil.saferescue.ui.viewmodel.MensajeViewModel
import com.movil.saferescue.ui.viewmodel.MensajeViewModelFactory

/**
 * El Composable de nivel superior que ahora recibe el MensajeViewModelFactory.
 */
@Composable
fun ChatScreenVm(
    // 2. PARÁMETRO DEL FACTORY ACTUALIZADO
    mensajeViewModelFactory: MensajeViewModelFactory,
    onNavigateBack: () -> Unit
) {
    // 3. SE CREA UNA INSTANCIA DEL VIEWMODEL CORRECTO
    val viewModel: MensajeViewModel = viewModel(factory = mensajeViewModelFactory)

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
    // 4. EL VIEWMODEL AHORA ES MensajeViewModel
    viewModel: MensajeViewModel,
    onNavigateBack: () -> Unit
) {
    // 5. EL FLUJO AHORA EMITE UNA LISTA DE 'MensajeConRemitente'
    val mensajes by viewModel.todosLosMensajesDeChat.collectAsStateWithLifecycle(initialValue = emptyList())
    val listState = rememberLazyListState()

    // El ID del usuario actual para diferenciar las burbujas del chat
    val currentUserId = 3L

    LaunchedEffect(mensajes) {
        if (mensajes.isNotEmpty()) {
            listState.animateScrollToItem(mensajes.lastIndex)
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
                    // 6. SE LLAMA A LA FUNCIÓN CORRECTA DEL VIEWMODEL
                    viewModel.enviarMensajeDeChat(text)
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
            // 7. SE ITERA SOBRE LA LISTA DE 'MensajeConRemitente'
            items(
                items = mensajes,
                // La key ahora se extrae de la propiedad 'mensaje'
                key = { mensajeConRemitente -> mensajeConRemitente.mensaje.id }
            ) { mensajeConRemitente ->
                MessageBubble(
                    mensaje = mensajeConRemitente,
                    // Se compara el ID del remitente desde la propiedad 'mensaje'
                    isFromCurrentUser = mensajeConRemitente.mensaje.remitente_id == currentUserId
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
    // 8. LA BURBUJA AHORA RECIBE LA DATA CLASS 'MensajeConRemitente'
    mensaje: MensajeConRemitente,
    isFromCurrentUser: Boolean
) {
    val alignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
    val backgroundColor = if (isFromCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isFromCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (!isFromCurrentUser) {
            Text(
                // 9. SE USA LA PROPIEDAD 'nombreRemitente'
                text = mensaje.nombreRemitente,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                // 10. EL TEXTO DEL MENSAJE ESTÁ DENTRO DEL OBJETO 'mensaje'
                text = mensaje.mensaje.mensaje,
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
