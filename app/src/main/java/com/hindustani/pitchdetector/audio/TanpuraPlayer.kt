package com.hindustani.pitchdetector.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sin

/**
 * Generates and plays synthetic tanpura sound with 4 strings
 * String 1: Variable (user selected note)
 * String 2: Sa (tonic root)
 * String 3: Sa (tonic root)
 * String 4: SA (upper octave tonic)
 */
class TanpuraPlayer(
    private val sampleRate: Int = 44100
) {
    companion object {
        private const val TAG = "TanpuraPlayer"
    }
    // Just Intonation ratios for all 12 notes
    private val noteRatios = mapOf(
        "S" to 1.0,
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
        "N" to 15.0 / 8.0
    )

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null

    // Tanpura configuration
    private var saFrequency: Double = 130.81  // Default C3
    private var string1Note: String = "P"     // Default to Pa
    private var volume: Float = 0.5f

    // Audio buffer size
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).let { minSize ->
        maxOf(minSize * 2, 8192)
    }

    /**
     * Start playing the tanpura
     */
    fun start(saFreq: Double, string1: String, vol: Float = 0.5f) {
        Log.d(TAG, "start() called: saFreq=$saFreq, string1=$string1, vol=$vol")
        stop()  // Stop any existing playback

        this.saFrequency = saFreq
        this.string1Note = string1
        this.volume = vol.coerceIn(0f, 1f)

        playbackJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Tanpura playback coroutine started")
                // Calculate frequencies for all 4 strings
                val string1Freq = saFrequency * (noteRatios[string1Note] ?: 1.0)
                val string2Freq = saFrequency
                val string3Freq = saFrequency
                val string4Freq = saFrequency * 2.0  // Upper octave Sa

                // Tanpura plucking pattern with overlapping sustains
                // Real tanpura: strings sustain 3-10 seconds, plucked at steady tempo
                //
                // Key insight: Unlike sequential playback, real tanpura has multiple strings
                // ringing simultaneously creating a continuous, seamless drone

                // Each string sustains for 7 seconds (very long sustain for deep drone)
                val sustainDuration = 7.0

                // Time between plucks (steady rhythm ~75 BPM)
                // With 7s sustain and 0.8s interval, we get ~8-9x overlap = seamless drone
                val pluckInterval = 0.8

                // Pre-generate all 4 strings with very slight amplitude variations for naturalism
                Log.d(TAG, "Generating string samples...")
                val string1Samples = generateStringPluck(string1Freq, sustainDuration, 0.98)
                val string2Samples = generateStringPluck(string2Freq, sustainDuration, 1.0)
                val string3Samples = generateStringPluck(string3Freq, sustainDuration, 1.0)
                val string4Samples = generateStringPluck(string4Freq, sustainDuration, 0.96)
                Log.d(TAG, "String samples generated")

                val allStrings = listOf(string1Samples, string2Samples, string3Samples, string4Samples)

                // Calculate buffer size for continuous playback
                val pluckIntervalSamples = (sampleRate * pluckInterval).toInt()
                val cycleDuration = pluckInterval * 4  // 4 strings
                val cycleSize = (sampleRate * cycleDuration).toInt()

                // Pre-generate first mixed buffer before creating AudioTrack
                // This ensures we have audio ready to play immediately
                val firstMixedBuffer = ShortArray(cycleSize)
                for ((stringIndex, stringSamples) in allStrings.withIndex()) {
                    val offset = stringIndex * pluckIntervalSamples
                    for (i in stringSamples.indices) {
                        val bufferIndex = offset + i
                        if (bufferIndex < firstMixedBuffer.size) {
                            val mixed = firstMixedBuffer[bufferIndex].toInt() + stringSamples[i].toInt()
                            firstMixedBuffer[bufferIndex] = mixed.coerceIn(-32768, 32767).toShort()
                        }
                    }
                }

                // Normalize the pre-generated buffer (leave more headroom for deep, full sound)
                val maxAmp = firstMixedBuffer.maxOfOrNull { kotlin.math.abs(it.toInt()) } ?: 1
                if (maxAmp > 28000) {
                    val scale = 28000.0 / maxAmp
                    for (i in firstMixedBuffer.indices) {
                        firstMixedBuffer[i] = (firstMixedBuffer[i] * scale).toInt().toShort()
                    }
                }

                // Now create and initialize AudioTrack with audio ready
                Log.d(TAG, "Creating AudioTrack...")
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                val trackState = audioTrack?.state
                Log.d(TAG, "AudioTrack created with state: $trackState")

                if (trackState != AudioTrack.STATE_INITIALIZED) {
                    Log.e(TAG, "AudioTrack initialization failed! State: $trackState")
                    throw IllegalStateException("AudioTrack initialization failed")
                }

                // Write initial data before starting playback
                val written = audioTrack?.write(firstMixedBuffer, 0, firstMixedBuffer.size) ?: 0
                Log.d(TAG, "Wrote initial buffer: $written samples")

                // Now start playback
                audioTrack?.play()
                Log.d(TAG, "AudioTrack play() called, playback started")

                // Continue playing in loop
                while (isActive) {
                    // Create a buffer for one complete cycle
                    val mixedBuffer = ShortArray(cycleSize)

                    // Mix all strings with their proper timing offsets
                    for ((stringIndex, stringSamples) in allStrings.withIndex()) {
                        val offset = stringIndex * pluckIntervalSamples

                        // Add this string to the mix
                        for (i in stringSamples.indices) {
                            val bufferIndex = offset + i
                            if (bufferIndex < mixedBuffer.size) {
                                // Mix by adding samples (will normalize later)
                                val mixed = mixedBuffer[bufferIndex].toInt() + stringSamples[i].toInt()
                                mixedBuffer[bufferIndex] = mixed.coerceIn(-32768, 32767).toShort()
                            }
                        }
                    }

                    // Normalize the mixed buffer to prevent clipping (leave more headroom for full sound)
                    val maxAmplitude = mixedBuffer.maxOfOrNull { kotlin.math.abs(it.toInt()) } ?: 1
                    if (maxAmplitude > 28000) {
                        val scale = 28000.0 / maxAmplitude
                        for (i in mixedBuffer.indices) {
                            mixedBuffer[i] = (mixedBuffer[i] * scale).toInt().toShort()
                        }
                    }

                    if (!isActive) break

                    // Write the mixed cycle to audio output
                    audioTrack?.write(mixedBuffer, 0, mixedBuffer.size)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in tanpura playback", e)
                // Clean up AudioTrack on error
                audioTrack?.apply {
                    try {
                        if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                            stop()
                        }
                        release()
                    } catch (ex: Exception) {
                        Log.e(TAG, "Error cleaning up AudioTrack", ex)
                    }
                }
                audioTrack = null
            }
        }
    }

    /**
     * Stop playing the tanpura
     */
    fun stop() {
        Log.d(TAG, "stop() called")
        playbackJob?.cancel()
        playbackJob = null

        audioTrack?.apply {
            try {
                val state = playState
                Log.d(TAG, "AudioTrack playState: $state")
                if (state == AudioTrack.PLAYSTATE_PLAYING) {
                    stop()
                    Log.d(TAG, "AudioTrack stopped")
                }
                release()
                Log.d(TAG, "AudioTrack released")
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping AudioTrack", e)
            }
        }
        audioTrack = null
    }

    /**
     * Update tanpura parameters while playing
     */
    fun updateParameters(saFreq: Double, string1: String, vol: Float = 0.5f) {
        Log.d(TAG, "updateParameters() called: saFreq=$saFreq, string1=$string1, vol=$vol")
        val wasPlaying = isPlaying()
        Log.d(TAG, "wasPlaying: $wasPlaying")
        if (wasPlaying) {
            stop()
            start(saFreq, string1, vol)
        }
    }

    /**
     * Check if tanpura is currently playing
     */
    fun isPlaying(): Boolean {
        val playing = playbackJob?.isActive == true
        Log.d(TAG, "isPlaying() returning: $playing")
        return playing
    }

    /**
     * Generate a single string pluck with realistic tanpura timbre
     * Uses additive synthesis with jivari effect (buzzing harmonics)
     *
     * @param frequency The fundamental frequency of the string
     * @param duration Duration of the pluck in seconds
     * @param amplitudeVariation Slight variation in amplitude (0.9-1.1) for naturalism
     */
    private fun generateStringPluck(frequency: Double, duration: Double, amplitudeVariation: Double = 1.0): ShortArray {
        val numSamples = (sampleRate * duration).toInt()
        val samples = ShortArray(numSamples)

        // Tanpura harmonic series with jivari effect
        // Deep, drony sound: very strong fundamental and lower harmonics
        // Reduced higher harmonics for less "guitar-like" brightness
        val harmonics = listOf(
            1.0 to 1.0,       // Fundamental (strongest - the core of the drone)
            2.0 to 0.85,      // Octave (very strong for depth)
            3.0 to 0.68,      // Fifth (prominent)
            4.0 to 0.55,      // Double octave (strong)
            5.0 to 0.45,      // Major third
            6.0 to 0.38,      // Fifth + octave
            7.0 to 0.32,      // Minor seventh
            8.0 to 0.26,      // Triple octave
            9.0 to 0.20,      // Major ninth
            10.0 to 0.16,     // Major third + octave
            11.0 to 0.12,     // Eleventh
            12.0 to 0.10,     // Fifth + double octave
            13.0 to 0.08,     // Thirteenth
            14.0 to 0.06,     // Minor seventh + octave
            15.0 to 0.05,     // Major seventh + octave
            16.0 to 0.04,     // Quadruple octave
            18.0 to 0.03,     // Ninth + octave (subtle)
            20.0 to 0.02,     // Major third + double octave (very subtle)
            24.0 to 0.015     // High harmonics for subtle shimmer only
        )

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate

            // Envelope: Gentle attack, extremely long sustain with very slow decay
            val envelope = when {
                t < 0.1 -> {
                    // Gentle, gradual attack (100ms) - not percussive like guitar
                    (t / 0.1) * (1.0 - 0.3 * exp(-t * 15))  // Slightly curved attack
                }
                else -> {
                    // Extremely slow exponential decay for deep, sustained drone (7-10 seconds)
                    exp(-t * 0.25)  // Much slower decay for continuous drone feel
                }
            }

            // Very subtle jivari shimmer (much gentler than before)
            // Real tanpura shimmer comes from harmonic interactions, not obvious modulation
            val subtleShimmer = 1.0 + 0.015 * sin(2.0 * PI * 1.8 * t) * exp(-t * 1.2)

            // Generate sample with additive synthesis
            var sample = 0.0
            for ((harmonicNum, amplitude) in harmonics) {
                val harmonicFreq = frequency * harmonicNum

                // No frequency modulation - keep harmonics stable
                val phase = 2.0 * PI * harmonicFreq * t

                // Each harmonic decays at slightly different rate (more realistic)
                // Lower harmonics sustain longest (deep drone), higher harmonics fade gradually
                val harmonicDecay = exp(-t * (0.2 + harmonicNum * 0.02))

                // Very subtle random-like phase variation per harmonic (from harmonic interference)
                val harmonicPhaseShift = sin(harmonicNum * 0.7) * 0.05

                sample += amplitude * harmonicDecay * sin(phase + harmonicPhaseShift)
            }

            // Apply envelope, very subtle shimmer, amplitude variation, and volume
            sample *= envelope * subtleShimmer * amplitudeVariation * volume

            // Normalize and convert to 16-bit PCM
            val amplitudeSum = harmonics.sumOf { it.second }
            sample /= amplitudeSum

            // Soft clipping for natural saturation
            sample = sample.coerceIn(-1.0, 1.0)

            // Convert to 16-bit signed integer with fuller amplitude for deep drone
            samples[i] = (sample * 32767 * 0.8).toInt().coerceIn(-32768, 32767).toShort()
        }

        return samples
    }

    /**
     * Get list of available notes for string 1
     */
    fun getAvailableNotes(): List<String> {
        return noteRatios.keys.toList()
    }
}
