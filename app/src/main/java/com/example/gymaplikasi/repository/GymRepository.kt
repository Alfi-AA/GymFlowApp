package com.example.gymaplikasi.repository

import com.example.gymaplikasi.data.GymLog
import com.example.gymaplikasi.data.GymLogDao
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class GymRepository(
    private val gymLogDao: GymLogDao,
    private val firestore: FirebaseFirestore
) {

    // Fungsi menarik data latihan dari cloud
    suspend fun fetchInitialDataFromFirebase(userId: String) {
        withContext(Dispatchers.IO) {
            try {
                // Ambil semua data dari koleksi log user
                val documents = firestore.collection("users").document(userId)
                    .collection("logs")
                    .get()
                    .await()

                val remoteLogs = mutableListOf<GymLog>()

                for (document in documents) {
                    val cloudId = document.id.toIntOrNull() ?: 0
                    val exercise = document.getString("exercise") ?: ""
                    val weight = document.getLong("weight")?.toInt() ?: 0
                    val reps = document.getLong("reps")?.toInt() ?: 0
                    val date = document.getLong("date") ?: 0L

                    remoteLogs.add(
                        GymLog(
                            id = cloudId,
                            userId = userId,
                            exercise = exercise,
                            weight = weight,
                            reps = reps,
                            date = date,
                            isSynced = true
                        )
                    )
                }

                if (remoteLogs.isNotEmpty()) {
                    remoteLogs.forEach { log ->
                        gymLogDao.insertGymLog(log)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace() // Tangkap error jika internet mati
            }
        }
    }

    // Fungsi kirim data latihan ke cloud
    suspend fun syncUnsyncedLogsToFirebase(userId: String) {
        withContext(Dispatchers.IO) {
            try {
                val unsyncedLogs = gymLogDao.getUnsyncedLogs(userId)
                if (unsyncedLogs.isEmpty()) return@withContext

                val logIdsToUpdate = mutableListOf<Int>()

                for (log in unsyncedLogs) {
                    val logMap = hashMapOf(
                        "exercise" to log.exercise,
                        "weight" to log.weight,
                        "reps" to log.reps,
                        "date" to log.date
                    )

                    firestore.collection("users").document(userId)
                        .collection("logs").document(log.id.toString())
                        .set(logMap)
                        .await()

                    logIdsToUpdate.add(log.id)
                }

                if (logIdsToUpdate.isNotEmpty()) {
                    gymLogDao.markLogsAsSynced(logIdsToUpdate)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Fungsi hapus data
    suspend fun deleteLogFromFirebase(userId: String, logId: Int) {
        withContext(Dispatchers.IO) {
            try {
                firestore.collection("users").document(userId)
                    .collection("logs").document(logId.toString())
                    .delete()
                    .await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Fungsi memyimpan profile user
    suspend fun saveUserProfile(userId: String, profileData: HashMap<String, Any>) {
        withContext(Dispatchers.IO) {
            try {
                firestore.collection("users").document(userId)
                    .set(profileData)
                    .await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}