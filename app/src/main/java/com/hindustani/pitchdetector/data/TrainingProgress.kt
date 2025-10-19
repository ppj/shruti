package com.hindustani.pitchdetector.data

/**
 * Represents the user's progress in training exercises.
 */
data class TrainingProgress(
    val currentLevel: Int = DEFAULT_CURRENT_LEVEL,
    val completedExercises: Int = DEFAULT_COMPLETED_EXERCISES,
    val totalScore: Int = DEFAULT_TOTAL_SCORE,
    val unlockedSwaras: Set<String> = DEFAULT_UNLOCKED_SWARAS,
) {
    companion object {
        // Default values
        const val DEFAULT_CURRENT_LEVEL = 1
        const val DEFAULT_COMPLETED_EXERCISES = 0
        const val DEFAULT_TOTAL_SCORE = 0
        val DEFAULT_UNLOCKED_SWARAS: Set<String> = setOf("S", "R", "G", "M", "P", "D", "N")
    }
}
