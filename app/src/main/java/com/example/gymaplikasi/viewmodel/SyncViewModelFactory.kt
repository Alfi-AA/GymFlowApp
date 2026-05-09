package com.example.gymaplikasi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gymaplikasi.repository.GymRepository

class SyncViewModelFactory(private val repository: GymRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SyncViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SyncViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}