package com.hindustani.pitchdetector.data

/**
 * User settings for the pitch detector
 */
data class UserSettings(
    val saNote: String = "C3",
    val saFrequency: Double = 130.81,
    val toleranceCents: Double = 15.0,
    val use22Shruti: Boolean = false,
    val isTanpuraEnabled: Boolean = false,
    val tanpuraString1: String = "P",
    val tanpuraVolume: Float = 0.5f
)
