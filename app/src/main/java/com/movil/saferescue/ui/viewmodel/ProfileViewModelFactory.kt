package com.movil.saferescue.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.movil.saferescue.data.repository.UserRepository

class ProfileViewModelFactory (
    private val repository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(repository) as T
        }
        // 4. Si se pide crear un ViewModel desconocido, lanza una excepción.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
