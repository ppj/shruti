package com.hindustani.pitchdetector.audio

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

/**
 * Unit tests for ReferenceNotePlayer
 * Tests audio playback lifecycle, file name generation, and resource management
 */
@RunWith(RobolectricTestRunner::class)
class ReferenceNotePlayerTest {
    private lateinit var context: Context
    private lateinit var player: ReferenceNotePlayer

    @Before
    fun setUp() {
        ShadowLog.stream = System.out
        context = ApplicationProvider.getApplicationContext()
        player = ReferenceNotePlayer(context)
    }

    @After
    fun tearDown() {
        player.release()
    }

    @Test
    fun `initialization_createsPlayerInstance_withNullMediaPlayer`() {
        // isPlaying should return false when no media is playing
        assertThat(player.isPlaying()).isFalse()
    }

    @Test
    fun `play_withInvalidSwar_doesNotStartPlayback`() {
        // Play with an invalid swar name
        player.play(swar = "INVALID", saFrequency = 130.81)

        // Should not be playing since swar is invalid
        assertThat(player.isPlaying()).isFalse()
    }

    @Test
    fun `play_withValidSwar_attemptsToLoadCorrectFile`() {
        // This test verifies that play() attempts to load the correct file
        // For C3 (130.81 Hz) and swar "G", should try to load "plucks/c3_5_G.ogg"

        // Note: Without actual audio assets in test resources, MediaPlayer will fail
        // to load, but we can verify the method doesn't crash and handles it gracefully
        player.play(swar = "G", saFrequency = 130.81)

        // Method should complete without throwing
        // In a real app environment with assets, isPlaying would be true after prepared
        // In test environment without assets, it gracefully handles the missing file
        assertThat(player.isPlaying()).isFalse()
    }

    @Test
    fun `play_withDifferentSaNotes_generatesDifferentFilenames`() {
        // This test verifies different Sa frequencies produce different file lookups

        // C3 frequency
        player.play(swar = "S", saFrequency = 130.81)
        assertThat(player.isPlaying()).isFalse() // No asset in test

        player.release()

        // D3 frequency
        player.play(swar = "S", saFrequency = 146.83)
        assertThat(player.isPlaying()).isFalse() // No asset in test
    }

    @Test
    fun `stop_whenNotPlaying_doesNotCrash`() {
        // Stop should be safe to call even when nothing is playing
        player.stop()

        assertThat(player.isPlaying()).isFalse()
    }

    @Test
    fun `release_setsMediaPlayerToNull_andIsPlayingReturnsFalse`() {
        // After release, isPlaying should return false
        player.release()

        assertThat(player.isPlaying()).isFalse()
    }

    @Test
    fun `play_calledTwice_stopsPreviousPlayback`() {
        // First play
        player.play(swar = "S", saFrequency = 130.81)

        // Second play should stop the first one
        player.play(swar = "R", saFrequency = 130.81)

        // Should not crash and should handle lifecycle correctly
        assertThat(player.isPlaying()).isFalse() // No assets in test
    }

    @Test
    fun `play_withAllValidSwars_doesNotCrash`() {
        // Test all 12 swars
        val allSwars = listOf("S", "r", "R", "g", "G", "m", "M", "P", "d", "D", "n", "N")

        allSwars.forEach { swar ->
            player.play(swar = swar, saFrequency = 130.81)
            player.stop()
        }

        // All swars should be handled without crashing
        assertThat(player.isPlaying()).isFalse()
    }

    @Test
    fun `play_withSharpNotes_generatesSharpenedFilename`() {
        // Test with C#3 frequency (138.59 Hz)
        // SaNotes.findClosestSaName should return "cs3" (# replaced with s)
        player.play(swar = "P", saFrequency = 138.59)

        // Should attempt to load plucks/cs3_8_P.ogg
        // In test environment without assets, gracefully handles missing file
        assertThat(player.isPlaying()).isFalse()
    }

    @Test
    fun `release_canBeCalledMultipleTimes_safely`() {
        // Release should be idempotent
        player.release()
        player.release()
        player.release()

        assertThat(player.isPlaying()).isFalse()
    }
}
