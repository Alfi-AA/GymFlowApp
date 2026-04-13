package com.example.gymaplikasi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gymaplikasi.data.GymLogDao

class RankingViewModelFactory(private val dao: GymLogDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RankingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RankingViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}