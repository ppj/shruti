package com.hindustani.pitchdetector.constants

/**
 * Shared constants for vocal range limits
 * This range accommodates the full Sa range (G#2-B3) plus headroom for vocal extremes
 */
object VocalRangeConstants {
    /**
     * Minimum vocal frequency: 65 Hz (approximately C2)
     */
    const val MIN_VOCAL_FREQ = 65.0

    /**
     * Maximum vocal frequency: 1050 Hz (approximately C6)
     */
    const val MAX_VOCAL_FREQ = 1050.0
}
