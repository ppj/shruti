package com.hindustani.pitchdetector.data

/**
 * Represents the current state of a training exercise.
 */
data class TrainingState(
    val targetNote: String? = null,
    val targetFrequency: Double? = null,
    val currentSliderFrequency: Double = 261.63,
    val detunedFrequency: Double? = null,
    val deviationCents: Double? = null,
    val isCorrect: Boolean? = null,
    val showResult: Boolean = false,
    val scoreThisRound: Int = 0,
    val startTime: Long? = null,
    val currentLevel: Int = 1,
    val toleranceCents: Double = 20.0,
    val successfulExercisesThisLevel: Int = 0,
)
