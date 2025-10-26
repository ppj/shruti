package com.hindustani.pitchdetector.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
 * ViewModel for training mode where users practice holding swars accurately
 *
 * @param level Training difficulty level:
 *   - 1 = 7 shuddha notes sequential
 *   - 2 = 7 shuddha notes randomized
 *   - 3 = 12 notes sequential
 *   - 4 = 12 notes randomized
 * @param pitchViewModel Main PitchViewModel instance for accessing pitch data and controlling audio
 */
class TrainingViewModel(
    private val level: Int,
    private val pitchViewModel: PitchViewModel,
) : ViewModel() {
    companion object {
        private const val HOLD_DURATION_MILLIS = 2000L
        private const val COUNTDOWN_START = 3
        private const val TIMER_UPDATE_INTERVAL = 16L // ~60fps
        private const val TRAINING_TANPURA_STRING_1 = "P" // Hardcode to Pa for training

        private val LEVEL_1_NOTES = listOf("S", "R", "G", "m", "P", "D", "N")
        private val LEVEL_2_NOTES = listOf("S", "r", "R", "g", "G", "m", "M", "P", "d", "D", "n", "N")

        private const val BASE_POINTS = 100
        private const val ACCURACY_BONUS_BASE = 50

        private const val STAR_THRESHOLD_THREE = 0.85f
        private const val STAR_THRESHOLD_TWO = 0.60f

        /**
         * Factory for creating TrainingViewModel with dependencies
         */
        fun provideFactory(
            level: Int,
            pitchViewModel: PitchViewModel,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(TrainingViewModel::class.java)) {
                        return TrainingViewModel(level, pitchViewModel) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }

    private val _state = MutableStateFlow(TrainingState(level = level, countdown = COUNTDOWN_START))
    val state: StateFlow<TrainingState> = _state.asStateFlow()

    // Expose tanpura playing state and user's Sa from PitchViewModel
    val isTanpuraPlaying: StateFlow<Boolean> = pitchViewModel.isTanpuraPlaying
    val saNote: StateFlow<String> =
        pitchViewModel.pitchState
            .map { it.saNote }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = "C3",
            )

    private var noteSequence: List<String> = emptyList()
    private var holdTimerJob: Job? = null
    private var wasPerfectThroughout: Boolean = true

    /**
     * Generate note sequence based on level (randomized levels get new shuffle each time)
     */
    private fun generateNoteSequence(): List<String> =
        when (level) {
            1 -> LEVEL_1_NOTES // 7 shuddha notes sequential
            2 -> LEVEL_1_NOTES.shuffled() // 7 shuddha notes randomized (new shuffle each call)
            3 -> LEVEL_2_NOTES // 12 notes sequential
            4 -> LEVEL_2_NOTES.shuffled() // 12 notes randomized (new shuffle each call)
            else -> LEVEL_1_NOTES // Default to level 1
        }

    /**
     * Calculate maximum possible score for the current level
     * Max score assumes all notes are perfect with maximum combo multiplier
     */
    private fun calculateMaxScore(noteCount: Int): Int {
        var total = 0
        for (i in 1..noteCount) {
            total += BASE_POINTS + (ACCURACY_BONUS_BASE * i)
        }
        return total
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

        // Set tanpura String 1 to Pa (P) for training
        pitchViewModel.updateTanpuraString1(TRAINING_TANPURA_STRING_1)

        // Auto-start tanpura if not already playing
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
                // Only track pitch after countdown finishes
                if (_state.value.countdown > 0) return@onEach

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
                    if (holdTimerJob?.isActive != true) {
                        startHoldTimer()
                    }
                } else {
                    if (holdTimerJob?.isActive == true) {
                        wasPerfectThroughout = false
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
        wasPerfectThroughout = true
        holdTimerJob =
            viewModelScope.launch {
                val startTime = System.currentTimeMillis()
                while (System.currentTimeMillis() - startTime < HOLD_DURATION_MILLIS) {
                    val elapsedMillis = System.currentTimeMillis() - startTime
                    val progress = (elapsedMillis.toFloat() / HOLD_DURATION_MILLIS).coerceIn(0f, 1f)
                    _state.update { it.copy(holdProgress = progress) }
                    delay(TIMER_UPDATE_INTERVAL)
                }
                // Ensure progress reaches exactly 1.0 before advancing
                _state.update { it.copy(holdProgress = 1f) }
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
     * Advance to the next note in the sequence, or complete the session
     */
    private fun advanceToNextNote() {
        val currentState = _state.value
        var newScore = currentState.currentScore + BASE_POINTS
        var newCombo = currentState.comboCount

        if (wasPerfectThroughout) {
            newCombo++
            newScore += ACCURACY_BONUS_BASE * newCombo
        } else {
            newCombo = 0
        }

        resetHoldTimer()
        val currentIndex = currentState.currentNoteIndex
        val nextIndex = currentIndex + 1

        if (nextIndex < noteSequence.size) {
            _state.update {
                it.copy(
                    currentNoteIndex = nextIndex,
                    currentSwar = noteSequence[nextIndex],
                    holdProgress = 0f,
                    currentScore = newScore,
                    comboCount = newCombo,
                    wasLastNotePerfect = wasPerfectThroughout,
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
                    wasLastNotePerfect = wasPerfectThroughout,
                )
            }

            if (pitchViewModel.isRecording.value) {
                pitchViewModel.toggleRecording()
            }

            if (pitchViewModel.isTanpuraPlaying.value) {
                pitchViewModel.toggleTanpura()
            }
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
                level = level,
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
        // Ensure String 1 is set to Pa before toggling
        pitchViewModel.updateTanpuraString1(TRAINING_TANPURA_STRING_1)
        pitchViewModel.toggleTanpura()
    }

    /**
     * Clean up resources when ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        holdTimerJob?.cancel()

        if (pitchViewModel.isRecording.value) {
            pitchViewModel.toggleRecording()
        }

        if (pitchViewModel.isTanpuraPlaying.value) {
            pitchViewModel.toggleTanpura()
        }
    }
}
