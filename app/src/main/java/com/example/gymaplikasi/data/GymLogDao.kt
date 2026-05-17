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
    /// ==========================================
    // 1. QUERY UNTUK SPINNER LATIHAN
    // ==========================================
    @Query("SELECT * FROM exercise_table ORDER BY name ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercise(exercise: Exercise)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllExercises(exercises: List<Exercise>)


    // ==========================================
    // 2. QUERY UNTUK GYM LOG
    // ==========================================
    @Insert
    suspend fun insertGymLog(gymLog: GymLog)

    @Update
    suspend fun updateGymLog(gymLog: GymLog)

    @Delete
    suspend fun deleteGymLog(gymLog: GymLog)


    // ==========================================
    // 3. QUERY DASHBOARD & HISTORY (TERISOLASI PER USER)
    // ==========================================

    // Menghitung total set HANYA milik user yang sedang login
    @Query("SELECT COUNT(*) FROM gym_logs WHERE userId = :userId AND date >= :startOfDay")
    fun getCountToday(userId: String, startOfDay: Long): Flow<Int>

    // Mengambil semua riwayat HANYA milik user yang sedang login
    @Query("SELECT * FROM gym_logs WHERE userId = :userId ORDER BY date DESC")
    fun getAllLogs(userId: String): Flow<List<GymLog>>

    // Mengambil data spesifik untuk Grafik HANYA milik user yang sedang login
    @Query("SELECT * FROM gym_logs WHERE userId = :userId AND exercise = :exerciseName ORDER BY date ASC")
    fun getLogsByExercise(userId: String, exerciseName: String): Flow<List<GymLog>>

    // Mengambil filter nama latihan HANYA yang pernah dilakukan user yang sedang login
    @Query("SELECT DISTINCT exercise FROM gym_logs WHERE userId = :userId ORDER BY exercise ASC")
    fun getUniqueExerciseNames(userId: String): Flow<List<String>>

    // Mengambil log pada hari tertentu (dari 00:00:00 sampai 23:59:59)
    @Query("SELECT * FROM gym_logs WHERE userId = :userId AND date >= :startOfDay AND date <= :endOfDay ORDER BY date DESC")
    fun getLogsByDateRange(userId: String, startOfDay: Long, endOfDay: Long): Flow<List<GymLog>>


    // ==========================================
    // 4. QUERY UNTUK RANKING / RADAR CHART
    // ==========================================

    // Mengambil Personal Record HANYA milik user yang sedang login
    @Query("SELECT MAX(weight) FROM gym_logs WHERE userId = :userId AND exercise = :exerciseName")
    suspend fun getMaxWeightForExercise(userId: String, exerciseName: String): Int?

    // ==========================================
    // 5. MANTRA BARU: UNTUK CLOUD SYNC FIRESTORE
    // ==========================================

    // Mengambil data milik user ini yang BELUM di-backup ke awan (isSynced = 0)
    @Query("SELECT * FROM gym_logs WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedLogs(userId: String): List<GymLog>

    // Mengubah status jadi "Sudah di-backup" (isSynced = 1) setelah sukses dikirim
    @Query("UPDATE gym_logs SET isSynced = 1 WHERE id IN (:logIds)")
    suspend fun markLogsAsSynced(logIds: List<Int>)
}