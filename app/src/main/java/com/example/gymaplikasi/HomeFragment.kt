package com.example.gymaplikasi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gymaplikasi.data.AppDatabase
import com.example.gymaplikasi.data.GymLog
import com.example.gymaplikasi.utils.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class HomeFragment : Fragment() {

    private lateinit var tvName: TextView
    private lateinit var tvTarget: TextView
    private lateinit var tvTotalSet: TextView
    private lateinit var spinnerExercise: Spinner
    private lateinit var etWeight: EditText
    private lateinit var etReps: EditText
    private lateinit var btnSave: Button

    private lateinit var db: AppDatabase
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Database Room dan UserPreferences
        db = AppDatabase.getDatabase(requireContext())
        userPreferences = UserPreferences(requireContext())

        // Menghubungkan variabel dengan komponen UI di XML
        tvName = view.findViewById(R.id.tvWelcomeName)
        tvTarget = view.findViewById(R.id.tvTargetWeight)
        tvTotalSet = view.findViewById(R.id.tvTotalSetCount)
        spinnerExercise = view.findViewById(R.id.spinnerExercise)
        etWeight = view.findViewById(R.id.etWeight)
        etReps = view.findViewById(R.id.etReps)
        btnSave = view.findViewById(R.id.btnSaveRecord)

        setupHeader()
        setupSpinner()
        observeTodaySummary()
        
        btnSave.setOnClickListener {
            saveWorkoutLog()
        }
    }

    // Menampilkan nama user dan target berat badan dari SharedPreferences
    private fun setupHeader() {
        val name = userPreferences.getUserName() ?: "User"
        val firstName = name.split(" ")[0]
        val targetBb = userPreferences.getTargetBb()

        tvName.text = "Hello, $firstName!"
        tvTarget.text = "Target BW: $targetBb kg"
    }

    // Mengambil daftar latihan dari database untuk mengisi Spinner
    private fun setupSpinner() {
        lifecycleScope.launch {
            val dao = db.gymLogDao()
            dao.getAllExercises().collect { exerciseList ->
                // Gunakan list default jika database kosong
                val exerciseNames = if (exerciseList.isNotEmpty()) {
                    exerciseList.map { it.name }
                } else {
                    listOf("Bench Press", "Squat", "Deadlift", "Overhead Press", "Pull Up", "Bicep Curl")
                }

                val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner_selected, exerciseNames)
                adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
                spinnerExercise.adapter = adapter
            }
        }
    }

    // Memantau jumlah set hari ini secara real-time dari database
    private fun observeTodaySummary() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis

        lifecycleScope.launch {
            db.gymLogDao().getCountToday(startOfDay).collect { count ->
                tvTotalSet.text = count.toString()
            }
        }
    }

    // Menyimpan data latihan baru ke database di background thread
    private fun saveWorkoutLog() {
        val exerciseName = spinnerExercise.selectedItem.toString()
        val weightStr = etWeight.text.toString()
        val repsStr = etReps.text.toString()

        if (weightStr.isEmpty() || repsStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill weight & reps", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val newLog = GymLog(
                exercise = exerciseName,
                weight = weightStr.toInt(),
                reps = repsStr.toInt(),
                date = System.currentTimeMillis()
            )

            db.gymLogDao().insertGymLog(newLog)

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Set Saved! Keep Pushing!", Toast.LENGTH_SHORT).show()
                etWeight.text.clear()
                etReps.text.clear()
            }
        }
    }
}