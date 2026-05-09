package com.example.gymaplikasi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymaplikasi.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(private val repository: GymRepository) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _gender = MutableStateFlow("")
    val gender: StateFlow<String> = _gender

    private val _dob = MutableStateFlow(0L)
    val dob: StateFlow<Long> = _dob

    private val _height = MutableStateFlow(0f)
    val height: StateFlow<Float> = _height

    private val _weight = MutableStateFlow(0f)
    val weight: StateFlow<Float> = _weight

    //fungsi memasukkan data variabel
    fun setName(newName: String) { _name.value = newName }

    fun setGender(newGender: String) { _gender.value = newGender }

    fun setDob(newDob: Long) { _dob.value = newDob }

    fun setPhysicalData(newHeight: Float, newWeight: Float) {
        _height.value = newHeight
        _weight.value = newWeight
    }

    //fungsi mengirim isi ke database
    fun saveProfileToDatabase(userId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            // semua data
            val profileData = hashMapOf<String, Any>(
                "name" to _name.value,
                "gender" to _gender.value,
                "dob" to _dob.value,
                "height" to _height.value,
                "weight" to _weight.value
            )

            // simpan ke firestore
            repository.saveUserProfile(userId, profileData)
            onComplete()
        }
    }
}