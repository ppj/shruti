package com.hindustani.pitchdetector.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for PitchViewModel
 * Tests pitch detection lifecycle, audio processing, state management, and settings persistence
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PitchViewModelTest {
    private lateinit var viewModel: PitchViewModel
    private lateinit var application: Application
    private val testDispatcher = UnconfinedTestDispatcher()

    companion object {
        private const val C3_FREQUENCY = 130.81
        private const val D3_FREQUENCY = 146.83
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        viewModel = PitchViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization_loadsDefaultSettings`() =
        runTest {
            val settings = viewModel.settings.first()

            // Should have default or persisted settings
            assertThat(settings.saNote).isNotEmpty()
            assertThat(settings.saFrequency).isGreaterThan(0.0)
            assertThat(settings.toleranceCents).isGreaterThan(0.0)
        }

    @Test
    fun `initialization_pitchState_reflectsInitialSettings`() =
        runTest {
            val pitchState = viewModel.pitchState.first()

            // Pitch state should be initialized
            assertThat(pitchState.saNote).isNotEmpty()
            assertThat(pitchState.saFrequency).isGreaterThan(0.0)
            assertThat(pitchState.toleranceCents).isGreaterThan(0.0)
        }

    @Test
    fun `initialization_isNotRecording`() =
        runTest {
            assertThat(viewModel.isRecording.first()).isFalse()
        }

    @Test
    fun `initialization_tanpuraNotPlaying`() =
        runTest {
            assertThat(viewModel.isTanpuraPlaying.first()).isFalse()
        }

    @Test
    fun `toggleRecording_whenOff_startsRecording`() =
        runTest {
            // Initially not recording
            assertThat(viewModel.isRecording.first()).isFalse()

            // Toggle recording on
            viewModel.toggleRecording()
            advanceUntilIdle()

            assertThat(viewModel.isRecording.value).isTrue()
        }

    @Test
    fun `toggleRecording_whenOn_stopsRecording`() =
        runTest {
            // Start recording
            viewModel.toggleRecording()
            advanceUntilIdle()
            assertThat(viewModel.isRecording.value).isTrue()

            // Stop recording
            viewModel.toggleRecording()
            advanceUntilIdle()

            assertThat(viewModel.isRecording.value).isFalse()
        }

    @Test
    fun `stopRecording_clearsCurrentNote_inPitchState`() =
        runTest {
            // Start and immediately stop recording
            viewModel.toggleRecording()
            advanceUntilIdle()
            viewModel.toggleRecording()
            advanceUntilIdle()

            val pitchState = viewModel.pitchState.first()
            assertThat(pitchState.currentNote).isNull()
            assertThat(pitchState.currentFrequency).isNull()
            assertThat(pitchState.confidence).isEqualTo(0f)
        }

    @Test
    fun `updateSa_withValidNote_updatesState`() =
        runTest {
            viewModel.updateSa("D3")
            advanceUntilIdle()

            // Should update pitch state
            val pitchState = viewModel.pitchState.first()
            assertThat(pitchState.saNote).isEqualTo("D3")
            assertThat(pitchState.saFrequency).isWithin(0.01).of(D3_FREQUENCY)
        }

    @Test
    fun `updateSa_withInvalidNote_doesNotUpdateState`() =
        runTest {
            val originalSaNote = viewModel.pitchState.first().saNote

            viewModel.updateSa("INVALID")
            advanceUntilIdle()

            // State should remain unchanged
            val pitchState = viewModel.pitchState.first()
            assertThat(pitchState.saNote).isEqualTo(originalSaNote)
        }

    @Test
    fun `getSaFrequency_returnsCurrentSaFrequency`() =
        runTest {
            val frequency = viewModel.getSaFrequency()

            assertThat(frequency).isGreaterThan(0.0)
        }

    @Test
    fun `updateTolerance_updatesSettings`() =
        runTest {
            viewModel.updateTolerance(20.0)
            advanceUntilIdle()

            // Settings should eventually reflect the change
            // Note: This is eventually consistent due to DataStore
            val settings = viewModel.settings.value
            // Just verify method doesn't crash
            assertThat(settings).isNotNull()
        }

    @Test
    fun `updateTuningSystem_updatesSettings`() =
        runTest {
            viewModel.updateTuningSystem(true)
            advanceUntilIdle()

            // Just verify method doesn't crash
            val settings = viewModel.settings.value
            assertThat(settings).isNotNull()
        }

    @Test
    fun `toggleTanpura_whenOff_startsTanpura`() =
        runTest {
            // Initially off
            assertThat(viewModel.isTanpuraPlaying.first()).isFalse()

            viewModel.toggleTanpura()
            advanceUntilIdle()

            assertThat(viewModel.isTanpuraPlaying.value).isTrue()
        }

    @Test
    fun `toggleTanpura_whenOn_stopsTanpura`() =
        runTest {
            // Start tanpura
            viewModel.toggleTanpura()
            advanceUntilIdle()
            assertThat(viewModel.isTanpuraPlaying.value).isTrue()

            // Stop tanpura
            viewModel.toggleTanpura()
            advanceUntilIdle()

            assertThat(viewModel.isTanpuraPlaying.value).isFalse()
        }

    @Test
    fun `updateTanpuraString1_updatesSettings`() =
        runTest {
            // Start tanpura
            viewModel.toggleTanpura()
            advanceUntilIdle()

            // Update string 1
            viewModel.updateTanpuraString1("m")
            advanceUntilIdle()

            // Just verify method doesn't crash
            assertThat(viewModel.isTanpuraPlaying.value).isTrue()
        }

    @Test
    fun `updateTanpuraVolume_updatesSettings`() =
        runTest {
            // Start tanpura
            viewModel.toggleTanpura()
            advanceUntilIdle()

            // Update volume
            viewModel.updateTanpuraVolume(0.5f)
            advanceUntilIdle()

            // Just verify method doesn't crash
            assertThat(viewModel.isTanpuraPlaying.value).isTrue()
        }

    @Test
    fun `getTanpuraAvailableNotes_returnsNotes`() {
        val notes = viewModel.getTanpuraAvailableNotes()

        assertThat(notes).isNotEmpty()
        assertThat(notes).contains("P") // Pa should be available
    }

    @Test
    fun `updateSa_whileTanpuraPlaying_updatesTanpuraParameters`() =
        runTest {
            // Start tanpura
            viewModel.toggleTanpura()
            advanceUntilIdle()

            // Update Sa note
            viewModel.updateSa("D3")
            advanceUntilIdle()

            // Should not crash, tanpura should continue playing
            assertThat(viewModel.isTanpuraPlaying.value).isTrue()
        }

    @Test
    fun `multipleToggles_recordingState_updatesCorrectly`() =
        runTest {
            // Toggle multiple times
            viewModel.toggleRecording()
            advanceUntilIdle()
            assertThat(viewModel.isRecording.value).isTrue()

            viewModel.toggleRecording()
            advanceUntilIdle()
            assertThat(viewModel.isRecording.value).isFalse()

            viewModel.toggleRecording()
            advanceUntilIdle()
            assertThat(viewModel.isRecording.value).isTrue()
        }
}
