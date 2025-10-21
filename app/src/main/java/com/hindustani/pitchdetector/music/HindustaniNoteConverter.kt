package com.hindustani.pitchdetector.music

import kotlin.math.abs
import kotlin.math.ln

/**
 * Converts frequencies to Hindustani notation (S r R g G m M P d D n N)
 * Supports both 12-note Just Intonation and 22-shruti systems
 */
class HindustaniNoteConverter(
    private val saFrequency: Double,
    private val toleranceCents: Double = 15.0,
    private val use22Shruti: Boolean = false,
) {
    data class HindustaniNote(
        val swara: String, // S, r, R, g, G, m, M, P, d, D, n, N (or with shruti numbers)
        val octave: Octave, // Which saptak (octave)
        val centsDeviation: Double, // How far from perfect pitch
        val isPerfect: Boolean, // Within tolerance?
        val isFlat: Boolean, // Below tolerance?
        val isSharp: Boolean, // Above tolerance?
    )

    enum class Octave {
        MANDRA, // Lower octave (dot below)
        MADHYA, // Middle octave (plain)
        TAAR, // Upper octave (dot above)
    }

    // Just Intonation ratios (12 basic notes)
    private val justIntonationRatios =
        mapOf(
            "S" to 1.0 / 1.0, // Shadaj (Sa)
            "r" to 16.0 / 15.0, // Komal Rishabh
            "R" to 9.0 / 8.0, // Shuddha Rishabh
            "g" to 6.0 / 5.0, // Komal Gandhar
            "G" to 5.0 / 4.0, // Shuddha Gandhar
            "m" to 4.0 / 3.0, // Shuddha Madhyam
            "M" to 45.0 / 32.0, // Tivra Madhyam
            "P" to 3.0 / 2.0, // Pancham
            "d" to 8.0 / 5.0, // Komal Dhaivat
            "D" to 5.0 / 3.0, // Shuddha Dhaivat
            "n" to 16.0 / 9.0, // Komal Nishad
            "N" to 15.0 / 8.0, // Shuddha Nishad
        )

    // 22-shruti system (microtonal variations)
    private val shruti22Ratios =
        mapOf(
            "S" to 1.0, // Shadaj
            "r¹" to 256.0 / 243.0, // Komal Re shruti 1
            "r²" to 16.0 / 15.0, // Komal Re shruti 2
            "R¹" to 10.0 / 9.0, // Shuddha Re shruti 1
            "R²" to 9.0 / 8.0, // Shuddha Re shruti 2
            "g¹" to 32.0 / 27.0, // Komal Ga shruti 1
            "g²" to 6.0 / 5.0, // Komal Ga shruti 2
            "G¹" to 5.0 / 4.0, // Shuddha Ga shruti 1
            "G²" to 81.0 / 64.0, // Shuddha Ga shruti 2
            "m" to 4.0 / 3.0, // Shuddha Ma
            "M¹" to 27.0 / 20.0, // Tivra Ma shruti 1
            "M²" to 45.0 / 32.0, // Tivra Ma shruti 2
            "P" to 3.0 / 2.0, // Pancham
            "d¹" to 128.0 / 81.0, // Komal Dha shruti 1
            "d²" to 8.0 / 5.0, // Komal Dha shruti 2
            "D¹" to 5.0 / 3.0, // Shuddha Dha shruti 1
            "D²" to 27.0 / 16.0, // Shuddha Dha shruti 2
            "n¹" to 16.0 / 9.0, // Komal Ni shruti 1
            "n²" to 9.0 / 5.0, // Komal Ni shruti 2
            "N¹" to 15.0 / 8.0, // Shuddha Ni shruti 1
            "N²" to 243.0 / 128.0, // Shuddha Ni shruti 2
        )

    /**
     * Convert a frequency to Hindustani notation with octave detection
     */
    fun convertFrequency(frequency: Double): HindustaniNote {
        val ratios = if (use22Shruti) shruti22Ratios else justIntonationRatios

        // Find closest note across all octaves
        var closestSwara = "S"
        var closestOctave = Octave.MADHYA
        var minCentsDiff = Double.MAX_VALUE
        var bestTargetFreq = saFrequency

        // Check notes in three octaves: mandra (÷2), madhya (×1), taar (×2)
        val octaveMultipliers =
            listOf(
                0.5 to Octave.MANDRA, // Lower octave
                1.0 to Octave.MADHYA, // Middle octave
                2.0 to Octave.TAAR, // Upper octave
            )

        for ((swara, swaraRatio) in ratios) {
            for ((multiplier, octave) in octaveMultipliers) {
                val targetFreq = saFrequency * swaraRatio * multiplier
                val cents = 1200 * log2(frequency / targetFreq)
                val absCents = abs(cents)

                if (absCents < minCentsDiff) {
                    minCentsDiff = absCents
                    closestSwara = swara
                    closestOctave = octave
                    bestTargetFreq = targetFreq
                }
            }
        }

        // Calculate deviation from the closest note
        val centsDeviation = 1200 * log2(frequency / bestTargetFreq)

        return HindustaniNote(
            swara = closestSwara,
            octave = closestOctave,
            centsDeviation = centsDeviation,
            isPerfect = abs(centsDeviation) <= toleranceCents,
            isFlat = centsDeviation < -toleranceCents,
            isSharp = centsDeviation > toleranceCents,
        )
    }

    private fun log2(value: Double): Double = ln(value) / ln(2.0)
}

/**
 * Extension function to convert HindustaniNote to display string with octave notation
 * - Mandra saptak (lower): .S, .R, .G
 * - Madhya saptak (middle): S, R, G
 * - Taar saptak (upper): S', R', G'
 */
fun HindustaniNoteConverter.HindustaniNote.toDisplayString(): String =
    when (octave) {
        HindustaniNoteConverter.Octave.MANDRA -> ".$swara"
        HindustaniNoteConverter.Octave.MADHYA -> swara
        HindustaniNoteConverter.Octave.TAAR -> "$swara'"
    }
