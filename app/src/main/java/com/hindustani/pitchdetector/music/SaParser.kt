package com.hindustani.pitchdetector.music

import kotlin.math.pow

/**
 * Parses Western notation (e.g., C4, A#3, Bb4) to frequency in Hz
 */
object SaParser {
    private val noteToSemitone =
        mapOf(
            "C" to 0, "C#" to 1, "Db" to 1,
            "D" to 2, "D#" to 3, "Eb" to 3,
            "E" to 4,
            "F" to 5, "F#" to 6, "Gb" to 6,
            "G" to 7, "G#" to 8, "Ab" to 8,
            "A" to 9, "A#" to 10, "Bb" to 10,
            "B" to 11,
        )

    /**
     * Parse Western notation to frequency
     * @param westernNote Format: "C4", "C#3", "Ab4", etc.
     * @return Frequency in Hz, or null if parsing fails
     */
    fun parseToFrequency(westernNote: String): Double? {
        // Regex to match note format: Note (with optional sharp/flat) + Octave
        val regex = Regex("([A-G][#b]?)([0-9])")
        val match = regex.matchEntire(westernNote.trim()) ?: return null

        val note = match.groupValues[1]
        val octave = match.groupValues[2].toIntOrNull() ?: return null

        val semitone = noteToSemitone[note] ?: return null

        // Calculate MIDI note number
        // MIDI note 69 = A4 = 440 Hz
        val midiNote = (octave + 1) * 12 + semitone
        val semitonesFromA4 = midiNote - 69

        // Calculate frequency using equal temperament formula
        return 440.0 * 2.0.pow(semitonesFromA4 / 12.0)
    }

    /**
     * Get Sa options in typical vocal range (G#2 to A#3)
     * Returns list of note-frequency pairs for dropdown selector
     */
    fun getSaOptionsInRange(): List<Pair<String, Double>> = SaNotes.getSaOptionsInRange()

    /**
     * Get Sa options in typical vocal range (G#2 to A#3)
     * Returns list of note names for dropdown selector
     */
    fun getSaOptions(): List<String> = SaNotes.getSaOptions()
}
