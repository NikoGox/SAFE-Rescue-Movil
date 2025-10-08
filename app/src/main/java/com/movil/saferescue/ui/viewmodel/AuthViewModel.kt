package com.movil.saferescue.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.saferescue.navigation.NavigationEvent
import com.movil.saferescue.navigation.Route
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ----------------------------------------------------------------------------------
// DATA CLASS DE ESTADO (UI State)
// ----------------------------------------------------------------------------------

data class LoginUiState(
    val email: String = "",
    val pass: String = "",
    val isSubmitting: Boolean = false, // Flag de carga/bloqueo del botón
    val success: Boolean = false,
    val errorMsg: String? = null // Error general (usuario o clave incorrecta)
)

// ----------------------------------------------------------------------------------
// VIEWMODEL
// ----------------------------------------------------------------------------------

class AuthViewModel : ViewModel() {

    // --- ESTADOS (LO QUE LA UI LEE Y MUESTRA) ---
    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    // --- EVENTOS (CANAL PARA ACCIONES ÚNICAS COMO NAVEGACIÓN) ---
    private val _navigationEvents = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvents.receiveAsFlow()


    // --- LÓGICA DE NEGOCIO ---

    fun onEmailChanged(email: String) {
        // Actualiza el estado del email
        _loginState.update { it.copy(email = email) }
    }

    fun onPasswordChanged(pass: String) {
        // Actualiza el estado de la contraseña
        _loginState.update { it.copy(pass = pass) }
    }

    /**
     * Procesa la solicitud de inicio de sesión y emite un evento de navegación.
     */
    fun onLoginSubmit() {
        viewModelScope.launch {
            _loginState.update { it.copy(isSubmitting = true, errorMsg = null) }

            // 1. Simular la verificación de credenciales (reemplazar con lógica real de API/DB)
            delay(1500)

            // 2. Lógica de verificación (DEMO: Exitoso si los campos tienen datos)
            val loginSuccess = _loginState.value.email.isNotBlank() && _loginState.value.pass.length >= 4

            if (loginSuccess) {
                // 3. Emitir el evento de navegación a Home
                _navigationEvents.send(
                    NavigationEvent.NavigateTo(
                        route = Route.Home,
                        popUpToRoute = Route.Login,
                        inclusive = true // Limpia el Login de la pila para que no se pueda volver
                    )
                )

                _loginState.update { it.copy(isSubmitting = false, success = true) }
            } else {
                // 4. Mostrar error si falla
                _loginState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMsg = "Verifique sus credenciales."
                    )
                }
            }
        }
    }

    /**
     * Lógica para navegar a la pantalla de Registro.
     */
    fun onGoRegisterClicked() {
        viewModelScope.launch {
            _navigationEvents.send(NavigationEvent.NavigateTo(Route.Register))
        }
    }
}
