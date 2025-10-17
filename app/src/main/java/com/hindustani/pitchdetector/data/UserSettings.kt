package com.hindustani.pitchdetector.data

/**
 * User settings for the pitch detector
 */
data class UserSettings(
    val saNote: String = DEFAULT_SA_NOTE,
    val saFrequency: Double = DEFAULT_SA_FREQUENCY,
    val toleranceCents: Double = DEFAULT_TOLERANCE_CENTS,
    val use22Shruti: Boolean = DEFAULT_USE_22_SHRUTI,
    val isTanpuraEnabled: Boolean = DEFAULT_TANPURA_ENABLED,
    val tanpuraString1: String = DEFAULT_TANPURA_STRING1,
    val tanpuraVolume: Float = DEFAULT_TANPURA_VOLUME,
) {
    companion object {
        // Default values - single source of truth
        const val DEFAULT_SA_NOTE = "C3"
        const val DEFAULT_SA_FREQUENCY = 130.81
        const val DEFAULT_TOLERANCE_CENTS = 15.0
        const val DEFAULT_USE_22_SHRUTI = false
        const val DEFAULT_TANPURA_ENABLED = false
        const val DEFAULT_TANPURA_STRING1 = "P"
        const val DEFAULT_TANPURA_VOLUME = 0.5f
    }
}
