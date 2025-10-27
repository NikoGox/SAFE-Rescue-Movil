// Ruta: app/src/main/java/com/movil/saferescue/ui/viewmodel/AuthViewModel.kt
package com.movil.saferescue.ui.viewmodel

import androidx.compose.ui.semantics.password
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.saferescue.data.local.storage.UserPreferences
import com.movil.saferescue.data.local.user.UserEntity
import com.movil.saferescue.data.repository.UserRepository // <<< ¡IMPORTANTE!
import com.movil.saferescue.domain.validation.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

// Las clases data LoginUiState y RegisterUiState no cambian

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
    val rolId: Long = 3L,
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
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login.asStateFlow()

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register.asStateFlow()

    private val _isAuthenticated = MutableStateFlow<Boolean?>(null)
    val isAuthenticated: StateFlow<Boolean?> = _isAuthenticated.asStateFlow()

    init {
        // Intento de restauración de sesión al inicio
        viewModelScope.launch {
            // Leemos solo el valor inicial de activeUserIdFlow para intentar restaurar
            // la sesión al iniciar el ViewModel. No hacemos una colección continua porque
            // eso provocaba que, al no persistir la sesión (rememberMe = false), el flujo
            // emitiera null y sobreescribiera el estado de autenticación justo después
            // de un login exitoso, forzando un logout inmediato.
            try {
                val userId = userPreferences.activeUserIdFlow.firstOrNull()
                if (userId != null) {
                    userRepository.tryLoginFromPreferences().fold(
                        onSuccess = { user ->
                            _isAuthenticated.value = user != null
                        },
                        onFailure = {
                            _isAuthenticated.value = false
                        }
                    )
                } else {
                    _isAuthenticated.value = false
                }
            } catch (e: Exception) {
                _isAuthenticated.value = false
            }
        }
    }

    fun setAuthenticationState(authenticated: Boolean) {
        _isAuthenticated.value = authenticated
    }

    private fun clearAllState() {
        viewModelScope.launch {
            userPreferences.clearUserSession()
            userPreferences.clearUserIdentifier()
            _login.update { LoginUiState() }
            _register.update { RegisterUiState() }
            _isAuthenticated.value = false
        }
    }

    /**
     * Cierra la sesión del usuario. Limpia la sesión persistente.
     */
    fun logout() {
        viewModelScope.launch {
            userRepository.clearLoggedInUser()
            userPreferences.clearUserSession()
            userPreferences.clearUserIdentifier()
            _login.update { LoginUiState() }
            _register.update { RegisterUiState() }
            _isAuthenticated.value = false
        }
    }

    // --- LÓGICA DE LOGIN ---

    fun submitLogin(rememberMe: Boolean) {
        val s = _login.value
        if (!s.canSubmit || s.isSubmitting) return

        viewModelScope.launch {
            try {
                _login.update { it.copy(isSubmitting = true, errorMsg = null) }

                val user = userRepository.getByEmailOrUsername(s.identifier)
                if (user == null || s.identifier.isBlank()) {
                    _login.update { it.copy(isSubmitting = false, errorMsg = "Usuario no encontrado o identificador vacío") }
                    return@launch
                }

                val result = userRepository.login(s.identifier, s.pass, rememberMe)
                result.fold(
                    onSuccess = { loggedInUser ->
                        _isAuthenticated.value = true
                        if (rememberMe) {
                            userPreferences.saveUserSession(loggedInUser.id)
                            userPreferences.saveUserIdentifier(s.identifier)
                        }
                        _login.update { it.copy(success = true, isSubmitting = false) }
                    },
                    onFailure = { e ->
                        _isAuthenticated.value = false
                        userPreferences.clearUserSession()
                        userPreferences.clearUserIdentifier()
                        _login.update {
                            it.copy(
                                isSubmitting = false,
                                errorMsg = e.message ?: "Error al iniciar sesión"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _isAuthenticated.value = false
                userPreferences.clearUserSession()
                userPreferences.clearUserIdentifier()
                _login.update {
                    it.copy(
                        isSubmitting = false,
                        errorMsg = "Error inesperado al iniciar sesión"
                    )
                }
            }
        }
    }

    // --- LÓGICA DE REGISTRO ---

    fun submitRegister() {
        val s = _register.value
        if (!s.canSubmit || s.isSubmitting) return

        viewModelScope.launch {
            try {
                _register.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }

                // Verificar si el usuario ya existe
                val existingUser = userRepository.getByEmailOrUsername(s.email)
                    ?: userRepository.getByEmailOrUsername(s.username)

                if (existingUser != null) {
                    val errorMsg = when {
                        existingUser.email == s.email -> "El email ya está en uso"
                        existingUser.username == s.username -> "El nombre de usuario ya está en uso"
                        else -> "Usuario ya existe"
                    }
                    throw Exception(errorMsg)
                }

                // Crear nuevo usuario
                val hashedPassword = BCrypt.hashpw(s.pass, BCrypt.gensalt())
                val newUser = UserEntity(
                    name = s.name,
                    email = s.email,
                    phone = s.phone,
                    password = hashedPassword,
                    username = s.username,
                    run = s.run,
                    dv = s.dv,
                    foto_id = null,
                    rol_id = s.rolId
                )

                userRepository.createUser(newUser)
                _register.update { it.copy(isSubmitting = false, success = true) }

            } catch (e: Exception) {
                _register.update {
                    it.copy(
                        isSubmitting = false,
                        errorMsg = e.message ?: "Error al registrar el usuario"
                    )
                }
            }
        }
    }

    // --- El resto de funciones (handlers de cambio de texto y validaciones) no cambian ---
    fun onLoginIdentifierChange(value: String) { _login.update { it.copy(identifier = value, identifierError = null, errorMsg = null) }; recomputeLoginCanSubmit() }
    fun onLoginPassChange(value: String) { _login.update { it.copy(pass = value, passError = null, errorMsg = null) }; recomputeLoginCanSubmit() }
    private fun recomputeLoginCanSubmit() { val s = _login.value; _login.update { it.copy(canSubmit = s.identifier.isNotBlank() && s.pass.isNotBlank()) } }
    fun clearLoginResult() { _login.update { it.copy(success = false, errorMsg = null) } }
    fun onNameChange(value: String) { val f = value.filter { it.isLetter() || it.isWhitespace() }; _register.update { it.copy(name = f, nameError = validateNameLettersOnly(f)) }; recomputeRegisterCanSubmit() }
    fun onRegisterEmailChange(value: String) { _register.update { it.copy(email = value, emailError = validateEmail(value)) }; recomputeRegisterCanSubmit() }
    fun onPhoneChange(value: String) { val d = value.filter { it.isDigit() }; _register.update { it.copy(phone = d, phoneError = validatePhoneDigitsOnly(d)) }; recomputeRegisterCanSubmit() }
    fun onRegisterPassChange(value: String) { _register.update { it.copy(pass = value, passError = validateStrongPassword(value), confirmError = validateConfirm(value, it.confirm)) }; recomputeRegisterCanSubmit() }
    fun onConfirmChange(value: String) { _register.update { it.copy(confirm = value, confirmError = validateConfirm(it.pass, value)) }; recomputeRegisterCanSubmit() }
    fun onUsernameChange(value: String) { _register.update { it.copy(username = value, usernameError = validateUsername(value)) }; recomputeRegisterCanSubmit() }
    fun onRunChange(value: String) { val d = value.filter { it.isDigit() }; val e = validateChileanRUN(d, _register.value.dv); _register.update { it.copy(run = d, runError = e, dvError = e) }; recomputeRegisterCanSubmit() }
    fun onDvChange(value: String) { val u = value.uppercase().filter { it.isLetterOrDigit() }; val e = validateChileanRUN(_register.value.run, u); _register.update { it.copy(dv = u, runError = e, dvError = e) }; recomputeRegisterCanSubmit() }
    fun onFotoUrlChange(value: String) { _register.update { it.copy(fotoUrl = value, fotoUrlError = if (value.isNotBlank()) validateUrl(value) else null) }; recomputeRegisterCanSubmit() }
    fun onRolIdChange(value: Long) { _register.update { it.copy(rolId = value) }; recomputeRegisterCanSubmit() }
    private fun recomputeRegisterCanSubmit() { val s = _register.value; val ok = listOf(s.nameError, s.emailError, s.phoneError, s.passError, s.confirmError, s.usernameError, s.runError, s.dvError, s.fotoUrlError).all { it == null }; val filled = s.name.isNotBlank() && s.email.isNotBlank() && s.phone.isNotBlank() && s.pass.isNotBlank() && s.confirm.isNotBlank() && s.username.isNotBlank() && s.run.isNotBlank() && s.dv.isNotBlank(); _register.update { it.copy(canSubmit = ok && filled) } }
    fun clearRegisterResult() { _register.update { RegisterUiState() } }
}
