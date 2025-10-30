package com.hindustani.pitchdetector.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hindustani.pitchdetector.audio.ReferenceNotePlayer
import com.hindustani.pitchdetector.data.TrainingState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Training difficulty levels with their note sequences
 *
 * @property levelNumber The level number (1-4)
 * @property baseNotes The base note sequence for this level
 * @property randomized Whether the notes should be shuffled each time
 */
enum class TrainingLevel(val levelNumber: Int, val baseNotes: List<String>, val randomized: Boolean) {
    LEVEL_1(1, listOf("S", "R", "G", "m", "P", "D", "N"), false),
    LEVEL_2(2, listOf("S", "R", "G", "m", "P", "D", "N"), true),
    LEVEL_3(3, listOf("S", "r", "R", "g", "G", "m", "M", "P", "d", "D", "n", "N"), false),
    LEVEL_4(4, listOf("S", "r", "R", "g", "G", "m", "M", "P", "d", "D", "n", "N"), true),
    ;

    /**
     * Get the note sequence for this level (randomized if applicable)
     */
    fun getSequence(): List<String> = if (randomized) baseNotes.shuffled() else baseNotes

    companion object {
        /**
         * Get TrainingLevel from integer level number (defaults to LEVEL_1 if invalid)
         */
        fun fromInt(level: Int): TrainingLevel = values().firstOrNull { it.levelNumber == level } ?: LEVEL_1
    }
}

/**
 * ViewModel for training mode where users practice holding swars accurately
 *
 * @param level Training difficulty level (1-4, see TrainingLevel enum)
 * @param pitchViewModel Main PitchViewModel instance for accessing pitch data and controlling audio
 * @param application Application context for accessing audio resources
 */
class TrainingViewModel(
    level: Int,
    private val pitchViewModel: PitchViewModel,
    application: Application,
) : AndroidViewModel(application) {
    private val trainingLevel: TrainingLevel = TrainingLevel.fromInt(level)
    private val referenceNotePlayer = ReferenceNotePlayer(application)

    companion object {
        private const val HOLD_DURATION_MILLIS = 2000L
        private const val COUNTDOWN_START = 3
        private const val TIMER_UPDATE_INTERVAL = 16L // ~60fps
        private const val TRAINING_TANPURA_STRING_1_PA = "P"

        private const val BASE_POINTS = 100
        private const val ACCURACY_BONUS_BASE = 50

        private const val STAR_THRESHOLD_THREE = 0.85f
        private const val STAR_THRESHOLD_TWO = 0.60f

        private const val DEFAULT_SA_NOTE = "C3"

        /**
         * Factory for creating TrainingViewModel with dependencies
         */
        fun provideFactory(
            level: Int,
            pitchViewModel: PitchViewModel,
            application: Application,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(TrainingViewModel::class.java)) {
                        return TrainingViewModel(level, pitchViewModel, application) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }

    private val _state = MutableStateFlow(TrainingState(level = trainingLevel.levelNumber, countdown = COUNTDOWN_START))
    val state: StateFlow<TrainingState> = _state.asStateFlow()

    // Expose tanpura playing state and user's Sa from PitchViewModel
    val isTanpuraPlaying: StateFlow<Boolean> = pitchViewModel.isTanpuraPlaying
    val saNote: StateFlow<String> =
        pitchViewModel.pitchState
            .map { it.saNote }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DEFAULT_SA_NOTE,
            )

    private var noteSequence: List<String> = emptyList()
    private var holdTimerJob: Job? = null
    private var currentNoteEverImperfect: Boolean = false

    /**
     * Generate note sequence based on level (randomized levels get new shuffle each time)
     */
    private fun generateNoteSequence(): List<String> = trainingLevel.getSequence()

    /**
     * Calculate maximum possible score for the current level using arithmetic series formula
     * Max score assumes all notes are perfect with maximum combo multiplier
     * Formula: noteCount * BASE_POINTS + ACCURACY_BONUS_BASE * sum(1..noteCount)
     */
    private fun calculateMaxScore(noteCount: Int): Int {
        return noteCount * BASE_POINTS + ACCURACY_BONUS_BASE * (noteCount * (noteCount + 1) / 2)
    }

    /**
     * Calculate number of stars based on score percentage
     */
    private fun calculateStars(
        score: Int,
        maxScore: Int,
    ): Int {
        if (maxScore == 0) return 1
        val percentage = score.toFloat() / maxScore
        return when {
            percentage >= STAR_THRESHOLD_THREE -> 3
            percentage >= STAR_THRESHOLD_TWO -> 2
            else -> 1
        }
    }

    init {
        initializeSession()
        startCountdown()
        observePitch()
    }

    /**
     * Initialize the training session with first note and start tanpura
     */
    private fun initializeSession() {
        noteSequence = generateNoteSequence()

        _state.update {
            it.copy(
                currentSwar = noteSequence.first(),
            )
        }

        pitchViewModel.updateTanpuraString1(TRAINING_TANPURA_STRING_1_PA)

        if (!pitchViewModel.isTanpuraPlaying.value) {
            pitchViewModel.toggleTanpura()
        }
    }

    /**
     * Run countdown 3-2-1 before starting pitch tracking
     */
    private fun startCountdown() {
        viewModelScope.launch {
            for (i in COUNTDOWN_START downTo 1) {
                _state.update { it.copy(countdown = i) }
                delay(1000)
            }
            _state.update { it.copy(countdown = 0) }

            if (!pitchViewModel.isRecording.value) {
                pitchViewModel.toggleRecording()
            }
        }
    }

    /**
     * Observe pitch state from PitchViewModel and track hold progress
     */
    private fun observePitch() {
        pitchViewModel.pitchState
            .onEach { pitchState ->
                if (_state.value.countdown > 0) return@onEach // Only track pitch after countdown finishes

                val currentTarget = _state.value.currentSwar
                val detectedNote = pitchState.currentNote
                val detectedSwar = detectedNote?.swar

                val isSingingCorrectSwar = detectedSwar == currentTarget
                val isCorrect = isSingingCorrectSwar && detectedNote?.isPerfect == true
                val isFlat = isSingingCorrectSwar && detectedNote?.isFlat == true
                val isSharp = isSingingCorrectSwar && detectedNote?.isSharp == true

                _state.update {
                    it.copy(
                        detectedSwar = detectedSwar,
                        isHoldingCorrectly = isCorrect,
                        isFlat = isFlat,
                        isSharp = isSharp,
                    )
                }

                if (isCorrect) {
                    if (holdTimerJob?.isActive != true && !_state.value.isSessionComplete) {
                        startHoldTimer()
                    }
                } else {
                    if (holdTimerJob?.isActive == true) {
                        currentNoteEverImperfect = true
                    }
                    resetHoldTimer()
                }
            }.launchIn(viewModelScope)
    }

    /**
     * Start the 2-second hold timer, updating progress at 60fps
     */
    private fun startHoldTimer() {
        holdTimerJob?.cancel()
        holdTimerJob =
            viewModelScope.launch {
                val startTime = System.currentTimeMillis()
                while (System.currentTimeMillis() - startTime < HOLD_DURATION_MILLIS) {
                    val elapsedMillis = System.currentTimeMillis() - startTime
                    val progress = (elapsedMillis.toFloat() / HOLD_DURATION_MILLIS).coerceIn(0f, 1f)
                    _state.update { it.copy(holdProgress = progress) }
                    delay(TIMER_UPDATE_INTERVAL)
                }
                _state.update { it.copy(holdProgress = 1f) } // Ensure progress reaches exactly 1.0 before advancing
                advanceToNextNote()
            }
    }

    /**
     * Reset the hold timer and progress
     */
    private fun resetHoldTimer() {
        holdTimerJob?.cancel()
        holdTimerJob = null
        if (_state.value.holdProgress > 0f) {
            _state.update { it.copy(holdProgress = 0f) }
        }
    }

    /**
     * Stop audio recording and tanpura playback if active
     */
    private fun stopAudio() {
        if (pitchViewModel.isRecording.value) {
            pitchViewModel.toggleRecording()
        }
        if (pitchViewModel.isTanpuraPlaying.value) {
            pitchViewModel.toggleTanpura()
        }
    }

    /**
     * Advance to the next note in the sequence, or complete the session
     */
    private fun advanceToNextNote() {
        val currentState = _state.value
        var newScore = currentState.currentScore + BASE_POINTS
        var newCombo = currentState.comboCount

        val wasNotePerfect = !currentNoteEverImperfect
        if (wasNotePerfect) {
            newCombo++
            newScore += ACCURACY_BONUS_BASE * newCombo
        } else {
            newCombo = 0
        }

        currentNoteEverImperfect = false

        resetHoldTimer()
        val currentIndex = currentState.currentNoteIndex
        val nextIndex = currentIndex + 1

        if (nextIndex < noteSequence.size) {
            val nextSwar = noteSequence[nextIndex]
            _state.update {
                it.copy(
                    currentNoteIndex = nextIndex,
                    currentSwar = nextSwar,
                    holdProgress = 0f,
                    currentScore = newScore,
                    comboCount = newCombo,
                )
            }
        } else {
            val maxScore = calculateMaxScore(noteSequence.size)
            val stars = calculateStars(newScore, maxScore)
            val newSessionBest = maxOf(newScore, currentState.sessionBestScore)

            _state.update {
                it.copy(
                    isSessionComplete = true,
                    holdProgress = 0f,
                    currentScore = newScore,
                    comboCount = newCombo,
                    earnedStars = stars,
                    sessionBestScore = newSessionBest,
                )
            }

            stopAudio()
        }
    }

    /**
     * Reset the training session to start over
     */
    fun resetSession() {
        resetHoldTimer()
        val currentSessionBest = _state.value.sessionBestScore
        _state.value =
            TrainingState(
                level = trainingLevel.levelNumber,
                countdown = COUNTDOWN_START,
                sessionBestScore = currentSessionBest,
            )
        initializeSession()
        startCountdown()
    }

    /**
     * Toggle tanpura on/off (String 1 is always set to Pa for training)
     */
    fun toggleTanpura() {
        pitchViewModel.updateTanpuraString1(TRAINING_TANPURA_STRING_1_PA) // Ensure String 1 is set to Pa before toggling
        pitchViewModel.toggleTanpura()
    }

    /**
     * Play reference note for the current target swar
     * User can call this when they need to hear the target pitch
     */
    fun playReferenceNote() {
        val currentSwar = _state.value.currentSwar
        if (currentSwar != null) {
            val saFrequency = pitchViewModel.getSaFrequency()
            referenceNotePlayer.play(currentSwar, saFrequency)
        }
    }

    /**
     * Clean up resources when ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        holdTimerJob?.cancel()
        referenceNotePlayer.release()
        stopAudio()
    }
}
