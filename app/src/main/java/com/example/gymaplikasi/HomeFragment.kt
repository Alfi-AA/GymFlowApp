package com.example.gymaplikasi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gymaplikasi.data.AppDatabase
import com.example.gymaplikasi.data.GymLog
import com.example.gymaplikasi.domain.Muscle
import com.example.gymaplikasi.domain.exerciseToMuscleMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import com.example.gymaplikasi.utils.UserPreferences

class HomeFragment : Fragment() {

    private lateinit var tvName: TextView
    private lateinit var tvTarget: TextView
    private lateinit var tvTotalSet: TextView
    private lateinit var autoCompleteExercise: AutoCompleteTextView
    private lateinit var etWeight: EditText
    private lateinit var etReps: EditText
    private lateinit var btnSave: Button
    private var validExerciseNames: List<String> = emptyList()

    private lateinit var ivNeglectedMuscleIcon: ImageView
    private lateinit var tvNeglectedMuscleName: TextView
    private lateinit var ivLastWorkoutRank: ImageView
    private lateinit var tvLastWorkoutName: TextView
    private lateinit var tvLastWorkoutDetail: TextView

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
        autoCompleteExercise= view.findViewById(R.id.autoCompleteExercise)
        etWeight = view.findViewById(R.id.etWeight)
        etReps = view.findViewById(R.id.etReps)
        btnSave = view.findViewById(R.id.btnSaveRecord)

        ivNeglectedMuscleIcon = view.findViewById(R.id.ivNeglectedMuscleIcon)
        tvNeglectedMuscleName = view.findViewById(R.id.tvNeglectedMuscleName)
        ivLastWorkoutRank = view.findViewById(R.id.ivLastWorkoutRank)
        tvLastWorkoutName = view.findViewById(R.id.tvLastWorkoutName)
        tvLastWorkoutDetail = view.findViewById(R.id.tvLastWorkoutDetail)

        setupHeader()
        setupAutoComplete()
        observeTodaySummary()
        observeFooterData()
        
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

    // Mengambil daftar latihan dari database untuk mengisi AutoComplete
    private fun setupAutoComplete() {
        lifecycleScope.launch {
            val dao = db.gymLogDao()
            dao.getAllExercises().collect { exerciseList ->

                if (exerciseList.isNotEmpty()) {
                    // KONDISI 1: Database SIAP dan ADA ISINYA
                    validExerciseNames = exerciseList.map { it.name }


                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        validExerciseNames
                    )
                    autoCompleteExercise.setAdapter(adapter)

                    // Aktifkan tombol Save karena data sudah valid
                    btnSave.isEnabled = true
                    btnSave.alpha = 1.0f

                } else {
                    // KONDISI 2: Database KOSONG atau sedang proses LOADING
                    val loadingAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_selected, listOf("Loading exercises..."))
                    autoCompleteExercise.setAdapter(loadingAdapter)

                    // MATIKAN tombol Save agar user tidak mensubmit teks "Loading exercises..." ke tabel GymLog
                    btnSave.isEnabled = false
                    btnSave.alpha = 0.5f
                }
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

    // LOGIKA LAST WORKOUT DAN MUSCLE NEGLECTED
    private fun observeFooterData() {
        lifecycleScope.launch {
            db.gymLogDao().getAllLogs().collect { logs ->

                val lastLog = logs.firstOrNull()
                if (lastLog != null) {
                    tvLastWorkoutName.text = lastLog.exercise
                    val weightText = if (lastLog.weight % 1f == 0f) lastLog.weight.toInt().toString() else lastLog.weight.toString()
                    tvLastWorkoutDetail.text = "$weightText kg x ${lastLog.reps} reps"

                    val targetData = exerciseToMuscleMap[lastLog.exercise]
                    val score = if (targetData != null && targetData.targetMax > 0) {
                        ((lastLog.weight.toFloat() / targetData.targetMax.toFloat()) * 100).toInt()
                    } else 0
                    ivLastWorkoutRank.setImageResource(getRankIconForScore(score))
                } else {
                    tvLastWorkoutName.text = "Belum Ada"
                    tvLastWorkoutDetail.text = "Mulai latihan pertamamu!"
                    ivLastWorkoutRank.setImageResource(R.drawable.rank_bronze)
                }

                val sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
                val recentLogs = logs.filter { it.date >= sevenDaysAgo }

                val muscleCounts = mutableMapOf<Muscle, Int>()
                Muscle.entries.forEach { muscleCounts[it] = 0 }

                recentLogs.forEach { log ->
                    val muscle = exerciseToMuscleMap[log.exercise]?.muscle
                    if (muscle != null) {
                        muscleCounts[muscle] = muscleCounts[muscle]!! + 1
                    }
                }

                val neglectedMuscle = muscleCounts.minByOrNull { it.value }?.key ?: Muscle.CHEST

                tvNeglectedMuscleName.text = neglectedMuscle.name
                ivNeglectedMuscleIcon.setImageResource(getIconForMuscle(neglectedMuscle))
            }
        }
    }

    // Menyimpan data latihan baru ke database di background thread
    private fun saveWorkoutLog() {
        val exerciseName = autoCompleteExercise.text.toString()
        val weightStr = etWeight.text.toString()
        val repsStr = etReps.text.toString()

        if (exerciseName.isEmpty() || exerciseName == "Loading exercises...") return
        if (!validExerciseNames.contains(exerciseName)) {
            Toast.makeText(requireContext(), "Latihan tidak valid! Pilih dari daftar dropdown", Toast.LENGTH_SHORT).show()
            autoCompleteExercise.showDropDown()
            return
        }
        if (weightStr.isEmpty() || repsStr.isEmpty()) {
            Toast.makeText(requireContext(), "Harap isi beban & repetisi", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val newLog = GymLog(exercise = exerciseName, weight = weightStr.toInt(), reps = repsStr.toInt(), date = System.currentTimeMillis())
            db.gymLogDao().insertGymLog(newLog)

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Set Tersimpan! Mantap Bro!", Toast.LENGTH_SHORT).show()
                etWeight.text.clear()
                etReps.text.clear()
            }
        }
    }

    private fun getIconForMuscle(muscle: Muscle): Int {
        return when (muscle) {
            Muscle.CHEST -> R.drawable.ic_icon_chest
            Muscle.BACK -> R.drawable.ic_icon_back
            Muscle.QUADS -> R.drawable.ic_icon_quad
            Muscle.BICEP -> R.drawable.ic_icon_bicep
            Muscle.TRICEP -> R.drawable.ic_icon_tricep
            Muscle.ABS -> R.drawable.ic_icon_abs
            Muscle.CALVES -> R.drawable.ic_icon_calves
            Muscle.SHOULDERS -> R.drawable.ic_icon_shoulder
            Muscle.HAMSTRINGS -> R.drawable.ic_icon_hamstring
            Muscle.GLUTES -> R.drawable.ic_icon_glutes
        }
    }

    private fun getRankIconForScore(score: Int): Int {
        return when (score) {
            in 90..Int.MAX_VALUE -> R.drawable.rank_mythril
            in 80..89 -> R.drawable.rank_adamantium
            in 60..79 -> R.drawable.rank_platinum
            in 40..59 -> R.drawable.rank_gold
            in 20..39 -> R.drawable.rank_silver
            else -> R.drawable.rank_bronze
        }
    }
}