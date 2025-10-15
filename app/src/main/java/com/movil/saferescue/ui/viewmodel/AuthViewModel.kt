package com.movil.saferescue.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.saferescue.data.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.movil.saferescue.domain.validation.*
import kotlinx.coroutines.flow.asStateFlow

// ----------------- ESTADOS DE UI (observable con StateFlow) -----------------

data class LoginUiState(
    val identifier: String = "",
    val pass: String = "",
    val identifierError: String? = null,
    val passError: String? = null,
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null
)

// --- CORRECCIÓN 1: Simplifiqué el estado de registro para que coincida con el código ---
// He eliminado 'rolIdError' y 'runDvError' que no se usaban o eran incorrectos.
data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val pass: String = "",
    val confirm: String = "",
    val run: String = "",
    val dv: String = "",
    val username: String = "",
    val fotoUrl: String = "",
    val rolId: Long = 2L, // Asumimos rol por defecto 2 = Bombero

    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passError: String? = null,
    val confirmError: String? = null,
    val runError: String? = null,
    val dvError: String? = null,
    val usernameError: String? = null,
    val fotoUrlError: String? = null,

    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null
)

class AuthViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register.asStateFlow()


    // ----------------- LOGIN: handlers y envío (Sin cambios) -----------------
    fun onLoginIdentifierChange(value: String) {
        val error = if (value.isBlank()) "El campo es obligatorio" else null
        _login.update { it.copy(identifier = value, identifierError = error) }
        recomputeLoginCanSubmit()
    }

    fun onLoginPassChange(value: String) {
        _login.update { it.copy(pass = value) }
        recomputeLoginCanSubmit()
    }

    private fun recomputeLoginCanSubmit() {
        val s = _login.value
        val can = s.identifierError == null &&
                s.identifier.isNotBlank() &&
                s.pass.isNotBlank()
        _login.update { it.copy(canSubmit = can) }
    }

    fun submitLogin() {
        val s = _login.value
        if (!s.canSubmit || s.isSubmitting) return
        viewModelScope.launch {
            _login.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }
            delay(500)

            val result = repository.login(s.identifier, s.pass)
            _login.update {
                if (result.isSuccess) {
                    it.copy(isSubmitting = false, success = true, errorMsg = null)
                } else {
                    it.copy(
                        isSubmitting = false, success = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "Error de autenticación"
                    )
                }
            }
        }
    }

    fun clearLoginResult() {
        _login.update { it.copy(success = false, errorMsg = null) }
    }


    // ----------------- REGISTRO: handlers y envío -----------------

    // --- CORRECCIÓN 2: Eliminadas todas las funciones handler duplicadas y unificadas ---
    // Cada función ahora sigue el mismo patrón: recibe un valor, lo valida y llama a recomputeRegisterCanSubmit()

    fun onNameChange(value: String) {
        val filtered = value.filter { it.isLetter() || it.isWhitespace() }
        _register.update {
            it.copy(name = filtered, nameError = validateNameLettersOnly(filtered))
        }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterEmailChange(value: String) {
        _register.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeRegisterCanSubmit()
    }

    fun onPhoneChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _register.update {
            it.copy(phone = digitsOnly, phoneError = validatePhoneDigitsOnly(digitsOnly))
        }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPassChange(value: String) {
        _register.update { it.copy(pass = value, passError = validateStrongPassword(value)) }
        // También re-validamos la confirmación de contraseña cada vez que la contraseña principal cambia
        _register.update { it.copy(confirmError = validateConfirm(it.pass, it.confirm)) }
        recomputeRegisterCanSubmit()
    }

    fun onConfirmChange(value: String) {
        _register.update { it.copy(confirm = value, confirmError = validateConfirm(it.pass, value)) }
        recomputeRegisterCanSubmit()
    }

    fun onUsernameChange(value: String) {
        _register.update {
            it.copy(username = value, usernameError = validateUsername(value))
        }
        recomputeRegisterCanSubmit()
    }

    // --- CORRECCIÓN 3: Lógica de validación del RUN separada en dos handlers ---
    fun onRunChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        // La validación del RUN completo se hace en una función externa `validateChileanRUN`,
        // que devuelve un solo String de error. Lo asignamos a `runError`.
        val runError = validateChileanRUN(digitsOnly, _register.value.dv)
        _register.update {
            it.copy(run = digitsOnly, runError = runError, dvError = runError)
        }
        recomputeRegisterCanSubmit()
    }

    fun onDvChange(value: String) {
        val upperValue = value.uppercase().filter { it.isLetterOrDigit() }
        val runError = validateChileanRUN(_register.value.run, upperValue)
        _register.update {
            it.copy(dv = upperValue, runError = runError, dvError = runError)
        }
        recomputeRegisterCanSubmit()
    }

    fun onFotoUrlChange(value: String) {
        // La URL puede estar vacía, así que solo validamos si no está en blanco.
        _register.update { it.copy(fotoUrl = value, fotoUrlError = if (value.isNotBlank()) validateUrl(value) else null) }
        recomputeRegisterCanSubmit()
    }

    fun onRolIdChange(value: Long) {
        // Esta función parece no usarse en la UI, pero la mantenemos correcta
        _register.update { it.copy(rolId = value) }
        recomputeRegisterCanSubmit()
    }

    private fun recomputeRegisterCanSubmit() {
        val s = _register.value
        // --- CORRECCIÓN 4: Lista de errores actualizada para coincidir con el UiState ---
        val noErrors = listOf(s.nameError, s.emailError, s.phoneError, s.passError, s.confirmError, s.usernameError, s.runError, s.dvError, s.fotoUrlError).all { it == null }
        val filled = s.name.isNotBlank() && s.email.isNotBlank() && s.phone.isNotBlank() && s.pass.isNotBlank() && s.confirm.isNotBlank() && s.username.isNotBlank() && s.run.isNotBlank() && s.dv.isNotBlank() && s.rolId > 0
        _register.update { it.copy(canSubmit = noErrors && filled) }
    }

    fun submitRegister() {
        val s = _register.value
        if (!s.canSubmit || s.isSubmitting) return
        viewModelScope.launch {
            _register.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }
            delay(700)

            val result = repository.register(
                name = s.name,
                email = s.email,
                phone = s.phone,
                pass = s.pass,
                username = s.username,
                run = s.run,
                dv = s.dv,
                fotoUrl = s.fotoUrl,
                rol_id = s.rolId
            )

            _register.update {
                if (result.isSuccess) {
                    it.copy(isSubmitting = false, success = true, errorMsg = null)
                } else {
                    it.copy(
                        isSubmitting = false, success = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "No se pudo registrar"
                    )
                }
            }
        }
    }

    fun clearRegisterResult() {
        _register.update { it.copy(success = false, errorMsg = null) }
    }
}

