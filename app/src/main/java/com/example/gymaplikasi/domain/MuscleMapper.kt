package com.example.gymaplikasi.domain


// 1. Kategori Tubuh
enum class BodyCategory {
    UPPER_BODY, LOWER_BODY
}

// 2. Enum otot sesuai kategori tubuh
enum class Muscle(val category: BodyCategory) {
    CHEST(BodyCategory.UPPER_BODY),
    BACK(BodyCategory.UPPER_BODY),
    SHOULDERS(BodyCategory.UPPER_BODY),
    BICEP(BodyCategory.UPPER_BODY),
    TRICEP(BodyCategory.UPPER_BODY),
    ABS(BodyCategory.UPPER_BODY),

    QUADS(BodyCategory.LOWER_BODY),
    HAMSTRINGS(BodyCategory.LOWER_BODY),
    GLUTES(BodyCategory.LOWER_BODY),
    CALVES(BodyCategory.LOWER_BODY)
}

// 3. Pembungkus Data Target
data class ExerciseTarget(
    val muscle: Muscle,
    val targetMax: Int // batas beban 100% untuk laki2 dalam kg
)

// 4. Kamus Pemetaan Latihan ke Otot
val exerciseToMuscleMap = mapOf<String, ExerciseTarget>(

    // ==================================
    // Chest
    // ==================================
    "Barbell Bench Press" to ExerciseTarget(Muscle.CHEST, 140),
    "Incline Bench Press" to ExerciseTarget(Muscle.CHEST, 110),
    "Pec Deck Fly" to ExerciseTarget(Muscle.CHEST, 90),
    "Cable Crossover" to ExerciseTarget(Muscle.CHEST, 60),

    // ==================================
    // Shoulder
    // ==================================
    "Shoulder Press" to ExerciseTarget(Muscle.SHOULDERS, 100),
    "Lateral Raise" to ExerciseTarget(Muscle.SHOULDERS, 30),
    "Rear Delt Fly" to ExerciseTarget(Muscle.SHOULDERS, 80),

    // ==================================
    // BICEP
    // ==================================
    "Barbell Bicep Curl" to ExerciseTarget(Muscle.BICEP, 80),
    "Dumbbell Bicep Curl" to ExerciseTarget(Muscle.BICEP, 35),
    "Hammer Curl" to ExerciseTarget(Muscle.BICEP, 40),
    "Cross-Body Hammer Curl" to ExerciseTarget(Muscle.BICEP, 35),

    // ==================================
    // TRICEP
    // ==================================
    "Weighted Tricep Dip" to ExerciseTarget(Muscle.TRICEP, 60),
    "Skullcrusher" to ExerciseTarget(Muscle.TRICEP, 60),
    "Straight Bar Pushdown" to ExerciseTarget(Muscle.TRICEP, 80),
    "Rope Tricep Pushdown" to ExerciseTarget(Muscle.TRICEP, 60),

    // ==================================
    // BACK
    // ==================================
    "Barbell Row" to ExerciseTarget(Muscle.BACK, 160),
    "Deadlift" to ExerciseTarget(Muscle.BACK, 220),
    "One-Arm Dumbbell Row" to ExerciseTarget(Muscle.BACK, 60),
    "Cable Lat Pulldown" to ExerciseTarget(Muscle.BACK, 120),
    "Seated Cable Row" to ExerciseTarget(Muscle.BACK, 120),
    "Weighted Pull Up" to ExerciseTarget(Muscle.BACK, 60),

    // ==================================
    // ABS
    // ==================================
    "Weighted Sit Up" to ExerciseTarget(Muscle.ABS, 40),
    "Machine Crunch" to ExerciseTarget(Muscle.ABS, 90),
    "Cable Crunch" to ExerciseTarget(Muscle.ABS, 90),

    // ==================================
    // HAMSTRINGS
    // ==================================
    "Romanian Deadlift" to ExerciseTarget(Muscle.HAMSTRINGS, 180),
    "Seated Leg Curl" to ExerciseTarget(Muscle.HAMSTRINGS, 100),
    "Lying Leg Curl" to ExerciseTarget(Muscle.HAMSTRINGS, 80),

    // ==================================
    // GLUTES
    // ==================================
    "Barbell Hip Thrust" to ExerciseTarget(Muscle.GLUTES, 200),
    "Glute Bridge" to ExerciseTarget(Muscle.GLUTES, 200),
    "Cable Glute Kickback" to ExerciseTarget(Muscle.GLUTES, 50),

    // ==================================
    // QUADS (PAHA DEPAN)
    // ==================================
    "Barbell Squat" to ExerciseTarget(Muscle.QUADS, 200),
    "Smith Machine Squat" to ExerciseTarget(Muscle.QUADS, 200),
    "Leg Press" to ExerciseTarget(Muscle.QUADS, 400),
    "Leg Extension" to ExerciseTarget(Muscle.QUADS, 100),
    "Bulgarian Split Squat" to ExerciseTarget(Muscle.QUADS, 100),

    // ==================================
    // CALVES (BETIS)
    // ==================================
    "Standing Calf Raise" to ExerciseTarget(Muscle.CALVES, 240),
    "Seated Calf Raise" to ExerciseTarget(Muscle.CALVES, 120)

)