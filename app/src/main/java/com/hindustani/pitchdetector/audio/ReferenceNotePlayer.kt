package com.hindustani.pitchdetector.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import com.hindustani.pitchdetector.music.SaNotes

/**
 * Plays short reference note audio samples for training mode
 *
 * Uses MediaPlayer for simple one-shot playback of pre-recorded swar plucks.
 * Audio files are loaded from assets/plucks/ directory with naming pattern:
 * [sa_note_name][octave]_[swar_index]_[swar_name].ogg (e.g., c3_5_G.ogg)
 */
class ReferenceNotePlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        private const val TAG = "ReferenceNotePlayer"

        /**
         * Map swar names to their file name representation
         * Format: [index]_[swar] (e.g., "1_S", "5_G")
         */
        private val swarFileMap =
            mapOf(
                "S" to "1_S",
                "r" to "2_r",
                "R" to "3_R",
                "g" to "4_g",
                "G" to "5_G",
                "m" to "6_m",
                "M" to "7_M",
                "P" to "8_P",
                "d" to "9_d",
                "D" to "10_D",
                "n" to "11_n",
                "N" to "12_N",
            )
    }

    /**
     * Play a reference note for the given swar at the specified Sa frequency
     *
     * @param swar The target swar to play (e.g., "S", "R", "G")
     * @param saFrequency The base Sa frequency in Hz (determines which audio file to use)
     */
    fun play(
        swar: String,
        saFrequency: Double,
    ) {
        stop() // Stop any previous playback

        val fileName = getNoteFilename(swar, saFrequency)
        if (fileName == null) {
            Log.e(TAG, "Could not generate filename for swar=$swar, saFrequency=$saFrequency")
            return
        }

        try {
            val afd = context.assets.openFd(fileName)

            // Use USAGE_ASSISTANCE_SONIFICATION to prevent audio ducking of background music (tanpura)
            // This tells Android that reference notes are short feedback sounds that should mix with
            // other media rather than ducking or pausing it
            val audioAttributes =
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

            mediaPlayer =
                MediaPlayer().apply {
                    setAudioAttributes(audioAttributes)
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    setOnCompletionListener {
                        release()
                    }
                    setOnPreparedListener {
                        start()
                    }
                    setOnErrorListener { mp, what, extra ->
                        Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                        release()
                        true // Error handled
                    }
                    prepareAsync()
                }
            afd.close()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play reference note: $fileName", e)
            release()
        }
    }

    /**
     * Stop playback immediately
     */
    fun stop() {
        try {
            mediaPlayer?.stop()
        } catch (e: IllegalStateException) {
            Log.w(TAG, "MediaPlayer was not in a valid state to stop", e)
        }
        release()
    }

    /**
     * Release MediaPlayer resources
     */
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    /**
     * Check if a note is currently playing
     */
    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    /**
     * Generate the asset filename for a given swar and Sa frequency
     *
     * @param swar The target swar (e.g., "G")
     * @param saFrequency The base Sa frequency in Hz
     * @return The asset path (e.g., "plucks/c3_5_G.ogg") or null if invalid
     */
    private fun getNoteFilename(
        swar: String,
        saFrequency: Double,
    ): String? {
        val saName = SaNotes.findClosestSaName(saFrequency)
        val swarFilePart = swarFileMap[swar]

        if (swarFilePart == null) {
            Log.e(TAG, "Unknown swar: $swar")
            return null
        }

        return "plucks/${saName}_$swarFilePart.ogg"
    }
}
