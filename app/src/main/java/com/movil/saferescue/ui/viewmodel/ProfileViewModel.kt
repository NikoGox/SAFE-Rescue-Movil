package com.movil.saferescue.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.saferescue.data.local.user.UserEntity
import com.movil.saferescue.data.repository.UserRepository
import com.movil.saferescue.domain.validation.validateDv
import com.movil.saferescue.domain.validation.validateRun
import com.movil.saferescue.domain.validation.validateNameLettersOnly
import com.movil.saferescue.domain.validation.validatePhoneDigitsOnly
import com.movil.saferescue.domain.validation.validateUsername
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val email: String = "",
    val rol: String = "",
    // --- Datos y errores de los campos editables ---
    val name: String = "",
    val nameError: String? = null, // Error para el nombre
    val username: String = "",
    val usernameError: String? = null, // Error para el username
    val phone: String = "",
    val phoneError: String? = null, // Error para el teléfono
    val run: String = "",
    val runError: String? = null,
    val dv: String = "",
    val dvError: String? = null,
    val fotoUrl: String = "",
    // --- Estados de la UI ---
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val canSave: Boolean = false, // Para habilitar/deshabilitar el botón de guardar
    val isSubmitting: Boolean = false,
    val successMsg: String? = null,
    val errorMsg: String? = null // Error general (del guardado, no de los campos)
)

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var lastSavedState: ProfileUiState? = null

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val user = repository.getLoggedInUser()
            if (user != null) {
                val fotoUrl = repository.getFotoUrlById(user.foto_id) ?: ""
                val initialState = ProfileUiState(
                    isLoading = false,
                    email = user.email,
                    rol = if (user.rol_id == 2L) "Bombero" else "Usuario",
                    name = user.name,
                    username = user.username,
                    phone = user.phone,
                    run = user.run,
                    dv = user.dv,
                    fotoUrl = fotoUrl,
                    canSave = true // Inicialmente se puede guardar
                )
                _uiState.value = initialState
                lastSavedState = initialState
            } else {
                _uiState.update { it.copy(isLoading = false, errorMsg = "No se pudo encontrar el perfil.") }
            }
        }
    }

    // --- CAMBIO 2: Handlers de edición AHORA validan en tiempo real ---

    fun onNameChange(newName: String) {
        val error = validateNameLettersOnly(newName)
        _uiState.update { it.copy(name = newName, nameError = error) }
        validateAllFields()
    }

    fun onUsernameChange(newUsername: String) {
        val error = validateUsername(newUsername)
        _uiState.update { it.copy(username = newUsername, usernameError = error) }
        validateAllFields()
    }

    fun onPhoneChange(newPhone: String) {
        val error = validatePhoneDigitsOnly(newPhone)
        _uiState.update { it.copy(phone = newPhone, phoneError = error) }
        validateAllFields()
    }

    // El RUN/DV y la foto no tienen validación en tiempo real en este ejemplo,
    // pero se podría añadir de la misma forma.
    fun onRunChange(newRun: String) {
        val error = validateRun(newRun)
        _uiState.update { it.copy(run = newRun, runError = error) }
        validateAllFields()
    }
    fun onDvChange(newDv: String) {
        val error = validateDv(newDv)
        _uiState.update { it.copy(dv = newDv, dvError = error) }
        validateAllFields()
    }
    fun onFotoUrlChange(newUrl: String) { _uiState.update { it.copy(fotoUrl = newUrl) } }


    /**
     * Comprueba si todos los campos son válidos para habilitar el botón de Guardar.
     */
    private fun validateAllFields() {
        _uiState.update {
            // --- ¡ESTA ES LA LÓGICA CORRECTA! ---
            // Comprueba si alguno de los campos de error tiene un valor (no es nulo).
            val hasErrors = it.nameError != null ||
                    it.usernameError != null ||
                    it.phoneError != null ||
                    it.runError != null ||
                    it.dvError != null
            // `canSave` será `true` solo si `hasErrors` es `false`.
            it.copy(canSave = !hasErrors)
        }
    }


            // --- CAMBIO 3: Lógica de Cancelación AHORA limpia todos los errores ---
    fun cancelEdit() {
        lastSavedState?.let {
            // Restauramos los datos Y nos aseguramos de que todos los errores se limpien.
            _uiState.value = it.copy(
                isEditing = false,
                nameError = null,
                usernameError = null,
                phoneError = null,
                runError = null,
                dvError = null,
                errorMsg = null,
                successMsg = null
            )
        }
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditing = !it.isEditing) }
    }

    fun saveChanges() {
        val currentState = _uiState.value
        if (!currentState.canSave) return // No intentar guardar si hay errores

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMsg = null) }
            val loggedInUser = repository.getLoggedInUser() ?: return@launch

            val updatedUser = loggedInUser.copy(
                name = currentState.name,
                username = currentState.username,
                phone = currentState.phone,
                run = currentState.run,
                dv = currentState.dv
            )

            val result = repository.updateUser(updatedUser)
            if (result.isSuccess) {
                val newSavedState = currentState.copy(
                    isEditing = false,
                    isSubmitting = false,
                    successMsg = "¡Perfil actualizado!",
                    // Limpiamos errores de campo al guardar con éxito
                    nameError = null,
                    usernameError = null,
                    phoneError = null
                )
                lastSavedState = newSavedState
                _uiState.value = newSavedState
            } else {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "Error al guardar."
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMsg = null, errorMsg = null) }
    }
}
