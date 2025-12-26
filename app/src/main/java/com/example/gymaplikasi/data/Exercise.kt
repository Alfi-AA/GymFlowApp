package com.example.gymaplikasi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_table")data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
