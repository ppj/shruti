package com.hindustani.pitchdetector.music

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.abs

class SaParserTest {

    @Test
    fun `parseToFrequency returns correct frequency for C4`() {
        val frequency = SaParser.parseToFrequency("C4")

        assertThat(frequency).isNotNull()
        assertThat(frequency!!).isWithin(0.01).of(261.63)
    }

    @Test
    fun `parseToFrequency returns correct frequency for A4`() {
        // A4 is the reference frequency (440 Hz)
        val frequency = SaParser.parseToFrequency("A4")

        assertThat(frequency).isNotNull()
        assertThat(frequency!!).isWithin(0.01).of(440.0)
    }

    @Test
    fun `parseToFrequency handles sharp notes correctly`() {
        val cSharp4 = SaParser.parseToFrequency("C#4")
        val c4 = SaParser.parseToFrequency("C4")

        assertThat(cSharp4).isNotNull()
        assertThat(c4).isNotNull()

        // C# should be higher than C
        assertThat(cSharp4!!).isGreaterThan(c4!!)

        // Should be approximately a semitone higher (2^(1/12) ≈ 1.0595)
        val ratio = cSharp4 / c4
        assertThat(ratio).isWithin(0.01).of(1.0595)
    }

    @Test
    fun `parseToFrequency handles flat notes correctly`() {
        val dFlat4 = SaParser.parseToFrequency("Db4")
        val cSharp4 = SaParser.parseToFrequency("C#4")

        assertThat(dFlat4).isNotNull()
        assertThat(cSharp4).isNotNull()

        // Db and C# should be enharmonic (same frequency)
        assertThat(dFlat4!!).isWithin(0.01).of(cSharp4!!)
    }

    @Test
    fun `parseToFrequency handles different octaves correctly`() {
        val c3 = SaParser.parseToFrequency("C3")
        val c4 = SaParser.parseToFrequency("C4")
        val c5 = SaParser.parseToFrequency("C5")

        assertThat(c3).isNotNull()
        assertThat(c4).isNotNull()
        assertThat(c5).isNotNull()

        // Each octave should double the frequency
        assertThat(c4!!).isWithin(0.01).of(c3!! * 2.0)
        assertThat(c5!!).isWithin(0.01).of(c4 * 2.0)
    }

    @Test
    fun `parseToFrequency returns null for invalid note name`() {
        val frequency = SaParser.parseToFrequency("H4")
        assertThat(frequency).isNull()
    }

    @Test
    fun `parseToFrequency returns null for invalid format`() {
        val invalidFormats = listOf("C", "4C", "CC4", "C44", "C#", "")

        invalidFormats.forEach { format ->
            val frequency = SaParser.parseToFrequency(format)
            assertThat(frequency).isNull()
        }
    }

    @Test
    fun `parseToFrequency handles whitespace correctly`() {
        val frequency = SaParser.parseToFrequency(" C4 ")

        assertThat(frequency).isNotNull()
        assertThat(frequency!!).isWithin(0.01).of(261.63)
    }

    @Test
    fun `getSaOptionsInRange returns list of Sa frequencies in vocal range`() {
        val saOptions = SaParser.getSaOptionsInRange()

        // Should have multiple entries
        assertThat(saOptions).isNotEmpty()

        // Each entry should have a valid note and frequency
        saOptions.forEach { (note, frequency) ->
            assertThat(SaParser.parseToFrequency(note)).isNotNull()
            assertThat(frequency).isGreaterThan(0.0)
        }

        // Should include C3 (common Sa)
        val c3Entry = saOptions.find { it.first == "C3" }
        assertThat(c3Entry).isNotNull()
        assertThat(c3Entry!!.second).isWithin(0.01).of(130.81)
    }

    @Test
    fun `frequency calculation uses equal temperament`() {
        // Test the 12-tone equal temperament formula
        // Each semitone should be 2^(1/12) times the previous
        val c4 = SaParser.parseToFrequency("C4")!!

        for (semitone in 1..12) {
            val expectedRatio = Math.pow(2.0, semitone / 12.0)
            val expectedFrequency = c4 * expectedRatio

            // Get the note at this semitone
            val notes = listOf("C4", "C#4", "D4", "D#4", "E4", "F4", "F#4", "G4", "G#4", "A4", "A#4", "B4", "C5")
            val actualFrequency = SaParser.parseToFrequency(notes[semitone])!!

            assertThat(actualFrequency).isWithin(0.01).of(expectedFrequency)
        }
    }
}
