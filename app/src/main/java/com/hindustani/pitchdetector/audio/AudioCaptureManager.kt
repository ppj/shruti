package com.hindustani.pitchdetector.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Manages real-time audio capture from device microphone
 */
class AudioCaptureManager(
    private val sampleRate: Int = 44100,
) {
    companion object {
        private const val TAG = "AudioCaptureManager"
        private const val BUFFER_SIZE_MULTIPLIER = 2
        private const val DEFAULT_BUFFER_SIZE = 4096
        private const val PCM_MAX_VALUE = 32768f
    }

    private var audioRecord: AudioRecord? = null
    private var captureJob: Job? = null

    private val bufferSize =
        AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        ).let { minSize ->
            // Use 2x minimum buffer size for better stability
            maxOf(minSize * BUFFER_SIZE_MULTIPLIER, DEFAULT_BUFFER_SIZE)
        }

    /**
     * Starts capturing audio and calls the callback with audio data
     *
     * Note: Permission is checked by MainActivity before calling startCapture()
     *
     * @param onAudioData Callback function that receives audio samples as FloatArray
     * @return Job that can be used to control the capture coroutine
     */
    @SuppressLint("MissingPermission")
    fun startCapture(onAudioData: (FloatArray) -> Unit): Job {
        stop()

        return CoroutineScope(Dispatchers.IO).launch {
            try {
                audioRecord =
                    AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize,
                    )

                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    throw IllegalStateException("AudioRecord initialization failed")
                }

                val buffer = ShortArray(bufferSize)
                audioRecord?.startRecording()

                while (isActive) {
                    val samplesRead = audioRecord?.read(buffer, 0, bufferSize) ?: 0

                    if (samplesRead > 0) {
                        // Convert 16-bit PCM to normalized float (-1.0 to 1.0)
                        val floatBuffer =
                            FloatArray(samplesRead) { i ->
                                buffer[i] / PCM_MAX_VALUE
                            }
                        onAudioData(floatBuffer)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during audio capture", e)
            } finally {
                stop()
            }
        }.also { job ->
            captureJob = job
        }
    }

    /**
     * Stops audio capture and releases resources
     */
    fun stop() {
        captureJob?.cancel()
        captureJob = null

        audioRecord?.apply {
            try {
                if (state == AudioRecord.STATE_INITIALIZED) {
                    stop()
                }
                release()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping AudioRecord", e)
            }
        }
        audioRecord = null
    }
}
