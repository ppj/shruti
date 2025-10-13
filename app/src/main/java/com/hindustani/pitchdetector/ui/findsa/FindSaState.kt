package com.hindustani.pitchdetector.ui.findsa

/**
 * Represents a musical note with its name and frequency
 */
data class Note(
    val name: String,        // e.g., "C3", "G#3"
    val frequency: Double    // in Hz
)

/**
 * Sealed class representing the different states of the Find Sa feature
 */
sealed class FindSaState {
    /**
     * Initial state - user hasn't started the test yet
     */
    object NotStarted : FindSaState()

    /**
     * User is actively recording their vocal range
     */
    object Recording : FindSaState()

    /**
     * Processing and analyzing the recorded data
     */
    object Analyzing : FindSaState()

    /**
     * Test complete with recommendation
     * @param recommendedSa The calculated ideal Sa note for the user
     * @param lowestNote The lowest comfortable note detected
     * @param highestNote The highest comfortable note detected
     */
    data class Finished(
        val recommendedSa: Note,
        val lowestNote: Note,
        val highestNote: Note
    ) : FindSaState()
}

/**
 * UI state container for the Find Sa feature
 */
data class FindSaUiState(
    val currentState: FindSaState = FindSaState.NotStarted,
    val currentPitch: Float = 0f,  // Real-time pitch feedback during recording (in Hz)
    val collectedSamplesCount: Int = 0,  // Number of valid samples collected
    val error: String? = null
)
