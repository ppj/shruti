package com.hindustani.pitchdetector.data

/**
 * State for training mode where users practice holding swaras accurately
 *
 * @property level Training difficulty level (1 = 7 shuddha notes, 2 = all 12 notes)
 * @property currentNoteIndex Current position in the note sequence (0-based)
 * @property currentSwara Current target swara to sing (e.g., "S", "R", "g")
 * @property nextSwara Preview of next swara in sequence, null if on last note
 * @property holdProgress Progress of holding current note (0.0f to 1.0f, representing 0-5 seconds)
 * @property isHoldingCorrectly Whether user is currently singing the correct pitch within tolerance
 * @property isSessionComplete Whether all notes in the level have been completed successfully
 * @property countdown Initial countdown before training starts (3, 2, 1, or 0 if started)
 */
data class TrainingState(
    val level: Int = 1,
    val currentNoteIndex: Int = 0,
    val currentSwara: String? = null,
    val nextSwara: String? = null,
    val holdProgress: Float = 0f,
    val isHoldingCorrectly: Boolean = false,
    val isSessionComplete: Boolean = false,
    val countdown: Int = 3,
)
