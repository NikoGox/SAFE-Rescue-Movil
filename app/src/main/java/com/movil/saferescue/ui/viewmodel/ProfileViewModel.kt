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
import com.movil.saferescue.domain.validation.validatePhoneExactNine
import com.movil.saferescue.domain.validation.validateUsername
import com.movil.saferescue.domain.validation.validateRolName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
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
    val errorMsg: String? = null,
    val isImagePickerDialogVisible: Boolean = false
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

    private suspend fun guardarImagenYObtenerId(uri: Uri): Long? {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
                val destinationFile = File(applicationContext.filesDir, fileName)

                applicationContext.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(destinationFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                val nuevaFoto = FotoEntity(
                    nombre = fileName,
                    url = Uri.fromFile(destinationFile).toString()
                )

                val newId = fotoDao.insertFoto(nuevaFoto)
                return@withContext newId

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

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

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName, nameError = validateNameLettersOnly(newName)) }
        validateAllFields()
    }

    fun onUsernameChange(newUsername: String) {
        _uiState.update { it.copy(usernameError = validateUsername(it.username)) }
        validateAllFields()
    }

    fun onPhoneChange(newPhone: String) {
        _uiState.update { it.copy(phone = newPhone, phoneError = validatePhoneExactNine(newPhone)) }
        validateAllFields()
    }

    fun onRunChange(newRun: String) {
        _uiState.update { it.copy(run = it.run) }
        val error = validateChileanRUN(_uiState.value.run, _uiState.value.dv)
        _uiState.update { it.copy(runAndDvError = error) }
        validateAllFields()
    }

    fun onDvChange(newDv: String) {
        _uiState.update { it.copy(dv = it.dv) }
        val error = validateChileanRUN(_uiState.value.run, _uiState.value.dv)
        _uiState.update { it.copy(runAndDvError = error) }
        validateAllFields()
    }

    private fun validateAllFields() {
        _uiState.update {
            val nameErr = validateNameLettersOnly(it.name)
            val phoneErr = validatePhoneExactNine(it.phone)
            val usernameErr = it.usernameError ?: validateUsername(it.username)
            val runDvErr = it.runAndDvError
            val rolErr = validateRolName(it.rol)

            it.copy(
                nameError = nameErr,
                phoneError = phoneErr,
                usernameError = usernameErr,
                canSave = (nameErr == null && phoneErr == null && usernameErr == null && runDvErr == null && rolErr == null)
            )
        }
    }

    fun cancelEdit() {
        _uiState.update { currentState ->
            currentState.originalUser?.let {
                original ->
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

            // <<< CORRECCIÓN: Obtener la entidad completa para no perder la contraseña >>>
            val currentUserEntity = repository.getUserById(currentState.id)
            if (currentUserEntity == null) {
                _uiState.update { it.copy(isSubmitting = false, errorMsg = "Error fatal: no se encontró el usuario actual.") }
                return@launch
            }

            // Crear la entidad actualizada manteniendo los datos que no se editan
            val updatedUserEntity = currentUserEntity.copy(
                name = currentState.name,
                phone = currentState.phone
            )

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

    fun onImagePickerClick() {
        _uiState.update { it.copy(isImagePickerDialogVisible = true) }
    }

    fun onImagePickerDismiss() {
        _uiState.update { it.copy(isImagePickerDialogVisible = false) }
    }

    fun onImageUriSelected(uri: Uri?) {
        if (uri != null) {
            actualizarFotoDePerfil(uri)
        } else {
            onImagePickerDismiss()
        }
    }
}
