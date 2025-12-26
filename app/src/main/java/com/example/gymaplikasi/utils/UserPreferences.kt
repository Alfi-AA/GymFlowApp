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
        const val KEY_NIM = "NIM"
        const val KEY_CLASS = "CLASS"
        const val KEY_TARGET_BB = "TARGET_BB"
    }

    // Menyimpan data profil user dan mengubah status login menjadi true
    fun saveUser(name: String, nim: String, kelas: String, targetBb: Float) {
        val editor = preferences.edit()
        editor.putBoolean(KEY_IS_LOGIN, true)
        editor.putString(KEY_NAME, name)
        editor.putString(KEY_NIM, nim)
        editor.putString(KEY_CLASS, kelas)
        editor.putFloat(KEY_TARGET_BB, targetBb)
        editor.apply()
    }

    // Hanya memperbarui target berat badan
    fun saveTargetWeight(target: Float) {
        val editor = preferences.edit()
        editor.putFloat(KEY_TARGET_BB, target)
        editor.apply()
    }

    // Mengambil data user dari penyimpanan
    fun getUserName(): String? = preferences.getString(KEY_NAME, null)
    fun getNim(): String? = preferences.getString(KEY_NIM, "-")
    fun getKelas(): String? = preferences.getString(KEY_CLASS, "-")
    fun isLoggedIn(): Boolean = preferences.getBoolean(KEY_IS_LOGIN, false)
    fun getTargetBb(): Float = preferences.getFloat(KEY_TARGET_BB, 0f)

    // Menghapus semua data untuk logout
    fun logout() {
        preferences.edit().clear().apply()
    }
}