package com.hindustani.pitchdetector.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hindustani.pitchdetector.data.PitchState
import com.hindustani.pitchdetector.music.HindustaniNoteConverter
import com.hindustani.pitchdetector.testutil.TestViewModelFactory
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

    @Test
    fun mainScreen_displaysAllElements() {
        val viewModel = TestViewModelFactory.createPitchViewModel()
        composeTestRule.setContent {
            MainScreen(viewModel = viewModel, onNavigateToSettings = {}, onNavigateToFindSa = {})
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
            MainScreen(viewModel = viewModel, onNavigateToSettings = {}, onNavigateToFindSa = {})
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
            MainScreen(viewModel = viewModel, onNavigateToSettings = {}, onNavigateToFindSa = {})
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
    fun mainScreen_displaysSwaraWithOctaveNotation_mandraSaptak() {
        val viewModel = TestViewModelFactory.createPitchViewModel()

        // Create a note in mandra saptak (lower octave)
        val mandraNote =
            HindustaniNoteConverter.HindustaniNote(
                swara = "S",
                octave = HindustaniNoteConverter.Octave.MANDRA,
                centsDeviation = 0.0,
                isPerfect = true,
                isFlat = false,
                isSharp = false,
            )

        // Update the pitch state to include this note
        val pitchState =
            PitchState(
                saNote = "C3",
                currentNote = mandraNote,
                confidence = 0.9,
                toleranceCents = 15.0,
            )
        (viewModel.pitchState as MutableStateFlow).value = pitchState

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
    fun mainScreen_displaysSwaraWithOctaveNotation_madhyaSaptak() {
        val viewModel = TestViewModelFactory.createPitchViewModel()

        // Create a note in madhya saptak (middle octave)
        val madhyaNote =
            HindustaniNoteConverter.HindustaniNote(
                swara = "R",
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
                confidence = 0.9,
                toleranceCents = 15.0,
            )
        (viewModel.pitchState as MutableStateFlow).value = pitchState

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
    fun mainScreen_displaysSwaraWithOctaveNotation_taarSaptak() {
        val viewModel = TestViewModelFactory.createPitchViewModel()

        // Create a note in taar saptak (upper octave)
        val taarNote =
            HindustaniNoteConverter.HindustaniNote(
                swara = "G",
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
                confidence = 0.9,
                toleranceCents = 15.0,
            )
        (viewModel.pitchState as MutableStateFlow).value = pitchState

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
        val viewModel = TestViewModelFactory.createPitchViewModel()

        // Set pitch state with no current note
        val pitchState =
            PitchState(
                saNote = "C3",
                currentNote = null,
                confidence = 0.0,
                toleranceCents = 15.0,
            )
        (viewModel.pitchState as MutableStateFlow).value = pitchState

        composeTestRule.setContent {
            MainScreen(
                viewModel = viewModel,
                onNavigateToSettings = {},
                onNavigateToFindSa = {},
                onNavigateToTraining = {},
            )
        }

        composeTestRule.waitForIdle()

        // Should display the "no note" placeholder (em-dash)
        composeTestRule.onNodeWithText("—").assertIsDisplayed()
    }
}
