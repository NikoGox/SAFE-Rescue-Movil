package com.movil.saferescue.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.saferescue.data.local.foto.FotoDao
import com.movil.saferescue.data.local.foto.FotoEntity
import com.movil.saferescue.data.local.user.UserEntity
import com.movil.saferescue.data.local.user.UserProfile
import com.movil.saferescue.data.repository.UserRepository
import com.movil.saferescue.domain.validation.validateChileanRUN
import com.movil.saferescue.domain.validation.validateNameLettersOnly
import com.movil.saferescue.domain.validation.validatePhoneDigitsOnly
import com.movil.saferescue.domain.validation.validateUsername
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
// <<< IMPORT ADICIONAL para generar el nombre de archivo con fecha
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ProfileUiState(
    val originalUser: UserProfile? = null,
    val id: Long? = null,
    val name: String = "",
    val nameError: String? = null,
    val username: String = "",
    val usernameError: String? = null,
    val phone: String = "",
    val phoneError: String? = null,
    val run: String = "",
    val dv: String = "",
    val runAndDvError: String? = null,
    val fotoUrl: String = "",
    val email: String = "",
    val rol: String = "",
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val canSave: Boolean = false,
    val isSubmitting: Boolean = false,
    val successMsg: String? = null,
    val errorMsg: String? = null
)

class ProfileViewModel(
    private val repository: UserRepository,
    private val fotoDao: FotoDao,
    private val applicationContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userProfile = repository.getLoggedInUser()

            if (userProfile != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        originalUser = userProfile,
                        id = userProfile.id,
                        name = userProfile.name,
                        username = userProfile.username,
                        phone = userProfile.phone,
                        run = userProfile.run,
                        dv = userProfile.dv,
                        email = userProfile.email,
                        rol = userProfile.rolName,
                        fotoUrl = userProfile.fotoUrl ?: ""
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMsg = "No se pudo cargar el perfil del usuario.") }
            }
        }
    }

    /**
     * Procesa la URI seleccionada, la copia a almacenamiento interno y guarda la nueva URI en la BD.
     * @param uri La URI de la imagen seleccionada (de cámara o galería).
     * @return El ID de la nueva FotoEntity guardada, o null si falla.
     */
    private suspend fun guardarImagenYObtenerId(uri: Uri): Long? {
        return withContext(Dispatchers.IO) {
            try {
                // <<< CORRECCIÓN 1: Generar un nombre de archivo único que incluye la fecha.
                val fileName = "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
                val destinationFile = File(applicationContext.filesDir, fileName)

                applicationContext.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(destinationFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                // <<< CORRECCIÓN 2: Crear la entidad usando el nuevo campo 'nombre'.
                val nuevaFoto = FotoEntity(
                    nombre = fileName, // Se usa el nombre de archivo generado.
                    url = Uri.fromFile(destinationFile).toString() // Guardamos la URI del archivo persistente.
                )

                fotoDao.insertFoto(nuevaFoto)

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Función pública que orquesta el cambio de foto de perfil.
     */
    fun actualizarFotoDePerfil(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val userId = _uiState.value.id
            if (userId == null) {
                _uiState.update { it.copy(isSubmitting = false, errorMsg = "Error: ID de usuario no encontrado.") }
                return@launch
            }

            val nuevoFotoId = guardarImagenYObtenerId(uri)

            if (nuevoFotoId != null) {
                val result = repository.updateUserPhoto(userId, nuevoFotoId)
                if (result.isSuccess) {
                    loadUserProfile()
                    _uiState.update { it.copy(isSubmitting = false, successMsg = "Foto de perfil actualizada.") }
                } else {
                    _uiState.update { it.copy(isSubmitting = false, errorMsg = "No se pudo actualizar la foto.") }
                }
            } else {
                _uiState.update { it.copy(isSubmitting = false, errorMsg = "Error al guardar la nueva imagen.") }
            }
        }
    }

    // --- El resto del ViewModel no necesita cambios ---

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

    private fun validateAllFields() {
        _uiState.update {
            val hasErrors = it.nameError != null || it.usernameError != null || it.phoneError != null || it.runAndDvError != null
            it.copy(canSave = !hasErrors)
        }
    }

    fun cancelEdit() {
        _uiState.update { currentState ->
            currentState.originalUser?.let { original ->
                currentState.copy(
                    isEditing = false,
                    name = original.name,
                    username = original.username,
                    phone = original.phone,
                    run = original.run,
                    dv = original.dv,
                    nameError = null,
                    usernameError = null,
                    phoneError = null,
                    runAndDvError = null,
                    errorMsg = null,
                    successMsg = null
                )
            } ?: currentState
        }
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditing = !it.isEditing) }
    }

    fun saveChanges() {
        val currentState = _uiState.value
        if (!currentState.canSave || currentState.id == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMsg = null, successMsg = null) }

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
