package com.movil.saferescue.ui.viewmodel

import android.content.Context // <<< CORRECCIÓN 1: Importar Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.movil.saferescue.data.local.foto.FotoDao // <<< CORRECCIÓN 2: Importar FotoDao
import com.movil.saferescue.data.repository.UserRepository


class ProfileViewModelFactory (
    private val repository: UserRepository,
    private val fotoDao: FotoDao,
    private val applicationContext: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // <<< CORRECCIÓN 4: Pasar las nuevas dependencias al ViewModel
            return ProfileViewModel(repository, fotoDao, applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
