package com.example.gymaplikasi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymaplikasi.repository.GymRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SyncViewModel(private val repository: GymRepository) : ViewModel() {

    fun triggerInitialSync(userId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.fetchInitialDataFromFirebase(userId)

            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun triggerUploadSync(userId: String) {
        viewModelScope.launch {
            repository.syncUnsyncedLogsToFirebase(userId)
        }
    }

    fun triggerDeleteSync(userId: String, logId: Int) {
        viewModelScope.launch {
            repository.deleteLogFromFirebase(userId, logId)
        }
    }
}