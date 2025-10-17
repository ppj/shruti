package com.hindustani.pitchdetector.music

import kotlin.math.abs

/**
 * Centralized source of truth for Sa (tonic) note definitions
 * Covers typical vocal range from G#2 to A#3 (15 semitones)
 */
object SaNotes {
    /**
     * Canonical mapping of Sa note names to frequencies
     * Single source of truth for all Sa note/frequency data in the app
     */
    val SA_NOTE_TO_FREQUENCY =
        mapOf(
            "G#2" to 103.83,
            "A2" to 110.00,
            "A#2" to 116.54,
            "B2" to 123.47,
            "C3" to 130.81, // Male bass/heavy
            "C#3" to 138.59, // Male baritone
            "D3" to 146.83, // Male tenor
            "D#3" to 155.56,
            "E3" to 164.81,
            "F3" to 174.61,
            "F#3" to 185.00,
            "G3" to 196.00, // Female alto/contralto
            "G#3" to 207.65, // Female mezzo-soprano
            "A3" to 220.00, // Female soprano
            "A#3" to 233.08,
        )

    /**
     * Get list of Sa note-frequency pairs
     * Used by UI dropdowns and pickers
     */
    fun getSaOptionsInRange(): List<Pair<String, Double>> = SA_NOTE_TO_FREQUENCY.entries.map { (key, value) -> key to value }

    /**
     * Get list of Sa note names only
     */
    fun getSaOptions(): List<String> = SA_NOTE_TO_FREQUENCY.keys.toList()

    /**
     * Get frequency for a given Sa note name
     * @return Frequency in Hz, or null if note not found
     */
    fun getFrequency(noteName: String): Double? = SA_NOTE_TO_FREQUENCY[noteName]

    /**
     * Find the closest Sa note name for a given frequency
     * Used by TanpuraPlayer for file lookup
     * @return Lowercase note name (e.g., "c3", "gs2")
     */
    fun findClosestSaName(frequency: Double): String {
        val closestEntry =
            SA_NOTE_TO_FREQUENCY.entries.minByOrNull { abs(it.value - frequency) }
                ?: return "c3" // Default to C3

        // Convert to lowercase and replace # with s for filename compatibility
        return closestEntry.key.lowercase().replace("#", "s")
    }

    /**
     * Find the closest Sa note with uppercase name
     * Used by FindSaViewModel
     */
    fun findClosestNote(frequency: Double): Pair<String, Double> {
        val closestEntry =
            SA_NOTE_TO_FREQUENCY.entries.minByOrNull { abs(it.value - frequency) }
                ?: return "C3" to 130.81

        return closestEntry.key to closestEntry.value
    }
}
