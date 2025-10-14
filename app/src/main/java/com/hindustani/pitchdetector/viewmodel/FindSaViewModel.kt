package com.hindustani.pitchdetector.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hindustani.pitchdetector.audio.AudioCaptureManager
import com.hindustani.pitchdetector.audio.PYINDetector
import com.hindustani.pitchdetector.audio.TanpuraPlayer
import com.hindustani.pitchdetector.constants.VocalRangeConstants
import com.hindustani.pitchdetector.ui.findsa.FindSaState
import com.hindustani.pitchdetector.ui.findsa.FindSaUiState
import com.hindustani.pitchdetector.ui.findsa.Note
import com.hindustani.pitchdetector.ui.findsa.TestMode
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

    companion object {
        // Confidence thresholds for accepting pitch samples
        private const val SPEECH_CONFIDENCE_THRESHOLD = 0.7f
        private const val SINGING_CONFIDENCE_THRESHOLD = 0.8f

        // Sample requirements
        private const val MIN_SPEECH_SAMPLES = 10
        private const val MIN_SINGING_SAMPLES = 20
        private const val MIN_SAMPLES_FOR_OUTLIER_REMOVAL = 20

        // Outlier removal percentages
        private const val SPEECH_OUTLIER_PERCENTAGE = 0.05  // 5%
        private const val SINGING_OUTLIER_PERCENTAGE = 0.1  // 10%

        // Musical interval calculations (in semitones)
        private const val SA_FROM_SPEAKING_SEMITONES = 5  // Perfect fourth above speaking pitch
        private const val SA_FROM_SINGING_SEMITONES = 7   // Perfect fifth above lowest note

        // Combination algorithm parameters
        private const val SEMITONE_AGREEMENT_THRESHOLD = 3.5
        private const val SINGING_WEIGHT = 0.7
        private const val SPEAKING_WEIGHT = 0.3

        // Playback settings
        private const val PREVIEW_VOLUME = 0.5f
    }

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
     * Set the test mode and reset to initial state
     */
    fun setTestMode(mode: TestMode) {
        _uiState.update { it.copy(
            testMode = mode,
            currentState = FindSaState.NotStarted,
            error = null
        )}
    }

    /**
     * Start the vocal range test
     * Begins with the appropriate phase based on selected mode
     */
    fun startTest() {
        // Clear previous data
        collectedSpeechPitches.clear()
        collectedSingingPitches.clear()

        val currentMode = _uiState.value.testMode

        when (currentMode) {
            TestMode.SPEAKING_ONLY,
            TestMode.BOTH -> {
                // Start with speaking phase
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
            TestMode.SINGING_ONLY -> {
                // Skip speaking phase, go directly to singing
                _uiState.update { it.copy(
                    currentState = FindSaState.RecordingSinging,
                    collectedSamplesCount = 0,
                    error = null
                )}

                // Start audio capture for singing
                recordingJob = audioCapture.startCapture { audioData ->
                    viewModelScope.launch(Dispatchers.Default) {
                        processSingingData(audioData)
                    }
                }
            }
        }
    }

    /**
     * Stop the speech test and transition to next phase based on mode
     */
    fun stopSpeechTest() {
        // Stop current audio capture
        audioCapture.stop()
        recordingJob?.cancel()
        recordingJob = null

        val currentMode = _uiState.value.testMode

        when (currentMode) {
            TestMode.SPEAKING_ONLY -> {
                // Go directly to analysis
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
            TestMode.BOTH -> {
                // Transition to singing phase
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
            TestMode.SINGING_ONLY -> {
                // This shouldn't happen, but handle it gracefully
                // Do nothing - singing phase shouldn't be preceded by speech phase in this mode
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

        if (pitchResult.frequency != null && pitchResult.confidence > SPEECH_CONFIDENCE_THRESHOLD) {
            val frequency = pitchResult.frequency

            if (frequency in VocalRangeConstants.MIN_VOCAL_FREQ..VocalRangeConstants.MAX_VOCAL_FREQ) {
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

        if (pitchResult.frequency != null && pitchResult.confidence > SINGING_CONFIDENCE_THRESHOLD) {
            val frequency = pitchResult.frequency

            if (frequency in VocalRangeConstants.MIN_VOCAL_FREQ..VocalRangeConstants.MAX_VOCAL_FREQ) {
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
        val currentMode = _uiState.value.testMode

        return when (currentMode) {
            TestMode.SPEAKING_ONLY -> {
                analyzeSpeakingOnly(speechPitches)
            }
            TestMode.SINGING_ONLY -> {
                analyzeSingingOnly(singingPitches)
            }
            TestMode.BOTH -> {
                analyzeBothMethods(speechPitches, singingPitches)
            }
        }
    }

    /**
     * Analyze using speaking voice only
     */
    private fun analyzeSpeakingOnly(speechPitches: List<Float>): FindSaState.Finished {
        if (speechPitches.isEmpty()) {
            throw IllegalStateException("No valid pitches recorded. Please try again.")
        }

        if (speechPitches.size < MIN_SPEECH_SAMPLES) {
            throw IllegalStateException("Insufficient data (${speechPitches.size} samples). Please speak longer.")
        }

        // Calculate Sa from speaking
        val saFromSpeaking = calculateSaFromSpeaking(speechPitches)
        val recommendedSa = snapToNearestNote(saFromSpeaking)

        // Calculate speaking pitch note
        val sortedSpeech = speechPitches.sorted()
        val speechOutlierCutoff = (sortedSpeech.size * SPEECH_OUTLIER_PERCENTAGE).toInt()
        val filteredSpeech = if (sortedSpeech.size > MIN_SAMPLES_FOR_OUTLIER_REMOVAL) {
            sortedSpeech.subList(speechOutlierCutoff, sortedSpeech.size - speechOutlierCutoff)
        } else {
            sortedSpeech
        }
        val speakingPitchNote = frequencyToNote(filteredSpeech.average())

        // For speaking only, use speaking pitch range as vocal range estimate
        val lowestNote = frequencyToNote(filteredSpeech.first().toDouble())
        val highestNote = frequencyToNote(filteredSpeech.last().toDouble())

        return FindSaState.Finished(
            originalSa = recommendedSa,
            recommendedSa = recommendedSa,
            lowestNote = lowestNote,
            highestNote = highestNote,
            speakingPitch = speakingPitchNote,
            testMode = TestMode.SPEAKING_ONLY
        )
    }

    /**
     * Analyze using singing range only
     */
    private fun analyzeSingingOnly(singingPitches: List<Float>): FindSaState.Finished {
        if (singingPitches.isEmpty()) {
            throw IllegalStateException("No valid pitches recorded. Please try again.")
        }

        if (singingPitches.size < MIN_SINGING_SAMPLES) {
            throw IllegalStateException("Insufficient data (${singingPitches.size} samples). Please hold notes longer.")
        }

        // Process singing pitches
        val sortedSingingPitches = singingPitches.sorted()
        val outlierCutoff = (sortedSingingPitches.size * SINGING_OUTLIER_PERCENTAGE).toInt()
        val filteredSingingPitches = if (sortedSingingPitches.size > MIN_SAMPLES_FOR_OUTLIER_REMOVAL) {
            sortedSingingPitches.subList(outlierCutoff, sortedSingingPitches.size - outlierCutoff)
        } else {
            sortedSingingPitches
        }

        // Find minimum and maximum comfortable frequencies
        val lowestFreq = filteredSingingPitches.first().toDouble()
        val highestFreq = filteredSingingPitches.last().toDouble()

        val saFromSinging = lowestFreq * 2.0.pow(SA_FROM_SINGING_SEMITONES.toDouble() / 12.0)
        val recommendedSa = snapToNearestNote(saFromSinging)

        // Convert lowest and highest to actual note names
        val lowestNote = frequencyToNote(lowestFreq)
        val highestNote = frequencyToNote(highestFreq)

        return FindSaState.Finished(
            originalSa = recommendedSa,
            recommendedSa = recommendedSa,
            lowestNote = lowestNote,
            highestNote = highestNote,
            speakingPitch = null,
            testMode = TestMode.SINGING_ONLY
        )
    }

    /**
     * Analyze using both speaking and singing methods (original algorithm)
     */
    private fun analyzeBothMethods(speechPitches: List<Float>, singingPitches: List<Float>): FindSaState.Finished {
        if (singingPitches.isEmpty()) {
            throw IllegalStateException("No valid pitches recorded. Please try again.")
        }

        if (singingPitches.size < MIN_SINGING_SAMPLES) {
            throw IllegalStateException("Insufficient data (${singingPitches.size} samples). Please hold notes longer.")
        }

        // Process singing pitches
        val sortedSingingPitches = singingPitches.sorted()
        val outlierCutoff = (sortedSingingPitches.size * SINGING_OUTLIER_PERCENTAGE).toInt()
        val filteredSingingPitches = if (sortedSingingPitches.size > MIN_SAMPLES_FOR_OUTLIER_REMOVAL) {
            sortedSingingPitches.subList(outlierCutoff, sortedSingingPitches.size - outlierCutoff)
        } else {
            sortedSingingPitches
        }

        // Find minimum and maximum comfortable frequencies
        val lowestFreq = filteredSingingPitches.first().toDouble()
        val highestFreq = filteredSingingPitches.last().toDouble()

        val saFromSinging = lowestFreq * 2.0.pow(SA_FROM_SINGING_SEMITONES.toDouble() / 12.0)

        val saFromSpeaking = if (speechPitches.size >= MIN_SPEECH_SAMPLES) {
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
        val speakingPitchNote = if (saFromSpeaking != null && speechPitches.size >= MIN_SPEECH_SAMPLES) {
            val sortedSpeech = speechPitches.sorted()
            val speechOutlierCutoff = (sortedSpeech.size * SPEECH_OUTLIER_PERCENTAGE).toInt()
            val filteredSpeech = if (sortedSpeech.size > MIN_SAMPLES_FOR_OUTLIER_REMOVAL) {
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
            speakingPitch = speakingPitchNote,
            testMode = TestMode.BOTH
        )
    }

    /**
     * Calculate ideal Sa from speaking pitch
     * Speaking pitch is typically in the lower-middle of vocal range
     */
    private fun calculateSaFromSpeaking(speechPitches: List<Float>): Double {
        val sortedSpeech = speechPitches.sorted()

        val outlierCutoff = (sortedSpeech.size * SPEECH_OUTLIER_PERCENTAGE).toInt()
        val filteredSpeech = if (sortedSpeech.size > MIN_SAMPLES_FOR_OUTLIER_REMOVAL) {
            sortedSpeech.subList(outlierCutoff, sortedSpeech.size - outlierCutoff)
        } else {
            sortedSpeech
        }

        val meanSpeakingFreq = filteredSpeech.average()

        return meanSpeakingFreq * 2.0.pow(SA_FROM_SPEAKING_SEMITONES.toDouble() / 12.0)
    }

    /**
     * Combine Sa recommendations from speaking and singing
     * Uses weighted average if they're close, otherwise defaults to singing
     */
    private fun combineRecommendations(saFromSpeaking: Double, saFromSinging: Double): Double {
        val semitoneDiff = abs(kotlin.math.log2(saFromSpeaking / saFromSinging) * 12.0)

        return if (semitoneDiff < SEMITONE_AGREEMENT_THRESHOLD) {
            (saFromSinging * SINGING_WEIGHT) + (saFromSpeaking * SPEAKING_WEIGHT)
        } else {
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
                currentState = currentState.copy(recommendedSa = adjustedNote)
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
                vol = PREVIEW_VOLUME
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
