package com.hindustani.pitchdetector.data

import com.hindustani.pitchdetector.music.HindustaniNoteConverter

/**
 * Current pitch detection state
 */
data class PitchState(
    val currentNote: HindustaniNoteConverter.HindustaniNote? = null,
    val currentFrequency: Float? = null,
    val confidence: Float = 0f,
    val saNote: String = "C3",
    val saFrequency: Double = 130.81,
    val toleranceCents: Double = 15.0,
)
