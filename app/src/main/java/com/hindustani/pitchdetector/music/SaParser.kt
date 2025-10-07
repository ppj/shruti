package com.hindustani.pitchdetector.music

import kotlin.math.pow

/**
 * Parses Western notation (e.g., C4, A#3, Bb4) to frequency in Hz
 */
object SaParser {

    private val noteToSemitone = mapOf(
        "C" to 0, "C#" to 1, "Db" to 1,
        "D" to 2, "D#" to 3, "Eb" to 3,
        "E" to 4,
        "F" to 5, "F#" to 6, "Gb" to 6,
        "G" to 7, "G#" to 8, "Ab" to 8,
        "A" to 9, "A#" to 10, "Bb" to 10,
        "B" to 11
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
     * Get the note name without octave
     */
    fun getNoteName(westernNote: String): String? {
        val regex = Regex("([A-G][#b]?)([0-9])")
        val match = regex.matchEntire(westernNote.trim()) ?: return null
        return match.groupValues[1]
    }

    /**
     * Get the octave number
     */
    fun getOctave(westernNote: String): Int? {
        val regex = Regex("([A-G][#b]?)([0-9])")
        val match = regex.matchEntire(westernNote.trim()) ?: return null
        return match.groupValues[2].toIntOrNull()
    }

    /**
     * Validate Western notation format
     */
    fun isValidNotation(westernNote: String): Boolean {
        val regex = Regex("([A-G][#b]?)([0-9])")
        return regex.matches(westernNote.trim())
    }

    /**
     * Common Sa values for Hindustani classical music
     */
    fun getCommonSaValues(): List<Pair<String, Double>> = listOf(
        "C3" to parseToFrequency("C3")!!,
        "C#3" to parseToFrequency("C#3")!!,
        "D3" to parseToFrequency("D3")!!,
        "D#3" to parseToFrequency("D#3")!!,
        "E3" to parseToFrequency("E3")!!,
        "F3" to parseToFrequency("F3")!!,
        "F#3" to parseToFrequency("F#3")!!,
        "G3" to parseToFrequency("G3")!!,
        "G#3" to parseToFrequency("G#3")!!,
        "A3" to parseToFrequency("A3")!!,
        "A#3" to parseToFrequency("A#3")!!,
        "B3" to parseToFrequency("B3")!!,
        "C4" to parseToFrequency("C4")!!,
        "C#4" to parseToFrequency("C#4")!!,
        "D4" to parseToFrequency("D4")!!,
        "D#4" to parseToFrequency("D#4")!!
    )

    /**
     * Get Sa options in typical vocal range (F2 to B3)
     * Returns list of note-frequency pairs for dropdown selector
     */
    fun getSaOptionsInRange(): List<Pair<String, Double>> = listOf(
        "F2" to parseToFrequency("F2")!!,
        "F#2" to parseToFrequency("F#2")!!,
        "G2" to parseToFrequency("G2")!!,
        "G#2" to parseToFrequency("G#2")!!,
        "A2" to parseToFrequency("A2")!!,
        "A#2" to parseToFrequency("A#2")!!,
        "B2" to parseToFrequency("B2")!!,
        "C3" to parseToFrequency("C3")!!,
        "C#3" to parseToFrequency("C#3")!!,
        "D3" to parseToFrequency("D3")!!,
        "D#3" to parseToFrequency("D#3")!!,
        "E3" to parseToFrequency("E3")!!,
        "F3" to parseToFrequency("F3")!!,
        "F#3" to parseToFrequency("F#3")!!,
        "G3" to parseToFrequency("G3")!!,
        "G#3" to parseToFrequency("G#3")!!,
        "A3" to parseToFrequency("A3")!!,
        "A#3" to parseToFrequency("A#3")!!,
        "B3" to parseToFrequency("B3")!!
    )
}
