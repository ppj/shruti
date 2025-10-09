package com.hindustani.pitchdetector.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hindustani.pitchdetector.audio.AudioCaptureManager
import com.hindustani.pitchdetector.audio.PYINDetector
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
    private val smoothingAlpha = 0.25  // 0.25 = responsive but smooth, lower = smoother but slower

    init {
        viewModelScope.launch {
            val initialSettings = userSettingsRepository.userSettings.first()
            _settings.value = initialSettings
            updateSa(initialSettings.defaultSaNote) // set the sa note to the default
        }

        viewModelScope.launch {
            userSettingsRepository.userSettings.collect {
                userSettings ->
                _settings.value = userSettings
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

        processingJob = audioCapture.startCapture { audioData ->
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

        // Clear current note
        _pitchState.update { it.copy(currentNote = null, currentFrequency = null, confidence = 0f) }
    }

    /**
     * Process audio data and detect pitch
     */
    private suspend fun processAudioData(audioData: FloatArray) {
        val pitchResult = pitchDetector.detectPitch(audioData)

        if (pitchResult.frequency != null && pitchResult.confidence > 0.5f) {
            val frequency = pitchResult.frequency.toDouble()

            // Only process frequencies in reasonable vocal range (80 Hz - 1000 Hz)
            if (frequency in 80.0..1000.0) {
                val converter = HindustaniNoteConverter(
                    saFrequency = _settings.value.saFrequency,
                    toleranceCents = _settings.value.toleranceCents,
                    use22Shruti = _settings.value.use22Shruti
                )

                val note = converter.convertFrequency(frequency)

                // Apply Exponential Moving Average smoothing to reduce needle jitter
                smoothedCentsDeviation = smoothingAlpha * note.centsDeviation +
                                        (1 - smoothingAlpha) * smoothedCentsDeviation

                // Create smoothed note with updated cents deviation
                val smoothedNote = note.copy(
                    centsDeviation = smoothedCentsDeviation,
                    // Recalculate isPerfect/isFlat/isSharp based on smoothed value
                    isPerfect = kotlin.math.abs(smoothedCentsDeviation) <= _settings.value.toleranceCents,
                    isFlat = smoothedCentsDeviation < -_settings.value.toleranceCents,
                    isSharp = smoothedCentsDeviation > _settings.value.toleranceCents
                )

                withContext(Dispatchers.Main) {
                    _pitchState.update {
                        it.copy(
                            currentNote = smoothedNote,
                            currentFrequency = pitchResult.frequency,
                            confidence = pitchResult.confidence,
                            saNote = _settings.value.saNote,
                            saFrequency = _settings.value.saFrequency,
                            toleranceCents = _settings.value.toleranceCents
                        )
                    }
                }
            }
        } else {
            // Low confidence or no pitch detected
            withContext(Dispatchers.Main) {
                _pitchState.update {
                    it.copy(
                        currentNote = null,
                        currentFrequency = null,
                        confidence = pitchResult.confidence
                    )
                }
            }
        }
    }

    /**
     * Update Sa (tonic) note
     */
    fun updateSa(westernNote: String) {
        val frequency = SaParser.parseToFrequency(westernNote)
        if (frequency != null) {
            viewModelScope.launch {
                userSettingsRepository.updateSaNote(westernNote)
            }
            _settings.update {
                it.copy(
                    saNote = westernNote,
                    saFrequency = frequency
                )
            }

            // Also update pitch state immediately for UI feedback
            _pitchState.update {
                it.copy(
                    saNote = westernNote,
                    saFrequency = frequency
                )
            }

            // Update tanpura if it's currently playing
            if (_isTanpuraPlaying.value) {
                tanpuraPlayer.updateParameters(
                    saFreq = frequency,
                    string1 = _settings.value.tanpuraString1,
                    vol = _settings.value.tanpuraVolume
                )
            }
        }
    }

    /**
     * Update default Sa (tonic) note
     */
    fun updateDefaultSa(westernNote: String) {
        viewModelScope.launch {
            userSettingsRepository.updateDefaultSaNote(westernNote)
        }
    }


    /**
     * Update tolerance in cents
     */
    fun updateTolerance(cents: Double) {
        viewModelScope.launch {
            userSettingsRepository.updateTolerance(cents)
        }
        _settings.update {
            it.copy(toleranceCents = cents)
        }
    }

    /**
     * Update tuning system (12-note vs 22-shruti)
     */
    fun updateTuningSystem(use22Shruti: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.updateTuningSystem(use22Shruti)
        }
        _settings.update {
            it.copy(use22Shruti = use22Shruti)
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
            vol = _settings.value.tanpuraVolume
        )
        _isTanpuraPlaying.value = true
        viewModelScope.launch {
            userSettingsRepository.updateTanpuraEnabled(true)
        }
        _settings.update { it.copy(isTanpuraEnabled = true) }
    }

    /**
     * Stop tanpura playback
     */
    private fun stopTanpura() {
        tanpuraPlayer.stop()
        _isTanpuraPlaying.value = false
        viewModelScope.launch {
            userSettingsRepository.updateTanpuraEnabled(false)
        }
        _settings.update { it.copy(isTanpuraEnabled = false) }
    }

    /**
     * Update tanpura string 1 note
     */
    fun updateTanpuraString1(swara: String) {
        viewModelScope.launch {
            userSettingsRepository.updateTanpuraString1(swara)
        }
        _settings.update {
            it.copy(tanpuraString1 = swara)
        }

        // Update tanpura if it's currently playing
        if (_isTanpuraPlaying.value) {
            tanpuraPlayer.updateParameters(
                saFreq = _settings.value.saFrequency,
                string1 = swara,
                vol = _settings.value.tanpuraVolume
            )
        }
    }

    /**
     * Update tanpura volume
     */
    fun updateTanpuraVolume(volume: Float) {
        viewModelScope.launch {
            userSettingsRepository.updateTanpuraVolume(volume)
        }
        _settings.update {
            it.copy(tanpuraVolume = volume)
        }

        // Update tanpura if it's currently playing
        if (_isTanpuraPlaying.value) {
            tanpuraPlayer.updateParameters(
                saFreq = _settings.value.saFrequency,
                string1 = _settings.value.tanpuraString1,
                vol = volume
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