package com.movil.saferescue.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.saferescue.data.local.user.UserEntity
import com.movil.saferescue.data.local.user.UserProfile
import com.movil.saferescue.data.repository.UserRepository
import com.movil.saferescue.domain.validation.validateChileanRUN
import com.movil.saferescue.domain.validation.validateNameLettersOnly
import com.movil.saferescue.domain.validation.validatePhoneDigitsOnly
import com.movil.saferescue.domain.validation.validateUsername
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 2. Data Class actualizada para usar el modelo de dominio
data class ProfileUiState(
    // Datos originales para poder cancelar la edición
    val originalUser: UserProfile? = null,

    // Datos y errores de los campos editables
    val id: Long? = null,
    val name: String = "",
    val nameError: String? = null,
    val username: String = "",
    val usernameError: String? = null,
    val phone: String = "",
    val phoneError: String? = null,
    val run: String = "",
    val dv: String = "",
    val runAndDvError: String? = null, // Un solo error para RUN y DV
    val fotoUrl: String = "",

    // Campos no editables que solo se muestran
    val email: String = "",
    val rol: String = "",

    // Estados de la UI
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val canSave: Boolean = false,
    val isSubmitting: Boolean = false,
    val successMsg: String? = null,
    val errorMsg: String? = null
)

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    // 3. Lógica de carga COMPLETAMENTE SIMPLIFICADA
    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Llama al método que ya hace todo el trabajo pesado
            val userProfile = repository.getLoggedInUser()

            if (userProfile != null) {
                // El repositorio ya nos dio el nombre del rol y la URL de la foto.
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        originalUser = userProfile, // Guardamos el estado original
                        id = userProfile.id,
                        name = userProfile.name,
                        username = userProfile.username,
                        phone = userProfile.phone,
                        run = userProfile.run,
                        dv = userProfile.dv,
                        email = userProfile.email,
                        rol = userProfile.rolName, // <-- Nombre del rol ya viene listo
                        fotoUrl = userProfile.fotoUrl ?: "" // <-- URL de la foto ya viene lista
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMsg = "No se pudo cargar el perfil del usuario.") }
            }
        }
    }

    // --- Handlers de edición (sin cambios, pero ahora más robustos) ---

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName, nameError = validateNameLettersOnly(newName)) }
        validateAllFields()
    }

    fun onUsernameChange(newUsername: String) {
        _uiState.update { it.copy(username = newUsername, usernameError = validateUsername(newUsername)) }
        validateAllFields()
    }

    fun onPhoneChange(newPhone: String) {
        _uiState.update { it.copy(phone = newPhone, phoneError = validatePhoneDigitsOnly(newPhone)) }
        validateAllFields()
    }

    fun onRunChange(newRun: String) {
        _uiState.update { it.copy(run = newRun) }
        // Validamos el RUN y el DV juntos
        val error = validateChileanRUN(_uiState.value.run, _uiState.value.dv)
        _uiState.update { it.copy(runAndDvError = error) }
        validateAllFields()
    }

    fun onDvChange(newDv: String) {
        _uiState.update { it.copy(dv = newDv.uppercase()) }
        val error = validateChileanRUN(_uiState.value.run, _uiState.value.dv)
        _uiState.update { it.copy(runAndDvError = error) }
        validateAllFields()
    }

    // --- Lógica de UI (Guardar, Cancelar, etc.) ---

    private fun validateAllFields() {
        _uiState.update {
            val hasErrors = it.nameError != null || it.usernameError != null || it.phoneError != null || it.runAndDvError != null
            it.copy(canSave = !hasErrors)
        }
    }

    fun cancelEdit() {
        _uiState.update { currentState ->
            currentState.originalUser?.let { original ->
                // Restaura todo a partir del 'originalUser' guardado
                currentState.copy(
                    isEditing = false,
                    name = original.name,
                    username = original.username,
                    phone = original.phone,
                    run = original.run,
                    dv = original.dv,
                    // Limpiamos todos los errores
                    nameError = null,
                    usernameError = null,
                    phoneError = null,
                    runAndDvError = null,
                    errorMsg = null,
                    successMsg = null
                )
            } ?: currentState // Si no hay original, no hagas nada
        }
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditing = !it.isEditing) }
    }

    // 4. Lógica de guardado SIMPLIFICADA
    fun saveChanges() {
        val currentState = _uiState.value
        if (!currentState.canSave || currentState.id == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMsg = null, successMsg = null) }

            // Creamos una UserEntity a partir del estado de la UI para enviarla al repositorio
            val updatedUserEntity = UserEntity(
                id = currentState.id,
                name = currentState.name,
                username = currentState.username,
                email = currentState.email,
                phone = currentState.phone,
                run = currentState.run,
                dv = currentState.dv,
                password = "",
                foto_id = currentState.originalUser?.fotoId ?: 1,
                rol_id = currentState.originalUser?.rolId ?: 2 )

            val result = repository.updateUser(updatedUserEntity)

            if (result.isSuccess) {
                // Volvemos a cargar el perfil para obtener la información fresca y actualizada
                loadUserProfile()
                _uiState.update {
                    it.copy(
                        isEditing = false,
                        isSubmitting = false,
                        successMsg = "¡Perfil actualizado con éxito!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "Error al guardar el perfil."
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMsg = null, errorMsg = null) }
    }
}
