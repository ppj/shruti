package com.hindustani.pitchdetector.ui

import android.Manifest
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.hindustani.pitchdetector.data.PitchState
import com.hindustani.pitchdetector.data.UserSettings
import com.hindustani.pitchdetector.music.HindustaniNoteConverter
import com.hindustani.pitchdetector.testutil.TestViewModelFactory
import com.hindustani.pitchdetector.viewmodel.PitchViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO)

    @Test
    fun mainScreen_displaysAllElements() {
        val viewModel = TestViewModelFactory.createPitchViewModel()
        composeTestRule.setContent {
            MainScreen(viewModel = viewModel, onNavigateToSettings = {}, onNavigateToFindSa = {}, onNavigateToTraining = {})
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Sa:", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Listen").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Tanpura").assertIsDisplayed()
    }

    @Test
    fun mainScreen_saCanBeChanged() {
        val viewModel = TestViewModelFactory.createPitchViewModel()
        composeTestRule.setContent {
            MainScreen(viewModel = viewModel, onNavigateToSettings = {}, onNavigateToFindSa = {}, onNavigateToTraining = {})
        }

        composeTestRule.waitForIdle()

        // Click on A3 key in the piano keyboard
        composeTestRule.onNodeWithText("A3").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Sa: A3", substring = true).assertIsDisplayed()
    }

    @Test
    fun mainScreen_saSelectionIsPersisted() {
        val viewModel = TestViewModelFactory.createPitchViewModel()

        composeTestRule.setContent {
            MainScreen(viewModel = viewModel, onNavigateToSettings = {}, onNavigateToFindSa = {}, onNavigateToTraining = {})
        }

        composeTestRule.waitForIdle()

        // Click on D3 key in the piano keyboard
        composeTestRule.onNodeWithText("D3").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Sa: D3", substring = true).assertIsDisplayed()

        // Verify that the Sa persisted in settings
        val persistedSa = runBlocking { viewModel.settings.first().saNote }
        assert(persistedSa == "D3") {
            "Sa should be persisted. Expected: D3, Got: $persistedSa"
        }
    }

    @Test
    fun mainScreen_displaysSwarWithOctaveNotation_mandraSaptak() {
        val mandraNote =
            HindustaniNoteConverter.HindustaniNote(
                swar = "S",
                octave = HindustaniNoteConverter.Octave.MANDRA,
                centsDeviation = 0.0,
                isPerfect = true,
                isFlat = false,
                isSharp = false,
            )

        val pitchState =
            PitchState(
                saNote = "C3",
                currentNote = mandraNote,
                confidence = 0.9f,
                toleranceCents = 15.0,
            )

        val viewModel = createMockViewModel(pitchState)

        composeTestRule.setContent {
            MainScreen(
                viewModel = viewModel,
                onNavigateToSettings = {},
                onNavigateToFindSa = {},
                onNavigateToTraining = {},
            )
        }

        composeTestRule.waitForIdle()

        // Should display with dot prefix for mandra saptak
        composeTestRule.onNodeWithText(".S").assertIsDisplayed()
    }

    @Test
    fun mainScreen_displaysSwarWithOctaveNotation_madhyaSaptak() {
        val madhyaNote =
            HindustaniNoteConverter.HindustaniNote(
                swar = "R",
                octave = HindustaniNoteConverter.Octave.MADHYA,
                centsDeviation = 0.0,
                isPerfect = true,
                isFlat = false,
                isSharp = false,
            )

        val pitchState =
            PitchState(
                saNote = "C3",
                currentNote = madhyaNote,
                confidence = 0.9f,
                toleranceCents = 15.0,
            )

        val viewModel = createMockViewModel(pitchState)

        composeTestRule.setContent {
            MainScreen(
                viewModel = viewModel,
                onNavigateToSettings = {},
                onNavigateToFindSa = {},
                onNavigateToTraining = {},
            )
        }

        composeTestRule.waitForIdle()

        // Should display without any prefix for madhya saptak
        composeTestRule.onNodeWithText("R").assertIsDisplayed()
    }

    @Test
    fun mainScreen_displaysSwarWithOctaveNotation_taarSaptak() {
        // Create a note in taar saptak (upper octave)
        val taarNote =
            HindustaniNoteConverter.HindustaniNote(
                swar = "G",
                octave = HindustaniNoteConverter.Octave.TAAR,
                centsDeviation = 0.0,
                isPerfect = true,
                isFlat = false,
                isSharp = false,
            )

        val pitchState =
            PitchState(
                saNote = "C3",
                currentNote = taarNote,
                confidence = 0.9f,
                toleranceCents = 15.0,
            )

        val viewModel = createMockViewModel(pitchState)

        composeTestRule.setContent {
            MainScreen(
                viewModel = viewModel,
                onNavigateToSettings = {},
                onNavigateToFindSa = {},
                onNavigateToTraining = {},
            )
        }

        composeTestRule.waitForIdle()

        // Should display with apostrophe suffix for taar saptak
        composeTestRule.onNodeWithText("G'").assertIsDisplayed()
    }

    @Test
    fun mainScreen_displaysNoNote_whenCurrentNoteIsNull() {
        // Set pitch state with no current note
        val pitchState =
            PitchState(
                saNote = "C3",
                currentNote = null,
                confidence = 0.0f,
                toleranceCents = 15.0,
            )

        val viewModel = createMockViewModel(pitchState)

        composeTestRule.setContent {
            MainScreen(
                viewModel = viewModel,
                onNavigateToSettings = {},
                onNavigateToFindSa = {},
                onNavigateToTraining = {},
            )
        }

        composeTestRule.waitForIdle()

        // Should display the "no note" placeholder (em-dash) in NoteDisplay
        composeTestRule.onNodeWithTag("NoteDisplay").assertTextEquals("—")
    }

    // Helper function to create a mocked ViewModel with controllable pitch state
    private fun createMockViewModel(pitchState: PitchState): PitchViewModel {
        val pitchStateFlow = MutableStateFlow(pitchState)
        val settingsFlow = MutableStateFlow(UserSettings())
        val viewModel = mockk<PitchViewModel>(relaxed = true)
        every { viewModel.pitchState } returns pitchStateFlow
        every { viewModel.settings } returns settingsFlow
        every { viewModel.isRecording } returns MutableStateFlow(false)
        every { viewModel.isTanpuraPlaying } returns MutableStateFlow(false)
        return viewModel
    }
}
