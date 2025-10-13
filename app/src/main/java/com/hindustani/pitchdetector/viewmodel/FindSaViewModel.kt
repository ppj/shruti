package com.hindustani.pitchdetector.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hindustani.pitchdetector.audio.AudioCaptureManager
import com.hindustani.pitchdetector.audio.PYINDetector
import com.hindustani.pitchdetector.audio.TanpuraPlayer
import com.hindustani.pitchdetector.ui.findsa.FindSaState
import com.hindustani.pitchdetector.ui.findsa.FindSaUiState
import com.hindustani.pitchdetector.ui.findsa.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.pow

/**
 * ViewModel for the Find Your Sa feature
 * Helps users discover their ideal Sa (tonic) note through vocal range analysis
 */
class FindSaViewModel(application: Application) : AndroidViewModel(application) {

    private val audioCapture = AudioCaptureManager()
    private val pitchDetector = PYINDetector()
    private val tanpuraPlayer = TanpuraPlayer(application.applicationContext)

    private val _uiState = MutableStateFlow(FindSaUiState())
    val uiState: StateFlow<FindSaUiState> = _uiState.asStateFlow()

    // Temporary storage for collected pitch samples during recording
    private val collectedPitches = mutableListOf<Float>()
    private var recordingJob: Job? = null

    // Minimum confidence threshold for accepting pitch samples (stricter than display threshold)
    private val confidenceThreshold = 0.8f

    // Standard Sa notes with their frequencies
    // These are the typical Sa recommendations for different voice types
    private val standardSaNotes = mapOf(
        "G#2" to 103.83,
        "A2" to 110.00,
        "A#2" to 116.54,
        "B2" to 123.47,
        "C3" to 130.81,   // Male bass/heavy
        "C#3" to 138.59,  // Male baritone
        "D3" to 146.83,   // Male tenor
        "D#3" to 155.56,
        "E3" to 164.81,
        "F3" to 174.61,
        "F#3" to 185.00,
        "G3" to 196.00,   // Female alto/contralto
        "G#3" to 207.65,  // Female mezzo-soprano
        "A3" to 220.00,   // Female soprano
        "A#3" to 233.08,
        "B3" to 246.94
    )

    /**
     * Start the vocal range test
     * Begins recording and collecting pitch samples
     */
    fun startTest() {
        // Clear previous data
        collectedPitches.clear()

        // Update state to Recording
        _uiState.update { it.copy(
            currentState = FindSaState.Recording,
            collectedSamplesCount = 0,
            error = null
        )}

        // Start audio capture
        recordingJob = audioCapture.startCapture { audioData ->
            viewModelScope.launch(Dispatchers.Default) {
                processAudioData(audioData)
            }
        }
    }

    /**
     * Stop the vocal range test and analyze the collected data
     */
    fun stopTest() {
        // Stop audio capture
        audioCapture.stop()
        recordingJob?.cancel()
        recordingJob = null

        // Update state to Analyzing
        _uiState.update { it.copy(currentState = FindSaState.Analyzing) }

        // Analyze the collected pitches
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val result = analyzePitches(collectedPitches)
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(currentState = result) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(
                        currentState = FindSaState.NotStarted,
                        error = "Unable to analyze: ${e.message}"
                    )}
                }
            }
        }
    }

    /**
     * Process incoming audio data and collect valid pitch samples
     */
    private suspend fun processAudioData(audioData: FloatArray) {
        val pitchResult = pitchDetector.detectPitch(audioData)

        if (pitchResult.frequency != null && pitchResult.confidence > confidenceThreshold) {
            val frequency = pitchResult.frequency

            // Only accept frequencies in reasonable vocal range (80-1000 Hz)
            if (frequency in 80f..1000f) {
                collectedPitches.add(frequency)

                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(
                        currentPitch = frequency,
                        collectedSamplesCount = collectedPitches.size
                    )}
                }
            }
        }
    }

    /**
     * Analyze collected pitches and calculate the recommended Sa
     * @return FindSaState.Finished with the recommendation, or throws exception if insufficient data
     */
    private fun analyzePitches(pitches: List<Float>): FindSaState.Finished {
        if (pitches.isEmpty()) {
            throw IllegalStateException("No valid pitches recorded. Please try again.")
        }

        if (pitches.size < 20) {
            throw IllegalStateException("Insufficient data (${pitches.size} samples). Please hold notes longer.")
        }

        // Sort pitches
        val sortedPitches = pitches.sorted()

        // Remove outliers: discard bottom 10% and top 10%
        val outlierCutoff = (sortedPitches.size * 0.1).toInt()
        val filteredPitches = if (sortedPitches.size > 20) {
            sortedPitches.subList(outlierCutoff, sortedPitches.size - outlierCutoff)
        } else {
            sortedPitches
        }

        // Find minimum and maximum comfortable frequencies
        val lowestFreq = filteredPitches.first().toDouble()
        val highestFreq = filteredPitches.last().toDouble()

        // Calculate ideal Sa: 7 semitones above the lowest comfortable note
        // Formula: freq_new = freq_old × 2^(semitones/12)
        val idealSaFreq = lowestFreq * 2.0.pow(7.0 / 12.0)

        // Snap to nearest standard Sa note
        val recommendedSa = snapToNearestNote(idealSaFreq)
        val lowestNote = snapToNearestNote(lowestFreq)
        val highestNote = snapToNearestNote(highestFreq)

        return FindSaState.Finished(
            originalSa = recommendedSa,
            recommendedSa = recommendedSa,
            lowestNote = lowestNote,
            highestNote = highestNote
        )
    }

    /**
     * Snap a frequency to the nearest standard Sa note
     */
    private fun snapToNearestNote(frequency: Double): Note {
        var closestNote = standardSaNotes.entries.first()
        var minDifference = abs(frequency - closestNote.value)

        for (entry in standardSaNotes.entries) {
            val difference = abs(frequency - entry.value)
            if (difference < minDifference) {
                minDifference = difference
                closestNote = entry
            }
        }

        return Note(name = closestNote.key, frequency = closestNote.value)
    }

    /**
     * Convert a note name to its frequency
     */
    private fun noteToFrequency(noteName: String): Double {
        return standardSaNotes[noteName] ?: throw IllegalArgumentException("Unknown note: $noteName")
    }

    /**
     * Adjust the recommended Sa by a number of semitones
     * @param semitones Number of semitones to adjust (positive = higher, negative = lower)
     */
    fun adjustRecommendation(semitones: Int) {
        val currentState = _uiState.value.currentState
        if (currentState is FindSaState.Finished) {
            val currentFreq = currentState.recommendedSa.frequency
            val adjustedFreq = currentFreq * 2.0.pow(semitones / 12.0)
            val adjustedNote = snapToNearestNote(adjustedFreq)

            _uiState.update { it.copy(
                currentState = currentState.copy(
                    recommendedSa = adjustedNote
                    // originalSa remains unchanged
                )
            )}
        }
    }

    /**
     * Play the recommended Sa note using the tanpura
     */
    fun playRecommendedSa() {
        val currentState = _uiState.value.currentState
        if (currentState is FindSaState.Finished) {
            tanpuraPlayer.start(
                saFreq = currentState.recommendedSa.frequency,
                string1 = "P",
                vol = 0.5f
            )
        }
    }

    /**
     * Stop playing the tanpura
     */
    fun stopPlaying() {
        tanpuraPlayer.stop()
    }

    /**
     * Reset the test to initial state
     */
    fun resetTest() {
        audioCapture.stop()
        recordingJob?.cancel()
        recordingJob = null
        tanpuraPlayer.stop()
        collectedPitches.clear()

        _uiState.update { FindSaUiState() }
    }

    /**
     * Get the recommended Sa as a western note string (for updating app settings)
     */
    fun getRecommendedSaNote(): String? {
        val currentState = _uiState.value.currentState
        return if (currentState is FindSaState.Finished) {
            currentState.recommendedSa.name
        } else {
            null
        }
    }

    /**
     * Clean up resources
     */
    override fun onCleared() {
        super.onCleared()
        audioCapture.stop()
        recordingJob?.cancel()
        tanpuraPlayer.stop()
    }
}
