package com.example.gymaplikasi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gym_logs")
data class GymLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val exercise: String, // Nama latihan, misal: "Bench Press"
    val weight: Int,      // Beban (kg/lbs)
    val reps: Int,        // Jumlah repetisi
    val date: Long = System.currentTimeMillis() // Default ke waktu sekarang
)
