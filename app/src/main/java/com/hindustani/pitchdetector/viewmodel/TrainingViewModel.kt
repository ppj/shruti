package com.hindustani.pitchdetector.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hindustani.pitchdetector.audio.StringPluckPlayer
import com.hindustani.pitchdetector.data.TrainingProgress
import com.hindustani.pitchdetector.data.TrainingProgressRepository
import com.hindustani.pitchdetector.data.TrainingState
import com.hindustani.pitchdetector.data.UserSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * ViewModel for training mode state management.
 * Manages ear training exercises where users tune a virtual string to match a target note.
 */
class TrainingViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val BASE_POINTS = 100
        private const val MAX_TIME_BONUS_SECONDS = 30
        private const val MIN_TIME_BONUS_MULTIPLIER = 0.5
        private const val MAX_TIME_BONUS_MULTIPLIER = 1.5
        private const val EXERCISES_PER_LEVEL = 10

        // Level configurations
        private const val LEVEL_1_TOLERANCE = 20.0
        private const val LEVEL_2_TOLERANCE = 10.0
        private const val LEVEL_3_TOLERANCE = 5.0

        // Detuning range (cents)
        private const val MIN_DETUNE_CENTS = 30.0
        private const val MAX_DETUNE_CENTS = 80.0

        // Swaras by level
        private val LEVEL_1_SWARAS = listOf("S", "R", "G", "M", "P", "D", "N")
        private val CHROMATIC_SWARAS = listOf("S", "r", "R", "g", "G", "m", "M", "P", "d", "D", "n", "N")
    }

    private val stringPluckPlayer = StringPluckPlayer()
    private val trainingProgressRepository = TrainingProgressRepository(application.applicationContext)
    private val userSettingsRepository = UserSettingsRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(TrainingState())
    val uiState: StateFlow<TrainingState> = _uiState.asStateFlow()

    private val _progress = MutableStateFlow(TrainingProgress())
    val progress: StateFlow<TrainingProgress> = _progress.asStateFlow()

    private var userSaFrequency: Double = 261.63 // Default to C4

    init {
        viewModelScope.launch {
            // Load user's Sa frequency
            val userSettings = userSettingsRepository.userSettings.first()
            userSaFrequency = userSettings.saFrequency

            // Load training progress
            val trainingProgress = trainingProgressRepository.trainingProgress.first()
            _progress.value = trainingProgress

            // Initialize state with current level and tolerance
            val tolerance = getToleranceForLevel(trainingProgress.currentLevel)
            _uiState.update {
                it.copy(
                    currentLevel = trainingProgress.currentLevel,
                    toleranceCents = tolerance,
                )
            }

            // Generate first exercise
            generateExercise()
        }

        // Observe training progress changes
        viewModelScope.launch {
            trainingProgressRepository.trainingProgress.collect { progress ->
                _progress.value = progress
            }
        }
    }

    /**
     * Generate a new training exercise with random target note and detuned frequency.
     */
    private fun generateExercise() {
        val currentLevel = _uiState.value.currentLevel
        val availableSwaras = getAvailableSwaras(currentLevel)

        // Select random target note
        val targetNote = availableSwaras.random()

        // Calculate target frequency using HindustaniNoteConverter
        val targetFrequency = calculateFrequencyForSwara(targetNote, userSaFrequency)

        // Generate random detuning (±30 to ±80 cents, avoiding ±0)
        val detuneCents =
            if (Random.nextBoolean()) {
                Random.nextDouble(MIN_DETUNE_CENTS, MAX_DETUNE_CENTS)
            } else {
                -Random.nextDouble(MIN_DETUNE_CENTS, MAX_DETUNE_CENTS)
            }

        // Calculate detuned frequency: f_new = f_target * 2^(cents/1200)
        val detunedFrequency = targetFrequency * 2.0.pow(detuneCents / 1200.0)

        // Initialize slider to detuned frequency
        val startTime = System.currentTimeMillis()

        _uiState.update {
            it.copy(
                targetNote = targetNote,
                targetFrequency = targetFrequency,
                currentSliderFrequency = detunedFrequency,
                detunedFrequency = detunedFrequency,
                deviationCents = null,
                isCorrect = null,
                showResult = false,
                scoreThisRound = 0,
                startTime = startTime,
            )
        }

        // Auto-play the detuned note
        stringPluckPlayer.play(detunedFrequency)
    }

    /**
     * Handle slider frequency change.
     */
    fun onSliderChanged(frequency: Double) {
        _uiState.update {
            it.copy(currentSliderFrequency = frequency)
        }
    }

    /**
     * Replay the out-of-tune note.
     */
    fun onReplayNote() {
        val detunedFrequency = _uiState.value.detunedFrequency
        if (detunedFrequency != null) {
            stringPluckPlayer.play(detunedFrequency)
        }
    }

    /**
     * Check the user's tuning attempt and calculate score.
     */
    fun onCheckTuning() {
        val state = _uiState.value
        val targetFrequency = state.targetFrequency ?: return
        val sliderFrequency = state.currentSliderFrequency
        val startTime = state.startTime ?: return

        // Calculate deviation in cents: cents = 1200 * log2(f_slider / f_target)
        val deviationCents = 1200.0 * kotlin.math.log2(sliderFrequency / targetFrequency)

        // Determine if correct (within tolerance)
        val isCorrect = abs(deviationCents) <= state.toleranceCents

        // Calculate score
        val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0
        val score = calculateScore(deviationCents, state.toleranceCents, elapsedSeconds)

        _uiState.update {
            it.copy(
                deviationCents = deviationCents,
                isCorrect = isCorrect,
                showResult = true,
                scoreThisRound = score,
            )
        }
    }

    /**
     * Calculate score based on accuracy and time.
     * Formula: basePoints * accuracyMultiplier * timeBonus
     */
    private fun calculateScore(
        deviationCents: Double,
        toleranceCents: Double,
        elapsedSeconds: Double,
    ): Int {
        // Accuracy multiplier: 0 if outside tolerance, linear within tolerance
        val accuracyMultiplier =
            if (abs(deviationCents) > toleranceCents) {
                0.0
            } else {
                kotlin.math.max(0.0, 1.0 - abs(deviationCents) / toleranceCents)
            }

        // Time bonus: 1.5x for fast (<20s), 1.0x at 30s, 0.5x minimum
        val timeBonus =
            kotlin.math.max(
                MIN_TIME_BONUS_MULTIPLIER,
                MAX_TIME_BONUS_MULTIPLIER - (elapsedSeconds / MAX_TIME_BONUS_SECONDS),
            )

        return (BASE_POINTS * accuracyMultiplier * timeBonus).roundToInt()
    }

    /**
     * Move to next exercise, save progress, and check for level up.
     */
    fun onNextExercise() {
        val state = _uiState.value
        val isCorrect = state.isCorrect ?: false

        viewModelScope.launch {
            // Save score
            if (state.scoreThisRound > 0) {
                trainingProgressRepository.addToScore(state.scoreThisRound)
            }

            // Increment exercises if successful
            if (isCorrect) {
                trainingProgressRepository.incrementExercises()

                val newSuccessCount = state.successfulExercisesThisLevel + 1

                // Check for level up
                if (newSuccessCount >= EXERCISES_PER_LEVEL) {
                    val newLevel = state.currentLevel + 1
                    val newTolerance = getToleranceForLevel(newLevel)

                    trainingProgressRepository.updateCurrentLevel(newLevel)

                    // Unlock chromatic swaras when advancing to level 2
                    if (newLevel == 2) {
                        trainingProgressRepository.updateUnlockedSwaras(CHROMATIC_SWARAS.toSet())
                    }

                    _uiState.update {
                        it.copy(
                            currentLevel = newLevel,
                            toleranceCents = newTolerance,
                            successfulExercisesThisLevel = 0,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(successfulExercisesThisLevel = newSuccessCount)
                    }
                }
            }

            // Generate new exercise
            generateExercise()
        }
    }

    /**
     * Get tolerance in cents for a given level.
     */
    private fun getToleranceForLevel(level: Int): Double {
        return when (level) {
            1 -> LEVEL_1_TOLERANCE
            2 -> LEVEL_2_TOLERANCE
            else -> LEVEL_3_TOLERANCE
        }
    }

    /**
     * Get available swaras for a given level.
     */
    private fun getAvailableSwaras(level: Int): List<String> {
        return if (level == 1) {
            LEVEL_1_SWARAS
        } else {
            CHROMATIC_SWARAS
        }
    }

    /**
     * Calculate frequency for a swara relative to Sa.
     */
    private fun calculateFrequencyForSwara(
        swara: String,
        saFrequency: Double,
    ): Double {
        // Use HindustaniNoteConverter's note ratios
        val noteRatios =
            mapOf(
                "S" to 1.0 / 1.0,
                "r" to 16.0 / 15.0,
                "R" to 9.0 / 8.0,
                "g" to 6.0 / 5.0,
                "G" to 5.0 / 4.0,
                "m" to 4.0 / 3.0,
                "M" to 45.0 / 32.0,
                "P" to 3.0 / 2.0,
                "d" to 8.0 / 5.0,
                "D" to 5.0 / 3.0,
                "n" to 16.0 / 9.0,
                "N" to 15.0 / 8.0,
            )

        val ratio = noteRatios[swara] ?: 1.0
        return saFrequency * ratio
    }

    /**
     * Clean up resources.
     */
    override fun onCleared() {
        super.onCleared()
        stringPluckPlayer.stop()
    }
}
