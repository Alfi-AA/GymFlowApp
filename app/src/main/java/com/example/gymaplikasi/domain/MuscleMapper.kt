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
    val targetMaxMale: Int, // batas beban 100% untuk laki2 dalam kg
    val targetMaxFemale: Int // batas beban 100% untuk perempuan dalam kg
)

// 4. Kamus Pemetaan Latihan ke Otot
val exerciseToMuscleMap = mapOf<String, ExerciseTarget>(

    // ==================================
    // Chest (Rasio Wanita 55-60%)
    // ==================================
    "Barbell Bench Press" to ExerciseTarget(Muscle.CHEST, 140, 80),
    "Incline Bench Press" to ExerciseTarget(Muscle.CHEST, 110, 60),
    "Pec Deck Fly" to ExerciseTarget(Muscle.CHEST, 90, 50),
    "Cable Crossover" to ExerciseTarget(Muscle.CHEST, 60, 35),

    // ==================================
    // Shoulder (Rasio Wanita 50-55%)
    // ==================================
    "Shoulder Press" to ExerciseTarget(Muscle.SHOULDERS, 100, 55),
    "Lateral Raise" to ExerciseTarget(Muscle.SHOULDERS, 30, 15),
    "Rear Delt Fly" to ExerciseTarget(Muscle.SHOULDERS, 80, 40),

    // ==================================
    // BICEP (Rasio Wanita 50-55%)
    // ==================================
    "Barbell Bicep Curl" to ExerciseTarget(Muscle.BICEP, 80, 40),
    "Dumbbell Bicep Curl" to ExerciseTarget(Muscle.BICEP, 35, 17),
    "Hammer Curl" to ExerciseTarget(Muscle.BICEP, 40, 20),
    "Cross-Body Hammer Curl" to ExerciseTarget(Muscle.BICEP, 35, 17),

    // ==================================
    // TRICEP (Rasio Wanita 55%)
    // ==================================
    "Weighted Tricep Dip" to ExerciseTarget(Muscle.TRICEP, 60, 30),
    "Skullcrusher" to ExerciseTarget(Muscle.TRICEP, 60, 30),
    "Straight Bar Pushdown" to ExerciseTarget(Muscle.TRICEP, 80, 45),
    "Rope Tricep Pushdown" to ExerciseTarget(Muscle.TRICEP, 60, 35),

    // ==================================
    // BACK (Rasio Wanita 60%)
    // ==================================
    "Barbell Row" to ExerciseTarget(Muscle.BACK, 160,95),
    "Deadlift" to ExerciseTarget(Muscle.BACK, 220, 130),
    "One-Arm Dumbbell Row" to ExerciseTarget(Muscle.BACK, 60, 35),
    "Cable Lat Pulldown" to ExerciseTarget(Muscle.BACK, 120, 70),
    "Seated Cable Row" to ExerciseTarget(Muscle.BACK, 120, 70),
    "Weighted Pull Up" to ExerciseTarget(Muscle.BACK, 60, 30),

    // ==================================
    // ABS (Rasio Wanita 70-75%)
    // ==================================
    "Weighted Sit Up" to ExerciseTarget(Muscle.ABS, 40, 30),
    "Machine Crunch" to ExerciseTarget(Muscle.ABS, 90, 65),
    "Cable Crunch" to ExerciseTarget(Muscle.ABS, 90, 65),

    // ==================================
    // HAMSTRINGS (Rasio Wanita 65-70%)
    // ==================================
    "Romanian Deadlift" to ExerciseTarget(Muscle.HAMSTRINGS, 180, 115),
    "Seated Leg Curl" to ExerciseTarget(Muscle.HAMSTRINGS, 100, 65),
    "Lying Leg Curl" to ExerciseTarget(Muscle.HAMSTRINGS, 80, 50),

    // ==================================
    // GLUTES (Rasio Wanita 75-80%)
    // ==================================
    "Barbell Hip Thrust" to ExerciseTarget(Muscle.GLUTES, 200, 150),
    "Glute Bridge" to ExerciseTarget(Muscle.GLUTES, 200, 150),
    "Cable Glute Kickback" to ExerciseTarget(Muscle.GLUTES, 50, 35),

    // ==================================
    // QUADS (PAHA DEPAN) (Rasio Wanita 65-70%)
    // ==================================
    "Barbell Squat" to ExerciseTarget(Muscle.QUADS, 200, 130),
    "Smith Machine Squat" to ExerciseTarget(Muscle.QUADS, 200, 130),
    "Leg Press" to ExerciseTarget(Muscle.QUADS, 400, 260),
    "Leg Extension" to ExerciseTarget(Muscle.QUADS, 100, 65),
    "Bulgarian Split Squat" to ExerciseTarget(Muscle.QUADS, 100, 65),

    // ==================================
    // CALVES (BETIS) (Rasio Wanita 70%)
    // ==================================
    "Standing Calf Raise" to ExerciseTarget(Muscle.CALVES, 240, 160),
    "Seated Calf Raise" to ExerciseTarget(Muscle.CALVES, 120, 80)

)