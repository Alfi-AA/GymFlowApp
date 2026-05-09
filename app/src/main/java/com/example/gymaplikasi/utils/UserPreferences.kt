package com.example.gymaplikasi.utils

import android.content.Context
import android.content.SharedPreferences

// Menangani penyimpanan data sederhana (Sesi Login, Profil User) menggunakan SharedPreferences
class UserPreferences(context: Context) {
    private val PREF_NAME = "GymFlowPrefs"
    private val preferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        const val KEY_IS_LOGIN = "IS_LOGIN"
        const val KEY_NAME = "NAME"
        const val KEY_GENDER = "GENDER"
        const val KEY_DOB = "DOB"
        const val KEY_WEIGHT = "WEIGHT"
        const val KEY_HEIGHT = "HEIGHT"
    }


    // --- Fungsi Login Awal ---
    // Menyimpan data profil user dan mengubah status login menjadi true
    fun saveUser(name: String) {
        val editor = preferences.edit()
        editor.putBoolean(KEY_IS_LOGIN, true)
        editor.putString(KEY_NAME, name)
        editor.apply()
    }

    // setter untuk edit profil
    fun setUserName(name: String) {
        preferences.edit().putString(KEY_NAME, name).apply()
    }
    fun setGender(gender: String) {
        preferences.edit().putString(KEY_GENDER, gender).apply()
    }
    fun setDob(dob: String) {
        preferences.edit().putString(KEY_DOB, dob).apply()
    }
    fun setWeight(weight: String) {
        preferences.edit().putString(KEY_WEIGHT, weight).apply()
    }
    fun setHeight(height: String) {
        preferences.edit().putString(KEY_HEIGHT, height).apply()
    }

    // --- GETTER untuk load profil ---
    fun getUserName(): String? = preferences.getString(KEY_NAME, "")
    fun isLoggedIn(): Boolean = preferences.getBoolean(KEY_IS_LOGIN, false)
    fun getGender(): String? = preferences.getString(KEY_GENDER, "Male")
    fun getDob(): String? = preferences.getString(KEY_DOB, "")
    fun getWeight(): String? = preferences.getString(KEY_WEIGHT, "")
    fun getHeight(): String? = preferences.getString(KEY_HEIGHT, "")

    fun logout() {
        preferences.edit().clear().apply()
    }
}