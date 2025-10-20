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
 * ViewModel for training mode where users practice holding swaras accurately
 *
 * @param level Training difficulty level (1 = 7 shuddha notes, 2 = all 12 notes)
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

    private val noteSequence: List<String> = if (level == 1) LEVEL_1_NOTES else LEVEL_2_NOTES
    private var holdTimerJob: Job? = null

    init {
        initializeSession()
        startCountdown()
        observePitch()
    }

    /**
     * Initialize the training session with first note and start tanpura
     */
    private fun initializeSession() {
        _state.update {
            it.copy(
                currentSwara = noteSequence.first(),
                nextSwara = noteSequence.getOrNull(1),
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

            // Start recording after countdown
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

                val currentTarget = _state.value.currentSwara
                val detectedNote = pitchState.currentNote

                // Check if detected note matches target and is within tolerance
                val isCorrect = detectedNote?.swara == currentTarget && detectedNote?.isPerfect == true

                _state.update { it.copy(isHoldingCorrectly = isCorrect) }

                if (isCorrect) {
                    // Start hold timer if not already running
                    if (holdTimerJob?.isActive != true) {
                        startHoldTimer()
                    }
                } else {
                    // Reset timer if pitch becomes incorrect
                    resetHoldTimer()
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Start the 5-second hold timer, updating progress at 60fps
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
        resetHoldTimer()
        val currentIndex = _state.value.currentNoteIndex
        val nextIndex = currentIndex + 1

        if (nextIndex < noteSequence.size) {
            // Move to next note
            _state.update {
                it.copy(
                    currentNoteIndex = nextIndex,
                    currentSwara = noteSequence[nextIndex],
                    nextSwara = noteSequence.getOrNull(nextIndex + 1),
                    holdProgress = 0f,
                )
            }
        } else {
            // Session complete
            _state.update {
                it.copy(
                    isSessionComplete = true,
                    holdProgress = 0f,
                )
            }

            // Stop recording when session completes
            if (pitchViewModel.isRecording.value) {
                pitchViewModel.toggleRecording()
            }

            // Stop tanpura when session completes
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
        _state.value = TrainingState(level = level, countdown = COUNTDOWN_START)
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

        // Stop recording if active
        if (pitchViewModel.isRecording.value) {
            pitchViewModel.toggleRecording()
        }

        // Stop tanpura when leaving training mode
        if (pitchViewModel.isTanpuraPlaying.value) {
            pitchViewModel.toggleTanpura()
        }
    }
}
