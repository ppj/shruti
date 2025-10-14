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
     * User is recording their natural speaking voice
     */
    object RecordingSpeech : FindSaState()

    /**
     * User is actively recording their vocal singing range
     */
    object RecordingSinging : FindSaState()

    /**
     * Processing and analyzing the recorded data
     */
    object Analyzing : FindSaState()

    /**
     * Test complete with recommendation
     * @param originalSa The originally calculated ideal Sa note (never changes)
     * @param recommendedSa The current Sa recommendation (can be adjusted by user)
     * @param lowestNote The lowest comfortable note detected
     * @param highestNote The highest comfortable note detected
     * @param speakingPitch The detected average speaking pitch (null if speech phase skipped)
     */
    data class Finished(
        val originalSa: Note,
        val recommendedSa: Note,
        val lowestNote: Note,
        val highestNote: Note,
        val speakingPitch: Note? = null
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
