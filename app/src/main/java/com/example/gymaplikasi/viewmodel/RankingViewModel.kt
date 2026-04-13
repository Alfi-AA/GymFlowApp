package com.example.gymaplikasi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymaplikasi.R
import com.example.gymaplikasi.data.GymLogDao
import com.example.gymaplikasi.domain.BodyCategory
import com.example.gymaplikasi.domain.Muscle
import com.example.gymaplikasi.domain.ProgressListItem
import com.example.gymaplikasi.domain.exerciseToMuscleMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RankingViewModel(private val gymLogDao: GymLogDao) : ViewModel() {

    //1. Wadah pengampung data untuk UI
    private val _upperBodyScores = MutableLiveData<Map<Muscle, Int>>()
    val upperBodyScores: LiveData<Map<Muscle, Int>> = _upperBodyScores

    private val _lowerBodyScores = MutableLiveData<Map<Muscle, Int>>()
    val lowerBodyScores: LiveData<Map<Muscle, Int>> = _lowerBodyScores

    //2. Wadah untuk list ranking
    private val _muscleProgressList = MutableLiveData<List<ProgressListItem.MuscleHeader>>()
    val muscleProgressList: LiveData<List<ProgressListItem.MuscleHeader>> = _muscleProgressList

    //2. Fungsi untuk mulai menghitung
    fun calculateScores() {
        viewModelScope.launch(Dispatchers.IO) {
            val tempUpperScores = mutableMapOf<Muscle, Int>()
            val tempLowerScores = mutableMapOf<Muscle, Int>()

            val tempProgressList = mutableListOf<ProgressListItem.MuscleHeader>()

            Muscle.entries.forEach { currentMuscle ->

                // Variabel untuk menyimpan skor tertinggi untuk otot ini (dimulai dari 0)
                var highestScoreForThisMuscle = 0

                // Filter exerciseToMuscleMap, ambil yang muscle-nya sama dengan currentMuscle
                val relatedExercises = exerciseToMuscleMap.filter { it.value.muscle == currentMuscle }

                //List penampung latihan untuk otot
                val childrenList = mutableListOf<ProgressListItem.ExerciseChild>()

                relatedExercises.forEach { (exerciseName, targetData) ->
                    // Panggil fungsi Dao
                    val userMaxWeight = gymLogDao.getMaxWeightForExercise(exerciseName) ?: 0

                    // Rumus: (Beban Maksimal User / Target Mythril) * 100
                    if (targetData.targetMax > 0) {
                        val currentScore =
                            ((userMaxWeight.toFloat() / targetData.targetMax.toFloat()) * 100).toInt()

                        // SISTEM ELIMINASI
                        if (currentScore > highestScoreForThisMuscle) {
                            highestScoreForThisMuscle = currentScore
                        }

                        // JIKA USER PERNAH LATIHAN INI
                        if (userMaxWeight > 0) {
                            val finalCurrentScore = currentScore.coerceAtMost(100)

                            val (nextRankName, nextRankPercent) = getNextRankTarget(finalCurrentScore)
                            val nextRankKg = ((nextRankPercent / 100f) * targetData.targetMax).toInt()

                            childrenList.add(
                                ProgressListItem.ExerciseChild(
                                    exerciseName = exerciseName,
                                    weightKg = userMaxWeight,
                                    score = finalCurrentScore,
                                    nextRankKg = nextRankKg, // Berapa Kg yang harus dicapai
                                    nextRankName = nextRankName // Nama rank selanjutnya
                                )
                            )
                        }
                    }
                }

                // Batas maksimal 100%
                val finalScore = highestScoreForThisMuscle.coerceAtMost(100)

                // Memisahkan kategori upper atau lower
                if (currentMuscle.category == BodyCategory.UPPER_BODY) {
                    tempUpperScores[currentMuscle] = finalScore
                } else {
                    tempLowerScores[currentMuscle] = finalScore
                }

                childrenList.sortByDescending { it.score }

                tempProgressList.add(
                    ProgressListItem.MuscleHeader(
                        muscle = currentMuscle,
                        iconResId = getIconForMuscle(currentMuscle),
                        overallScore = finalScore,
                        isExpanded = false,
                        exercises = childrenList
                    )
                )
            }

            // Kirim ke Fragment
            _upperBodyScores.postValue(tempUpperScores)
            _lowerBodyScores.postValue(tempLowerScores)
            _muscleProgressList.postValue(tempProgressList)
        }
    }

    // fungsi otot menjadi icon
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

    // fungsi penentu target rank selanjutnya
    private fun getNextRankTarget(currentScore: Int): Pair<String, Int> {
        return when {
            currentScore < 20 -> Pair("Silver", 20)
            currentScore < 40 -> Pair("Gold", 40)
            currentScore < 60 -> Pair("Platinum", 60)
            currentScore < 80 -> Pair("Adamantium", 80)
            currentScore < 90 -> Pair("Mythril", 90)
            else -> Pair("Maxed", 100) // Jika sudah Mythril / 100%
        }
    }
}