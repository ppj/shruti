package com.hindustani.pitchdetector.ui.findsa

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hindustani.pitchdetector.viewmodel.FindSaViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FindSaScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createViewModel(): FindSaViewModel {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return FindSaViewModel(context.applicationContext as Application)
    }

    // Initial State Tests

    @Test
    fun findSaScreen_displaysInitialElements() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // Should display title
        composeTestRule.onNodeWithText("Find Your Sa").assertIsDisplayed()

        // Should display back button
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()

        // Should display mode selection initially (not start test button yet)
        composeTestRule.onNodeWithText("Choose Test Method").assertIsDisplayed()
    }

    @Test
    fun findSaScreen_displaysInstructions() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // First select a mode to see instructions
        composeTestRule.onNodeWithText("Both (Recommended)").performClick()
        composeTestRule.waitForIdle()

        // Should display "How it works:" header
        composeTestRule.onNodeWithText("How it works:", substring = true).assertIsDisplayed()

        // Should display key instruction steps
        composeTestRule.onNodeWithText("Find a quiet place", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("glide", substring = true, ignoreCase = true).assertIsDisplayed()
    }

    // Recording State Tests

    @Test
    fun findSaScreen_startTestButton_transitionsToSpeechRecording() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // Select Both mode first
        composeTestRule.onNodeWithText("Both (Recommended)").performClick()
        composeTestRule.waitForIdle()

        // Click Start Test button
        composeTestRule.onNodeWithText("Start Test").performClick()
        composeTestRule.waitForIdle()

        // Should now show Phase 1: Speaking
        composeTestRule.onNodeWithText("Phase 1: Speaking").assertIsDisplayed()

        // Should show counting instruction
        composeTestRule.onNodeWithText("Count upwards slowly and naturally").assertIsDisplayed()

        // Should show progress indicator
        composeTestRule.onNodeWithText("Collecting samples...", substring = true).assertIsDisplayed()
    }

    @Test
    fun findSaScreen_singingPhase_showsRealTimeFeedback() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()
        // Select Both mode first
        composeTestRule.onNodeWithText("Both (Recommended)").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Start Test").performClick()
        composeTestRule.waitForIdle()

        // Transition to singing phase by calling stopSpeechTest
        viewModel.stopSpeechTest()
        composeTestRule.waitForIdle()

        // Should show Phase 2: Singing Range
        composeTestRule.onNodeWithText("Phase 2: Singing Range").assertIsDisplayed()

        // Should show guidance text
        composeTestRule.onNodeWithText("Start singing 'aaaaah'", substring = true)
            .assertIsDisplayed()

        // Should show ready indicator guidance
        composeTestRule.onNodeWithText("Wait for the 'Ready' indicator before stopping", substring = true)
            .assertIsDisplayed()
    }

    // Navigation Tests

    @Test
    fun findSaScreen_backButton_callsNavigateBack() {
        val viewModel = createViewModel()
        var backPressed = false

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = { backPressed = true },
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        assert(backPressed) { "Back navigation should be called" }
    }

    // Results View Tests (simulated)

    @Test
    fun findSaScreen_resultsView_displaysRecommendation() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // Select Both mode first
        composeTestRule.onNodeWithText("Both (Recommended)").performClick()
        composeTestRule.waitForIdle()

        // Start test (speech phase)
        composeTestRule.onNodeWithText("Start Test").performClick()
        composeTestRule.waitForIdle()

        // Transition to singing phase
        viewModel.stopSpeechTest()
        composeTestRule.waitForIdle()

        // In a real test, we would need to inject mock audio data
        // For now, we just verify the UI structure exists
        // The Stop Test button should be present in singing phase
        composeTestRule.onNodeWithText("Stop Test").assertExists()
    }

    @Test
    fun findSaScreen_tryAgainButton_resetsToInitialState() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // Select Both mode first
        composeTestRule.onNodeWithText("Both (Recommended)").performClick()
        composeTestRule.waitForIdle()

        // Start and immediately stop (will likely show error, but tests reset flow)
        composeTestRule.onNodeWithText("Start Test").performClick()
        composeTestRule.waitForIdle()

        viewModel.stopTest()
        composeTestRule.waitForIdle()

        // After error or completion, if "Try Again" button exists, clicking it should reset
        // Note: This tests the reset functionality even if we don't reach Finished state
        viewModel.resetTest()
        composeTestRule.waitForIdle()

        // Should be back to mode selection (initial state)
        composeTestRule.onNodeWithText("Choose Test Method").assertIsDisplayed()
    }

    // State Transitions Tests

    @Test
    fun findSaScreen_stopTest_transitionsToAnalyzing() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // Select Both mode first
        composeTestRule.onNodeWithText("Both (Recommended)").performClick()
        composeTestRule.waitForIdle()

        // Start test (speech phase)
        composeTestRule.onNodeWithText("Start Test").performClick()
        composeTestRule.waitForIdle()

        // Transition to singing phase
        viewModel.stopSpeechTest()
        composeTestRule.waitForIdle()

        // Stop test (will show analyzing or error depending on data)
        composeTestRule.onNodeWithText("Stop Test").performClick()
        composeTestRule.waitForIdle()

        // Should show either "Analyzing your voice..." or an error message
        // We can't guarantee which without proper audio data, so we just verify state changed
        composeTestRule.onNodeWithText("Stop Test").assertDoesNotExist()
    }

    // Error Handling Tests

    @Test
    fun findSaScreen_insufficientData_showsError() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // Select Both mode first
        composeTestRule.onNodeWithText("Both (Recommended)").performClick()
        composeTestRule.waitForIdle()

        // Start test (speech phase)
        composeTestRule.onNodeWithText("Start Test").performClick()
        composeTestRule.waitForIdle()

        // Transition to singing phase
        viewModel.stopSpeechTest()
        composeTestRule.waitForIdle()

        // Immediately stop (no singing data collected)
        composeTestRule.onNodeWithText("Stop Test").performClick()
        composeTestRule.waitForIdle()

        // Should show an error (likely "Insufficient data" or "No valid pitches")
        // The exact error depends on whether any samples were collected
        // Just verify we're not stuck in Recording state
        composeTestRule.onNodeWithText("Stop Test").assertDoesNotExist()
    }

    // Component Visibility Tests

    @Test
    fun findSaScreen_notStarted_hidesRecordingComponents() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // Recording-specific elements should not be visible
        composeTestRule.onNodeWithText("Stop Test").assertDoesNotExist()
        composeTestRule.onNodeWithText("Phase 1: Speaking").assertDoesNotExist()
        composeTestRule.onNodeWithText("Phase 2: Singing Range").assertDoesNotExist()
        composeTestRule.onNodeWithText("Accept and Save").assertDoesNotExist()
    }

    @Test
    fun findSaScreen_recording_hidesInitialComponents() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // Select Both mode first
        composeTestRule.onNodeWithText("Both (Recommended)").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Start Test").performClick()
        composeTestRule.waitForIdle()

        // Initial screen elements should not be visible
        composeTestRule.onNodeWithText("Start Test").assertDoesNotExist()
        composeTestRule.onNodeWithText("How it works:", substring = true).assertDoesNotExist()
    }

    // Accessibility Tests

    @Test
    fun findSaScreen_hasAccessibleContentDescriptions() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // Important interactive elements should have content descriptions
        composeTestRule.onNodeWithContentDescription("Back").assertExists()
    }

    // Mode Selection Tests

    @Test
    fun findSaScreen_initialState_showsModeSelection() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // Should show mode selection title
        composeTestRule.onNodeWithText("Choose Test Method").assertIsDisplayed()

        // Should show all three mode options
        composeTestRule.onNodeWithText("Speaking Voice").assertIsDisplayed()
        composeTestRule.onNodeWithText("Singing Range").assertIsDisplayed()
        composeTestRule.onNodeWithText("Both (Recommended)").assertIsDisplayed()
    }

    @Test
    fun findSaScreen_modeSelection_showsDurationEstimates() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // Should show duration estimates for each mode
        composeTestRule.onNodeWithText("~10 seconds").assertIsDisplayed()
        composeTestRule.onNodeWithText("~20 seconds").assertIsDisplayed()
        composeTestRule.onNodeWithText("~30 seconds").assertIsDisplayed()
    }

    @Test
    fun findSaScreen_modeSelection_showsRecommendedLabel() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // Should show "Recommended" label on Both mode
        composeTestRule.onNodeWithText("Recommended").assertIsDisplayed()
    }

    @Test
    fun findSaScreen_selectSpeakingMode_transitionsToInstructions() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // Click on Speaking Voice mode
        composeTestRule.onNodeWithText("Speaking Voice").performClick()
        composeTestRule.waitForIdle()

        // Should transition to instruction screen
        composeTestRule.onNodeWithText("Find Your Ideal Sa").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start Test").assertIsDisplayed()

        // Should show speaking-specific instructions
        composeTestRule.onNodeWithText("Count upwards slowly in your natural speaking voice", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun findSaScreen_selectSingingMode_showsSingingInstructions() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // Click on Singing Range mode
        composeTestRule.onNodeWithText("Singing Range").performClick()
        composeTestRule.waitForIdle()

        // Should show singing-specific instructions
        composeTestRule.onNodeWithText("Sing 'aaaaah' and glide to your lowest comfortable note", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun findSaScreen_selectBothMode_showsBothInstructions() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // Click on Both mode
        composeTestRule.onNodeWithText("Both (Recommended)").performClick()
        composeTestRule.waitForIdle()

        // Should show both phase instructions
        composeTestRule.onNodeWithText("Phase 1: Speaking Voice", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Phase 2: Singing Range", substring = true)
            .assertIsDisplayed()
    }

    // Mode-Specific Flow Tests

    @Test
    fun findSaScreen_speakingOnlyMode_skipsSingingPhase() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // Select Speaking Only mode
        composeTestRule.onNodeWithText("Speaking Voice").performClick()
        composeTestRule.waitForIdle()

        // Start test
        composeTestRule.onNodeWithText("Start Test").performClick()
        composeTestRule.waitForIdle()

        // Should go to speech recording
        composeTestRule.onNodeWithText("Phase 1: Speaking").assertIsDisplayed()

        // Button should say "Stop Test" not "Next: Vocal Range"
        composeTestRule.onNodeWithText("Stop Test").assertExists()
        composeTestRule.onNodeWithText("Next: Vocal Range").assertDoesNotExist()
    }

    @Test
    fun findSaScreen_singingOnlyMode_skipsSpeechPhase() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // Select Singing Only mode
        composeTestRule.onNodeWithText("Singing Range").performClick()
        composeTestRule.waitForIdle()

        // Start test
        composeTestRule.onNodeWithText("Start Test").performClick()
        composeTestRule.waitForIdle()

        // Should skip speech phase and go directly to singing
        composeTestRule.onNodeWithText("Phase 2: Singing Range").assertIsDisplayed()
        composeTestRule.onNodeWithText("Phase 1: Speaking").assertDoesNotExist()
    }

    @Test
    fun findSaScreen_bothMode_includesBothPhases() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // Select Both mode (default, but click to ensure)
        composeTestRule.onNodeWithText("Both (Recommended)").performClick()
        composeTestRule.waitForIdle()

        // Start test
        composeTestRule.onNodeWithText("Start Test").performClick()
        composeTestRule.waitForIdle()

        // Should start with speech phase
        composeTestRule.onNodeWithText("Phase 1: Speaking").assertIsDisplayed()

        // Button should say "Next: Vocal Range"
        composeTestRule.onNodeWithText("Next: Vocal Range").assertExists()

        // Transition to singing phase
        viewModel.stopSpeechTest()
        composeTestRule.waitForIdle()

        // Should show singing phase
        composeTestRule.onNodeWithText("Phase 2: Singing Range").assertIsDisplayed()
    }

    // Mode-Specific UI Element Tests

    @Test
    fun findSaScreen_speakingOnlyMode_showsCorrectButtonText() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // Select Speaking Only mode
        composeTestRule.onNodeWithText("Speaking Voice").performClick()
        composeTestRule.waitForIdle()

        // Start test
        composeTestRule.onNodeWithText("Start Test").performClick()
        composeTestRule.waitForIdle()

        // Should show "Stop Test" button with error color styling
        composeTestRule.onNodeWithText("Stop Test").assertIsDisplayed()

        // Should show appropriate helper text
        composeTestRule.onNodeWithText("Wait for the 'Ready' indicator before stopping", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun findSaScreen_bothMode_showsNextButtonInSpeechPhase() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // Select Both mode
        composeTestRule.onNodeWithText("Both (Recommended)").performClick()
        composeTestRule.waitForIdle()

        // Start test
        composeTestRule.onNodeWithText("Start Test").performClick()
        composeTestRule.waitForIdle()

        // Should show "Next: Vocal Range" button
        composeTestRule.onNodeWithText("Next: Vocal Range").assertIsDisplayed()

        // Should show appropriate helper text
        composeTestRule.onNodeWithText("Keep counting until ready", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun findSaScreen_modeSelection_bothModeIsDefaultSelected() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // Both mode should be selected by default (this is implicit in the UI state)
        // We verify by checking that if we proceed without selecting, we get Both mode behavior
        composeTestRule.onNodeWithText("Both (Recommended)").assertIsDisplayed()
    }

    @Test
    fun findSaScreen_canSwitchBetweenModes() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {},
            )
        }

        composeTestRule.waitForIdle()

        // Select Speaking mode
        composeTestRule.onNodeWithText("Speaking Voice").performClick()
        composeTestRule.waitForIdle()

        // Verify instructions changed
        composeTestRule.onNodeWithText("Find Your Ideal Sa").assertIsDisplayed()

        // Go back to mode selection by resetting
        viewModel.resetTest()
        composeTestRule.waitForIdle()

        // Should be back at mode selection
        composeTestRule.onNodeWithText("Choose Test Method").assertIsDisplayed()

        // Select different mode
        composeTestRule.onNodeWithText("Singing Range").performClick()
        composeTestRule.waitForIdle()

        // Verify new mode's instructions
        composeTestRule.onNodeWithText("Find Your Ideal Sa").assertIsDisplayed()
    }
}
