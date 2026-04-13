package com.example.gymaplikasi.domain

sealed class ProgressListItem {

    data class MuscleHeader(
        val muscle: Muscle,
        val iconResId: Int,
        val overallScore: Int,
        var isExpanded: Boolean = false, // Status laci terbuka/tertutup
        val exercises: List<ExerciseChild>
    ) : ProgressListItem()

    data class ExerciseChild(
        val exerciseName: String,
        val weightKg: Int,
        val score: Int,
        val nextRankKg: Int,
        val nextRankName: String
    ) : ProgressListItem()
}