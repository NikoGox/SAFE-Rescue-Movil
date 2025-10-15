package com.movil.saferescue.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.movil.saferescue.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. Estado de la UI para la pantalla de perfil
data class ProfileUiState(
    // Datos del usuario (no editables)
    val email: String = "",
    val rol: String = "", // Ejemplo: "Bombero"
    // Datos editables
    val name: String = "",
    val username: String = "",
    val phone: String = "",
    val run: String = "",
    val dv: String = "",
    val fotoUrl: String = "",
    // Estado de la UI
    val isLoading: Boolean = true,
    val isEditing: Boolean = false, // Para cambiar entre modo vista y modo edición
    val isSubmitting: Boolean = false,
    val successMsg: String? = null,
    val errorMsg: String? = null
)

// 2. ViewModel para el Perfil
class ProfileViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Al iniciar el ViewModel, carga los datos del usuario logueado
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Asumimos que el repositorio puede darnos el usuario actual
            val user = repository.getCurrentUser()
            if (user != null) {
                val fotoUrl = repository.getFotoUrlById(user.foto_id) ?: ""
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        email = user.email,
                        rol = if (user.rol_id == 2L) "Bombero" else "Usuario",
                        name = user.name,
                        username = user.username,
                        phone = user.phone,
                        run = user.run,
                        dv = user.dv,
                        fotoUrl = fotoUrl
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMsg = "No se pudo cargar el perfil.") }
            }
        }
    }

    // --- Handlers para la edición ---
    fun onNameChange(newName: String) { _uiState.update { it.copy(name = newName) } }
    fun onUsernameChange(newUsername: String) { _uiState.update { it.copy(username = newUsername) } }
    fun onPhoneChange(newPhone: String) { _uiState.update { it.copy(phone = newPhone) } }
    fun onRunChange(newRun: String) { _uiState.update { it.copy(run = newRun) } }
    fun onDvChange(newDv: String) { _uiState.update { it.copy(dv = newDv) } }
    fun onFotoUrlChange(newUrl: String) { _uiState.update { it.copy(fotoUrl = newUrl) } }

    // Cambiar entre modo vista y edición
    fun toggleEditMode() {
        _uiState.update { it.copy(isEditing = !it.isEditing) }
    }

    // Guardar los cambios
    fun saveChanges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMsg = null, successMsg = null) }
            val currentState = _uiState.value

            // Aquí llamarías a la función de tu repositorio para actualizar los datos
            // val result = repository.updateUser(...)
            // Por ahora, simulamos un éxito
            kotlinx.coroutines.delay(1000) // Simula llamada de red

            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    isEditing = false, // Vuelve a modo vista
                    successMsg = "¡Perfil actualizado con éxito!"
                )
            }
        }
    }
}

