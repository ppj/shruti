package com.hindustani.pitchdetector.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.sin

/**
 * Plays synthesized string pluck sounds at variable frequencies.
 * Used for training exercises where users need to tune to a specific pitch.
 *
 * Uses AudioTrack in MODE_STATIC for short, on-demand playback.
 * Generates sine waves with exponential decay envelope to simulate a plucked string.
 */
class StringPluckPlayer {
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "StringPluckPlayer"
        private const val SAMPLE_RATE = 44100
        private const val DURATION_SECONDS = 1.0
        private const val DECAY_FACTOR = 5.0 // Controls how fast the sound fades
    }

    /**
     * Plays a string pluck sound at the specified frequency.
     *
     * @param frequency The frequency in Hz (e.g., 261.63 for middle C)
     * @param volume The playback volume (0.0 to 1.0), defaults to 1.0
     */
    fun play(
        frequency: Double,
        volume: Float = 1.0f,
    ) {
        stop() // Ensure any previous sound is stopped and released

        playbackJob =
            coroutineScope.launch {
                try {
                    val pcmData = generateSineWaveWithDecay(frequency)
                    val bufferSizeInBytes = pcmData.size * 2 // Short is 2 bytes

                    audioTrack =
                        AudioTrack.Builder()
                            .setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build(),
                            )
                            .setAudioFormat(
                                AudioFormat.Builder()
                                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                    .setSampleRate(SAMPLE_RATE)
                                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                    .build(),
                            )
                            .setBufferSizeInBytes(bufferSizeInBytes)
                            .setTransferMode(AudioTrack.MODE_STATIC)
                            .build()

                    audioTrack?.apply {
                        // Write the entire PCM data to the buffer
                        write(pcmData, 0, pcmData.size)

                        // Set volume and start playback
                        setVolume(volume.coerceIn(0f, 1f))
                        play()

                        // Wait for playback to complete naturally
                        // In MODE_STATIC, getPlaybackHeadPosition() tracks progress
                        while (playState == AudioTrack.PLAYSTATE_PLAYING) {
                            kotlinx.coroutines.delay(50)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error playing string pluck at $frequency Hz", e)
                } finally {
                    // Clean up after playback completes
                    releaseAudioTrack()
                }
            }
    }

    /**
     * Stops playback and releases audio resources.
     */
    fun stop() {
        playbackJob?.cancel()
        playbackJob = null
        releaseAudioTrack()
    }

    /**
     * Releases the AudioTrack resource.
     */
    private fun releaseAudioTrack() {
        audioTrack?.apply {
            try {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    stop()
                }
                release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing AudioTrack", e)
            }
        }
        audioTrack = null
    }

    /**
     * Generates PCM data for a sine wave with exponential decay envelope.
     *
     * @param frequency The frequency of the sine wave in Hz
     * @return ShortArray containing PCM samples
     */
    private fun generateSineWaveWithDecay(frequency: Double): ShortArray {
        val numSamples = (SAMPLE_RATE * DURATION_SECONDS).toInt()
        val pcmData = ShortArray(numSamples)
        val angularFrequency = 2.0 * Math.PI * frequency / SAMPLE_RATE

        for (i in 0 until numSamples) {
            val time = i.toDouble() / SAMPLE_RATE

            // Apply exponential decay to the amplitude
            val amplitude = exp(-time * DECAY_FACTOR)

            // Generate the sine wave sample
            val sample = (amplitude * sin(angularFrequency * i) * Short.MAX_VALUE).toInt().toShort()
            pcmData[i] = sample
        }

        return pcmData
    }

    /**
     * Checks if audio is currently playing.
     */
    fun isPlaying(): Boolean {
        return audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING
    }
}
