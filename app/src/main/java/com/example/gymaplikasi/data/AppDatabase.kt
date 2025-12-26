package com.example.gymaplikasi.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Mendefinisikan Database Room dan tabel yang terdaftar (GymLog & Exercise)
@Database(entities = [GymLog::class, Exercise::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gymLogDao(): GymLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gymflow_database"
                )
                    .addCallback(GymDatabaseCallback(context)) // Menjalankan callback saat database dibuat
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // Callback untuk mengisi data awal latihan saat aplikasi pertama kali diinstall
    private class GymDatabaseCallback(private val context: Context) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val dao = getDatabase(context).gymLogDao()

                val initialData = listOf(
                    Exercise(name = "Bench Press"),
                    Exercise(name = "Squat"),
                    Exercise(name = "Deadlift"),
                    Exercise(name = "Pull Up"),
                    Exercise(name = "Shoulder Press"),
                    Exercise(name = "Bicep Curl"),
                    Exercise(name = "Leg Press")
                )
                dao.insertAllExercises(initialData)
            }
        }
    }
}