package com.example.gymaplikasi.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GymLogDao {
    // --- QUERY UNTUK SPINNER LATIHAN ---

    // Mengambil semua jenis latihan untuk ditampilkan di Spinner
    @Query("SELECT * FROM exercise_table ORDER BY name ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercise(exercise: Exercise)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllExercises(exercises: List<Exercise>)


    // --- QUERY UNTUK GYM LOG (RIWAYAT) ---

    // Menyimpan data set latihan baru
    @Insert
    suspend fun insertGymLog(gymLog: GymLog)

    @Update
    suspend fun updateGymLog(gymLog: GymLog)

    @Delete
    suspend fun deleteGymLog(gymLog: GymLog)


    // --- QUERY UNTUK DASHBOARD (HOME) ---

    // Menghitung total set latihan yang dilakukan hari ini
    @Query("SELECT COUNT(*) FROM gym_logs WHERE date >= :startOfDay")
    fun getCountToday(startOfDay: Long): Flow<Int>


    // --- QUERY UNTUK HISTORY FRAGMENT ---

    // Mengambil semua riwayat latihan untuk ditampilkan di RecyclerView (Terbaru di atas)
    @Query("SELECT * FROM gym_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<GymLog>>

    // Mengambil data spesifik berdasarkan nama latihan untuk Grafik
    @Query("SELECT * FROM gym_logs WHERE exercise = :exerciseName ORDER BY date ASC")
    fun getLogsByExercise(exerciseName: String): Flow<List<GymLog>>

    // Mengambil daftar nama latihan yang pernah dilakukan untuk filter
    @Query("SELECT DISTINCT exercise FROM gym_logs ORDER BY exercise ASC")
    fun getUniqueExerciseNames(): Flow<List<String>>
}