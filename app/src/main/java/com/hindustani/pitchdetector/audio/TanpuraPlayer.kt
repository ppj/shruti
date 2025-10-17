package com.hindustani.pitchdetector.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import com.hindustani.pitchdetector.music.SaNotes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Plays pre-recorded tanpura sound with 4 strings from OGG files
 * Uses AudioTrack with decoded PCM for truly gapless looping
 *
 * String 1: Variable (user selected note: P, M, S, or N)
 * String 2: Sa (tonic root)
 * String 3: Sa (tonic root)
 * String 4: Lower octave Sa
 */
class TanpuraPlayer(private val context: Context) {
    companion object {
        private const val TAG = "TanpuraPlayer"
        private const val SAMPLE_RATE = 44100
        private const val DEFAULT_SA_FREQUENCY = 130.81
        private const val DEFAULT_VOLUME = 0.5f
        private const val BUFFER_SIZE_MULTIPLIER = 2
        private const val DEFAULT_BUFFER_SIZE = 8192
        private const val WRITE_SIZE_DIVISOR = 2
        private const val DECODER_TIMEOUT_US = 10000L

        // Available String 1 notes (most common)
        private val AVAILABLE_STRING1_NOTES = listOf("P", "m", "M", "S", "N")

        // Map user-facing note names to filename suffixes (case-insensitive for macOS)
        private val NOTE_TO_FILENAME =
            mapOf(
                "P" to "P",
                "m" to "ms", // shuddha madhyam
                "M" to "Mt", // tivra madhyam
                "S" to "S",
                "N" to "N",
            )
    }

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private var pcmData: ShortArray? = null

    // Tanpura configuration
    private var currentSaFrequency: Double = DEFAULT_SA_FREQUENCY
    private var currentString1Note: String = "P" // Default to Pa
    private var currentVolume: Float = DEFAULT_VOLUME

    /**
     * Start playing the tanpura
     */
    fun start(
        saFreq: Double,
        string1: String,
        vol: Float = 0.5f,
    ) {
        Log.d(TAG, "start() called: saFreq=$saFreq, string1=$string1, vol=$vol")

        // Stop existing playback
        stop()

        currentSaFrequency = saFreq
        currentString1Note = string1
        currentVolume = vol.coerceIn(0f, 1f)

        playbackJob =
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Find closest Sa frequency in our map
                    val saName = findClosestSaName(saFreq)

                    // Map user-facing note name to filename suffix
                    val filenameSuffix = NOTE_TO_FILENAME[string1] ?: string1

                    // Construct filename: tanpura/<sa>_<suffix>.ogg
                    val filename = "tanpura/${saName}_$filenameSuffix.ogg"

                    Log.d(TAG, "Loading and decoding file: $filename")

                    // Decode OGG to PCM
                    val decodedPcm = decodeOggToPcm(filename)
                    if (decodedPcm == null || decodedPcm.isEmpty()) {
                        Log.e(TAG, "Failed to decode audio file")
                        return@launch
                    }

                    pcmData = decodedPcm
                    Log.d(TAG, "Decoded ${decodedPcm.size} samples")

                    // Calculate buffer size
                    val bufferSize =
                        AudioTrack.getMinBufferSize(
                            SAMPLE_RATE,
                            AudioFormat.CHANNEL_OUT_STEREO,
                            AudioFormat.ENCODING_PCM_16BIT,
                        ).let { minSize ->
                            maxOf(minSize * BUFFER_SIZE_MULTIPLIER, DEFAULT_BUFFER_SIZE)
                        }

                    // Create AudioTrack
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
                                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                                    .build(),
                            )
                            .setBufferSizeInBytes(bufferSize)
                            .setTransferMode(AudioTrack.MODE_STREAM)
                            .build()

                    if (audioTrack?.state != AudioTrack.STATE_INITIALIZED) {
                        Log.e(TAG, "AudioTrack initialization failed")
                        return@launch
                    }

                    audioTrack?.setVolume(currentVolume)

                    // Start playback
                    audioTrack?.play()
                    Log.d(TAG, "AudioTrack started, beginning gapless loop")

                    // Continuous gapless looping
                    var position = 0
                    val writeSize = minOf(bufferSize / WRITE_SIZE_DIVISOR, decodedPcm.size)

                    while (isActive) {
                        // Write chunk to AudioTrack
                        val written =
                            audioTrack?.write(
                                decodedPcm,
                                position,
                                minOf(writeSize, decodedPcm.size - position),
                            ) ?: 0

                        if (written > 0) {
                            position += written

                            // Loop seamlessly
                            if (position >= decodedPcm.size) {
                                position = 0
                            }
                        } else {
                            Log.w(TAG, "AudioTrack write returned $written")
                            break
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in tanpura playback", e)
                } finally {
                    // Cleanup
                    audioTrack?.apply {
                        try {
                            if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                                stop()
                            }
                            release()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error cleaning up AudioTrack", e)
                        }
                    }
                    audioTrack = null
                }
            }
    }

    /**
     * Decode OGG file to PCM samples
     */
    private fun decodeOggToPcm(filename: String): ShortArray? {
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null

        try {
            // Open asset file
            val afd = context.assets.openFd(filename)
            extractor.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()

            // Find audio track
            var trackIndex = -1
            var format: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val trackFormat = extractor.getTrackFormat(i)
                val mime = trackFormat.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    trackIndex = i
                    format = trackFormat
                    break
                }
            }

            if (trackIndex < 0 || format == null) {
                Log.e(TAG, "No audio track found")
                return null
            }

            extractor.selectTrack(trackIndex)

            // Create decoder
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            val bufferInfo = MediaCodec.BufferInfo()
            val pcmBuffer = mutableListOf<Short>()
            var isEOS = false

            // Decode loop
            while (!isEOS) {
                // Input
                if (!isEOS) {
                    val inputIndex = codec.dequeueInputBuffer(DECODER_TIMEOUT_US)
                    if (inputIndex >= 0) {
                        val inputBuffer = codec.getInputBuffer(inputIndex)
                        if (inputBuffer != null) {
                            val sampleSize = extractor.readSampleData(inputBuffer, 0)
                            if (sampleSize < 0) {
                                codec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                isEOS = true
                            } else {
                                val presentationTime = extractor.sampleTime
                                codec.queueInputBuffer(inputIndex, 0, sampleSize, presentationTime, 0)
                                extractor.advance()
                            }
                        }
                    }
                }

                // Output
                val outputIndex = codec.dequeueOutputBuffer(bufferInfo, DECODER_TIMEOUT_US)
                if (outputIndex >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputIndex)
                    if (outputBuffer != null && bufferInfo.size > 0) {
                        // Convert to ShortArray
                        val samples = ShortArray(bufferInfo.size / 2)
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.asShortBuffer().get(samples)
                        pcmBuffer.addAll(samples.toList())
                    }

                    codec.releaseOutputBuffer(outputIndex, false)

                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break
                    }
                }
            }

            return pcmBuffer.toShortArray()
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding OGG to PCM", e)
            return null
        } finally {
            codec?.stop()
            codec?.release()
            extractor.release()
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
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    stop()
                }
                release()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping AudioTrack", e)
            }
        }
        audioTrack = null
        pcmData = null
    }

    /**
     * Update tanpura parameters while playing
     */
    fun updateParameters(
        saFreq: Double,
        string1: String,
        vol: Float = 0.5f,
    ) {
        Log.d(TAG, "updateParameters() called: saFreq=$saFreq, string1=$string1, vol=$vol")

        // Check if we need to reload a different file
        val saName = findClosestSaName(saFreq)
        val currentSaName = findClosestSaName(currentSaFrequency)

        val needsReload = saName != currentSaName || string1 != currentString1Note

        if (needsReload && isPlaying()) {
            // Restart with new parameters
            start(saFreq, string1, vol)
        } else if (isPlaying()) {
            // Just update volume
            currentVolume = vol.coerceIn(0f, 1f)
            audioTrack?.setVolume(currentVolume)
        }
    }

    /**
     * Check if tanpura is currently playing
     */
    fun isPlaying(): Boolean {
        return playbackJob?.isActive == true
    }

    /**
     * Get list of available notes for string 1
     */
    fun getAvailableNotes(): List<String> {
        return AVAILABLE_STRING1_NOTES
    }

    /**
     * Find the closest Sa name for a given frequency
     */
    private fun findClosestSaName(frequency: Double): String = SaNotes.findClosestSaName(frequency)
}
