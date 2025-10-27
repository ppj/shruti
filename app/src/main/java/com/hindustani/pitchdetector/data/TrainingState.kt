package com.hindustani.pitchdetector.data

/**
 * State for training mode where users practice holding swars accurately
 *
 * @property level Training difficulty level (1 = 7 shuddha notes, 2 = all 12 notes)
 * @property currentNoteIndex Current position in the note sequence (0-based)
 * @property currentSwar Current target swar to sing (e.g., "S", "R", "g")
 * @property holdProgress Progress of holding current note (0.0f to 1.0f, representing 0-5 seconds)
 * @property isHoldingCorrectly Whether user is currently singing the correct pitch within tolerance
 * @property isSessionComplete Whether all notes in the level have been completed successfully
 * @property countdown Initial countdown before training starts (3, 2, 1, or 0 if started)
 * @property detectedSwar The swar user is currently singing (may differ from currentSwar)
 * @property isFlat Whether user is singing the correct swar but below tolerance (too flat)
 * @property isSharp Whether user is singing the correct swar but above tolerance (too sharp)
 * @property currentScore Current score for this session
 * @property comboCount Current streak of consecutive perfect notes
 * @property sessionBestScore Best score achieved in this session (resets on navigation away)
 * @property earnedStars Number of stars earned (1-3) based on score percentage
 */
data class TrainingState(
    val level: Int = 1,
    val currentNoteIndex: Int = 0,
    val currentSwar: String? = null,
    val holdProgress: Float = 0f,
    val isHoldingCorrectly: Boolean = false,
    val isSessionComplete: Boolean = false,
    val countdown: Int = 3,
    val detectedSwar: String? = null,
    val isFlat: Boolean = false,
    val isSharp: Boolean = false,
    val currentScore: Int = 0,
    val comboCount: Int = 0,
    val sessionBestScore: Int = 0,
    val earnedStars: Int = 0,
)
