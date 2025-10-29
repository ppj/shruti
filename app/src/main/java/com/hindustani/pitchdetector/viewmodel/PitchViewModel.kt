package com.hindustani.pitchdetector.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hindustani.pitchdetector.audio.AudioCaptureManager
import com.hindustani.pitchdetector.audio.PYINDetector
import com.hindustani.pitchdetector.constants.VocalRangeConstants
import com.hindustani.pitchdetector.data.PitchState
import com.hindustani.pitchdetector.data.UserSettings
import com.hindustani.pitchdetector.data.UserSettingsRepository
import com.hindustani.pitchdetector.music.HindustaniNoteConverter
import com.hindustani.pitchdetector.music.SaParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for pitch detection and state management
 */
class PitchViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val MIN_PITCH_CONFIDENCE = 0.5f
        private const val SMOOTHING_ALPHA = 0.25
    }

    private val audioCapture = AudioCaptureManager()
    private val pitchDetector = PYINDetector()
    private val tanpuraPlayer = com.hindustani.pitchdetector.audio.TanpuraPlayer(application.applicationContext)
    private val userSettingsRepository = UserSettingsRepository(application.applicationContext)

    private val _settings = MutableStateFlow(UserSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private val _pitchState = MutableStateFlow(PitchState())
    val pitchState: StateFlow<PitchState> = _pitchState.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isTanpuraPlaying = MutableStateFlow(false)
    val isTanpuraPlaying: StateFlow<Boolean> = _isTanpuraPlaying.asStateFlow()

    private var processingJob: Job? = null

    // Smoothing for needle movement
    private var smoothedCentsDeviation: Double = 0.0

    init {
        viewModelScope.launch {
            val initialSettings = userSettingsRepository.userSettings.first()
            _settings.value = initialSettings

            // Initialize with persisted Sa note
            val frequency = SaParser.parseToFrequency(initialSettings.saNote)
            if (frequency != null) {
                _pitchState.update {
                    it.copy(
                        saNote = initialSettings.saNote,
                        saFrequency = frequency,
                    )
                }
            }
        }

        viewModelScope.launch {
            userSettingsRepository.userSettings.collect { userSettings ->
                _settings.value = userSettings
                _pitchState.update {
                    it.copy(
                        saNote = userSettings.saNote,
                        saFrequency = userSettings.saFrequency,
                        toleranceCents = userSettings.toleranceCents,
                    )
                }
            }
        }
    }

    /**
     * Toggle recording on/off
     */
    fun toggleRecording() {
        if (_isRecording.value) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    /**
     * Start pitch detection
     */
    private fun startRecording() {
        // Reset smoothing when starting new recording session
        smoothedCentsDeviation = 0.0

        processingJob =
            audioCapture.startCapture { audioData ->
                viewModelScope.launch(Dispatchers.Default) {
                    processAudioData(audioData)
                }
            }
        _isRecording.value = true
    }

    /**
     * Stop pitch detection
     */
    private fun stopRecording() {
        audioCapture.stop()
        processingJob?.cancel()
        processingJob = null
        _isRecording.value = false

        _pitchState.update { it.copy(currentNote = null, currentFrequency = null, confidence = 0f) }
    }

    /**
     * Process audio data and detect pitch
     */
    private suspend fun processAudioData(audioData: FloatArray) {
        val pitchResult = pitchDetector.detectPitch(audioData)

        if (pitchResult.frequency != null && pitchResult.confidence > MIN_PITCH_CONFIDENCE) {
            val frequency = pitchResult.frequency.toDouble()

            if (frequency in VocalRangeConstants.MIN_VOCAL_FREQ..VocalRangeConstants.MAX_VOCAL_FREQ) {
                val converter =
                    HindustaniNoteConverter(
                        saFrequency = _settings.value.saFrequency,
                        toleranceCents = _settings.value.toleranceCents,
                        use22Shruti = _settings.value.use22Shruti,
                    )

                val note = converter.convertFrequency(frequency)
                val smoothedNote = createSmoothedNote(note)

                withContext(Dispatchers.Main) {
                    _pitchState.update {
                        it.copy(
                            currentNote = smoothedNote,
                            currentFrequency = pitchResult.frequency,
                            confidence = pitchResult.confidence,
                            saNote = _settings.value.saNote,
                            saFrequency = _settings.value.saFrequency,
                            toleranceCents = _settings.value.toleranceCents,
                        )
                    }
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                _pitchState.update {
                    it.copy(
                        currentNote = null,
                        currentFrequency = null,
                        confidence = pitchResult.confidence,
                    )
                }
            }
        }
    }

    /**
     * Create a smoothed note using Exponential Moving Average to reduce needle jitter
     * @param note The original detected note
     * @return Note with smoothed cents deviation and recalculated isPerfect/isFlat/isSharp flags
     */
    private fun createSmoothedNote(note: HindustaniNoteConverter.HindustaniNote): HindustaniNoteConverter.HindustaniNote {
        // Apply Exponential Moving Average smoothing to reduce needle jitter
        smoothedCentsDeviation = SMOOTHING_ALPHA * note.centsDeviation +
            (1 - SMOOTHING_ALPHA) * smoothedCentsDeviation

        // Create smoothed note with updated cents deviation
        return note.copy(
            centsDeviation = smoothedCentsDeviation,
            // Recalculate isPerfect/isFlat/isSharp based on smoothed value
            isPerfect = kotlin.math.abs(smoothedCentsDeviation) <= _settings.value.toleranceCents,
            isFlat = smoothedCentsDeviation < -_settings.value.toleranceCents,
            isSharp = smoothedCentsDeviation > _settings.value.toleranceCents,
        )
    }

    /**
     * Update Sa (tonic) note and persist it
     */
    fun updateSa(westernNote: String) {
        val frequency = SaParser.parseToFrequency(westernNote)
        if (frequency != null) {
            viewModelScope.launch {
                userSettingsRepository.updateSaNote(westernNote)
            }

            _pitchState.update {
                it.copy(
                    saNote = westernNote,
                    saFrequency = frequency,
                )
            }

            // Update tanpura if it's currently playing
            if (_isTanpuraPlaying.value) {
                tanpuraPlayer.updateParameters(
                    saFreq = frequency,
                    string1 = _settings.value.tanpuraString1,
                    vol = _settings.value.tanpuraVolume,
                )
            }
        }
    }

    /**
     * Get the current Sa frequency in Hz
     */
    fun getSaFrequency(): Double {
        return _pitchState.value.saFrequency
    }

    /**
     * Update tolerance in cents
     */
    fun updateTolerance(cents: Double) {
        // Persist to DataStore (flow collector will update _settings)
        viewModelScope.launch {
            userSettingsRepository.updateTolerance(cents)
        }
    }

    /**
     * Update tuning system (12-note vs 22-shruti)
     */
    fun updateTuningSystem(use22Shruti: Boolean) {
        // Persist to DataStore (flow collector will update _settings)
        viewModelScope.launch {
            userSettingsRepository.updateTuningSystem(use22Shruti)
        }
    }

    /**
     * Toggle tanpura on/off
     */
    fun toggleTanpura() {
        if (_isTanpuraPlaying.value) {
            stopTanpura()
        } else {
            startTanpura()
        }
    }

    /**
     * Start tanpura playback
     */
    private fun startTanpura() {
        tanpuraPlayer.start(
            saFreq = _settings.value.saFrequency,
            string1 = _settings.value.tanpuraString1,
            vol = _settings.value.tanpuraVolume,
        )
        _isTanpuraPlaying.value = true
        // Persist to DataStore (flow collector will update _settings)
        viewModelScope.launch {
            userSettingsRepository.updateTanpuraEnabled(true)
        }
    }

    /**
     * Stop tanpura playback
     */
    private fun stopTanpura() {
        tanpuraPlayer.stop()
        _isTanpuraPlaying.value = false
        // Persist to DataStore (flow collector will update _settings)
        viewModelScope.launch {
            userSettingsRepository.updateTanpuraEnabled(false)
        }
    }

    /**
     * Update tanpura string 1 note
     */
    fun updateTanpuraString1(swar: String) {
        // Persist to DataStore (flow collector will update _settings)
        viewModelScope.launch {
            userSettingsRepository.updateTanpuraString1(swar)
        }

        // Update tanpura immediately if it's currently playing
        if (_isTanpuraPlaying.value) {
            tanpuraPlayer.updateParameters(
                saFreq = _settings.value.saFrequency,
                string1 = swar,
                vol = _settings.value.tanpuraVolume,
            )
        }
    }

    /**
     * Update tanpura volume
     */
    fun updateTanpuraVolume(volume: Float) {
        // Persist to DataStore (flow collector will update _settings)
        viewModelScope.launch {
            userSettingsRepository.updateTanpuraVolume(volume)
        }

        // Update tanpura immediately if it's currently playing
        if (_isTanpuraPlaying.value) {
            tanpuraPlayer.updateParameters(
                saFreq = _settings.value.saFrequency,
                string1 = _settings.value.tanpuraString1,
                vol = volume,
            )
        }
    }

    /**
     * Get available notes for tanpura string 1
     */
    fun getTanpuraAvailableNotes(): List<String> {
        return tanpuraPlayer.getAvailableNotes()
    }

    /**
     * Clean up resources
     */
    override fun onCleared() {
        super.onCleared()
        stopRecording()
        stopTanpura()
    }
}
