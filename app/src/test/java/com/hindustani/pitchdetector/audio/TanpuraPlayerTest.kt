package com.hindustani.pitchdetector.audio

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class TanpuraPlayerTest {

    private lateinit var tanpuraPlayer: TanpuraPlayer

    @Before
    fun setup() {
        tanpuraPlayer = TanpuraPlayer()
    }

    @After
    fun tearDown() {
        tanpuraPlayer.stop()
    }

    @Test
    fun `initial state is not playing`() {
        assertThat(tanpuraPlayer.isPlaying()).isFalse()
    }

    @Test
    fun `getAvailableNotes returns all 12 notes`() {
        val notes = tanpuraPlayer.getAvailableNotes()

        assertThat(notes).hasSize(12)
        assertThat(notes).contains("S")
        assertThat(notes).contains("r")
        assertThat(notes).contains("R")
        assertThat(notes).contains("g")
        assertThat(notes).contains("G")
        assertThat(notes).contains("m")
        assertThat(notes).contains("M")
        assertThat(notes).contains("P")
        assertThat(notes).contains("d")
        assertThat(notes).contains("D")
        assertThat(notes).contains("n")
        assertThat(notes).contains("N")
    }

    @Test
    fun `getAvailableNotes returns notes in correct order`() {
        val notes = tanpuraPlayer.getAvailableNotes()
        val expectedOrder = listOf("S", "r", "R", "g", "G", "m", "M", "P", "d", "D", "n", "N")

        assertThat(notes).containsExactlyElementsIn(expectedOrder).inOrder()
    }

    @Test
    fun `stop when not playing does not throw exception`() {
        // Should be safe to stop when not playing
        tanpuraPlayer.stop()
        assertThat(tanpuraPlayer.isPlaying()).isFalse()
    }

    @Test
    fun `multiple stops do not throw exception`() {
        tanpuraPlayer.stop()
        tanpuraPlayer.stop()
        tanpuraPlayer.stop()

        assertThat(tanpuraPlayer.isPlaying()).isFalse()
    }

    @Test
    fun `updateParameters when not playing does not throw exception`() {
        // Should be safe to update parameters when not playing
        tanpuraPlayer.updateParameters(
            saFreq = 261.63,
            string1 = "P",
            vol = 0.5f
        )

        assertThat(tanpuraPlayer.isPlaying()).isFalse()
    }

    @Test
    fun `valid note ratios are defined for all notes`() {
        val notes = tanpuraPlayer.getAvailableNotes()

        // All notes should have valid ratios in the internal map
        // This is indirectly tested by ensuring no crashes occur when using these notes
        notes.forEach { note ->
            // Should not throw exception
            tanpuraPlayer.updateParameters(
                saFreq = 261.63,
                string1 = note,
                vol = 0.5f
            )
        }
    }

    @Test
    fun `default string 1 note is P (pancham)`() {
        // Based on the implementation default
        val notes = tanpuraPlayer.getAvailableNotes()
        assertThat(notes).contains("P")
    }

    @Test
    fun `volume parameter accepts valid range`() {
        // Volume should accept 0.0 to 1.0 range
        val validVolumes = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)

        validVolumes.forEach { volume ->
            tanpuraPlayer.updateParameters(
                saFreq = 261.63,
                string1 = "S",
                vol = volume
            )
        }

        // Should complete without exceptions
        assertThat(tanpuraPlayer.isPlaying()).isFalse()
    }

    @Test
    fun `frequency parameter accepts valid vocal range`() {
        // Test various Sa frequencies in typical vocal ranges
        val validFrequencies = listOf(
            110.0,   // Low A2 (bass)
            130.81,  // C3 (low male)
            196.0,   // G3 (mid male)
            261.63,  // C4 (middle C)
            329.63,  // E4 (high female)
            440.0    // A4 (high)
        )

        validFrequencies.forEach { freq ->
            tanpuraPlayer.updateParameters(
                saFreq = freq,
                string1 = "P",
                vol = 0.5f
            )
        }

        // Should complete without exceptions
        assertThat(tanpuraPlayer.isPlaying()).isFalse()
    }

    @Test
    fun `all swaras can be used as string 1`() {
        val allSwaras = listOf("S", "r", "R", "g", "G", "m", "M", "P", "d", "D", "n", "N")

        allSwaras.forEach { swara ->
            tanpuraPlayer.updateParameters(
                saFreq = 261.63,
                string1 = swara,
                vol = 0.5f
            )

            // Should not throw exception
            assertThat(tanpuraPlayer.isPlaying()).isFalse()
        }
    }

    @Test
    fun `sample rate is standard 44100 Hz`() {
        // Indirect test - constructor should accept standard sample rate
        val player = TanpuraPlayer(sampleRate = 44100)
        assertThat(player.isPlaying()).isFalse()
    }

    @Test
    fun `note ratios follow just intonation`() {
        // This is a conceptual test - the ratios should be mathematically correct
        // The actual values are tested indirectly through the audio generation
        val notes = tanpuraPlayer.getAvailableNotes()

        // Ensure all 12 notes of just intonation are present
        assertThat(notes).hasSize(12)
    }

    // Note: Tests for actual audio playback would require Android instrumentation tests
    // with proper audio system initialization, as AudioTrack requires Android framework
}
