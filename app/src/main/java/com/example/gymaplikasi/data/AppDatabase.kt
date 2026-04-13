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
                    // =======================
                    // UPPER BODY
                    // =======================

                    // Chest
                    Exercise(name = "Barbell Bench Press"),
                    Exercise(name = "Incline Bench Press"),
                    Exercise(name = "Pec Deck Fly"),
                    Exercise(name = "Cable Crossover"),

                    // Shoulder
                    Exercise(name = "Shoulder Press"),
                    Exercise(name = "Lateral Raise"),
                    Exercise(name = "Rear Delt Fly"),

                    // Bicep
                    Exercise(name = "Barbell Bicep Curl"),
                    Exercise(name = "Dumbbell Bicep Curl"),
                    Exercise(name = "Hammer Curl"),
                    Exercise(name = "Cross-Body Hammer Curl"),

                    // Tricep
                    Exercise(name = "Rope Tricep Pushdown"),
                    Exercise(name = "Straight Bar Pushdown"),
                    Exercise(name = "Skullcrusher"),
                    Exercise(name = "Weighted Tricep Dip"),

                    // Back
                    Exercise(name = "Weighted Pull Up"),
                    Exercise(name = "Deadlift"),
                    Exercise(name = "Cable Lat Pulldown"),
                    Exercise(name = "Seated Cable Row"),
                    Exercise(name = "One-Arm Dumbbell Row"),
                    Exercise(name = "Barbell Row"),

                    // Abs
                    Exercise(name = "Weighted Sit Up"),
                    Exercise(name = "Machine Crunch"),
                    Exercise(name = "Cable Crunch"),

                    // =======================
                    // LOWER BODY
                    // =======================

                    // Quads
                    Exercise(name = "Barbell Squat"),
                    Exercise(name = "Smith Machine Squat"),
                    Exercise(name = "Leg Press"),
                    Exercise(name = "Leg Extension"),
                    Exercise(name = "Bulgarian Split Squat"),

                    // Hamstrings
                    Exercise(name = "Romanian Deadlift"),
                    Exercise(name = "Lying Leg Curl"),
                    Exercise(name = "Seated Leg Curl"),

                    // Glutes
                    Exercise(name = "Barbell Hip Thrust"),
                    Exercise(name = "Cable Glute Kickback"),
                    Exercise(name = "Glute Bridge"),

                    // Calves
                    Exercise(name = "Standing Calf Raise"),
                    Exercise(name = "Seated Calf Raise")
                )
                dao.insertAllExercises(initialData)
            }
        }
    }
}