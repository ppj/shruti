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
    private val collectedSpeechPitches = mutableListOf<Float>()
    private val collectedSingingPitches = mutableListOf<Float>()
    private var recordingJob: Job? = null

    // Minimum confidence thresholds for accepting pitch samples
    private val speechConfidenceThreshold = 0.7f  // Lower for natural speech
    private val singingConfidenceThreshold = 0.8f  // Stricter for singing

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
     * Begins with the speaking phase
     */
    fun startTest() {
        // Clear previous data
        collectedSpeechPitches.clear()
        collectedSingingPitches.clear()

        // Update state to RecordingSpeech
        _uiState.update { it.copy(
            currentState = FindSaState.RecordingSpeech,
            collectedSamplesCount = 0,
            error = null
        )}

        // Start audio capture for speech
        recordingJob = audioCapture.startCapture { audioData ->
            viewModelScope.launch(Dispatchers.Default) {
                processSpeechData(audioData)
            }
        }
    }

    /**
     * Stop the speech test and transition to singing test
     */
    fun stopSpeechTest() {
        // Stop current audio capture
        audioCapture.stop()
        recordingJob?.cancel()
        recordingJob = null

        // Update state to RecordingSinging
        _uiState.update { it.copy(
            currentState = FindSaState.RecordingSinging,
            collectedSamplesCount = 0
        )}

        // Start audio capture for singing
        recordingJob = audioCapture.startCapture { audioData ->
            viewModelScope.launch(Dispatchers.Default) {
                processSingingData(audioData)
            }
        }
    }

    /**
     * Stop the singing test and analyze the collected data
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
                val result = analyzePitches(collectedSpeechPitches, collectedSingingPitches)
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
     * Process incoming speech audio data and collect valid pitch samples
     */
    private suspend fun processSpeechData(audioData: FloatArray) {
        val pitchResult = pitchDetector.detectPitch(audioData)

        if (pitchResult.frequency != null && pitchResult.confidence > speechConfidenceThreshold) {
            val frequency = pitchResult.frequency

            // Only accept frequencies in reasonable vocal range (65-1050 Hz, C2-C6)
            // This range accommodates the full Sa range (G#2-B3) plus headroom for vocal extremes
            if (frequency in 65f..1050f) {
                collectedSpeechPitches.add(frequency)

                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(
                        currentPitch = frequency,
                        collectedSamplesCount = collectedSpeechPitches.size
                    )}
                }
            }
        }
    }

    /**
     * Process incoming singing audio data and collect valid pitch samples
     */
    private suspend fun processSingingData(audioData: FloatArray) {
        val pitchResult = pitchDetector.detectPitch(audioData)

        if (pitchResult.frequency != null && pitchResult.confidence > singingConfidenceThreshold) {
            val frequency = pitchResult.frequency

            // Only accept frequencies in reasonable vocal range (65-1050 Hz, C2-C6)
            // This range accommodates the full Sa range (G#2-B3) plus headroom for vocal extremes
            if (frequency in 65f..1050f) {
                collectedSingingPitches.add(frequency)

                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(
                        currentPitch = frequency,
                        collectedSamplesCount = collectedSingingPitches.size
                    )}
                }
            }
        }
    }

    /**
     * Analyze collected pitches and calculate the recommended Sa
     * @return FindSaState.Finished with the recommendation, or throws exception if insufficient data
     */
    private fun analyzePitches(speechPitches: List<Float>, singingPitches: List<Float>): FindSaState.Finished {
        if (singingPitches.isEmpty()) {
            throw IllegalStateException("No valid pitches recorded. Please try again.")
        }

        if (singingPitches.size < 20) {
            throw IllegalStateException("Insufficient data (${singingPitches.size} samples). Please hold notes longer.")
        }

        // Process singing pitches
        val sortedSingingPitches = singingPitches.sorted()
        val outlierCutoff = (sortedSingingPitches.size * 0.1).toInt()
        val filteredSingingPitches = if (sortedSingingPitches.size > 20) {
            sortedSingingPitches.subList(outlierCutoff, sortedSingingPitches.size - outlierCutoff)
        } else {
            sortedSingingPitches
        }

        // Find minimum and maximum comfortable frequencies
        val lowestFreq = filteredSingingPitches.first().toDouble()
        val highestFreq = filteredSingingPitches.last().toDouble()

        // Calculate Sa from singing: 7 semitones above the lowest comfortable note
        val saFromSinging = lowestFreq * 2.0.pow(7.0 / 12.0)

        // Process speech pitches if available
        val saFromSpeaking = if (speechPitches.size >= 10) {  // Minimum 10 samples for speech
            calculateSaFromSpeaking(speechPitches)
        } else {
            null
        }

        // Combine recommendations if both are available
        val finalSaFreq = if (saFromSpeaking != null) {
            combineRecommendations(saFromSpeaking, saFromSinging)
        } else {
            saFromSinging
        }

        // Snap recommended Sa to nearest standard Sa note
        val recommendedSa = snapToNearestNote(finalSaFreq)

        // Convert lowest and highest to actual note names (not restricted to Sa scale)
        val lowestNote = frequencyToNote(lowestFreq)
        val highestNote = frequencyToNote(highestFreq)

        // Calculate speaking pitch note if available
        val speakingPitchNote = if (saFromSpeaking != null && speechPitches.size >= 10) {
            val sortedSpeech = speechPitches.sorted()
            val speechOutlierCutoff = (sortedSpeech.size * 0.05).toInt()
            val filteredSpeech = if (sortedSpeech.size > 20) {
                sortedSpeech.subList(speechOutlierCutoff, sortedSpeech.size - speechOutlierCutoff)
            } else {
                sortedSpeech
            }
            frequencyToNote(filteredSpeech.average())
        } else {
            null
        }

        return FindSaState.Finished(
            originalSa = recommendedSa,
            recommendedSa = recommendedSa,
            lowestNote = lowestNote,
            highestNote = highestNote,
            speakingPitch = speakingPitchNote
        )
    }

    /**
     * Calculate ideal Sa from speaking pitch
     * Speaking pitch is typically in the lower-middle of vocal range
     * Place Sa a perfect fourth (5 semitones) above mean speaking pitch
     */
    private fun calculateSaFromSpeaking(speechPitches: List<Float>): Double {
        val sortedSpeech = speechPitches.sorted()

        // Remove top and bottom 5% as outliers
        val outlierCutoff = (sortedSpeech.size * 0.05).toInt()
        val filteredSpeech = if (sortedSpeech.size > 20) {
            sortedSpeech.subList(outlierCutoff, sortedSpeech.size - outlierCutoff)
        } else {
            sortedSpeech
        }

        val meanSpeakingFreq = filteredSpeech.average()

        // Calculate Sa: 5 semitones above speaking pitch (perfect fourth)
        return meanSpeakingFreq * 2.0.pow(5.0 / 12.0)
    }

    /**
     * Combine Sa recommendations from speaking and singing
     * Uses weighted average if they're close, otherwise defaults to singing
     */
    private fun combineRecommendations(saFromSpeaking: Double, saFromSinging: Double): Double {
        // Calculate distance in semitones
        val semitoneDiff = abs(kotlin.math.log2(saFromSpeaking / saFromSinging) * 12.0)

        return if (semitoneDiff < 3.5) {
            // Recommendations are close - use weighted average (70% singing, 30% speaking)
            (saFromSinging * 0.7) + (saFromSpeaking * 0.3)
        } else {
            // Recommendations are far apart - trust singing more (it's more direct)
            saFromSinging
        }
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
     * Convert any frequency to its nearest note name (not restricted to standard Sa notes)
     */
    private fun frequencyToNote(frequency: Double): Note {
        // Calculate semitones from A4 (440 Hz)
        val semitonesFromA4 = 12.0 * kotlin.math.log2(frequency / 440.0)

        // Round to nearest semitone
        val nearestSemitone = kotlin.math.round(semitonesFromA4).toInt()

        // Calculate MIDI note number (A4 = 69)
        val midiNote = 69 + nearestSemitone

        // Extract octave and note within octave
        val octave = (midiNote / 12) - 1
        val noteIndex = midiNote % 12

        // Map to note name (using sharps for consistency)
        val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val noteName = "${noteNames[noteIndex]}$octave"

        // Calculate actual frequency for this note
        val actualFrequency = 440.0 * 2.0.pow(nearestSemitone / 12.0)

        return Note(name = noteName, frequency = actualFrequency)
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
        collectedSpeechPitches.clear()
        collectedSingingPitches.clear()

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
