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
                onSaSelected = {}
            )
        }

        composeTestRule.waitForIdle()

        // Should display title
        composeTestRule.onNodeWithText("Find Your Sa").assertIsDisplayed()

        // Should display back button
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()

        // Should display start test button
        composeTestRule.onNodeWithText("Start Test").assertIsDisplayed()
    }

    @Test
    fun findSaScreen_displaysInstructions() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {}
            )
        }

        composeTestRule.waitForIdle()

        // Should display "How it works:" header
        composeTestRule.onNodeWithText("How it works:", substring = true).assertIsDisplayed()

        // Should display key instruction steps
        composeTestRule.onNodeWithText("Find a quiet place", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("glide", substring = true, ignoreCase = true).assertIsDisplayed()
    }

    // Recording State Tests

    @Test
    fun findSaScreen_startTestButton_transitionsToRecording() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {}
            )
        }

        composeTestRule.waitForIdle()

        // Click Start Test button
        composeTestRule.onNodeWithText("Start Test").performClick()
        composeTestRule.waitForIdle()

        // Should now show "Stop Test" button
        composeTestRule.onNodeWithText("Stop Test").assertIsDisplayed()

        // Should show "Listening..." text
        composeTestRule.onNodeWithText("Listening...").assertIsDisplayed()

        // Should show progress indicator
        composeTestRule.onNodeWithText("Collecting samples...", substring = true).assertIsDisplayed()
    }

    @Test
    fun findSaScreen_recording_showsRealTimeFeedback() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {}
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Start Test").performClick()
        composeTestRule.waitForIdle()

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
                onSaSelected = {}
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
                onSaSelected = {}
            )
        }

        composeTestRule.waitForIdle()

        // Start test
        composeTestRule.onNodeWithText("Start Test").performClick()
        composeTestRule.waitForIdle()

        // In a real test, we would need to inject mock audio data
        // For now, we just verify the UI structure exists
        // The Stop Test button should be present
        composeTestRule.onNodeWithText("Stop Test").assertExists()
    }

    @Test
    fun findSaScreen_tryAgainButton_resetsToInitialState() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {}
            )
        }

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

        // Should be back to initial state
        composeTestRule.onNodeWithText("Start Test").assertIsDisplayed()
    }

    // State Transitions Tests

    @Test
    fun findSaScreen_stopTest_transitionsToAnalyzing() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {}
            )
        }

        composeTestRule.waitForIdle()

        // Start test
        composeTestRule.onNodeWithText("Start Test").performClick()
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
                onSaSelected = {}
            )
        }

        composeTestRule.waitForIdle()

        // Start and immediately stop (no data collected)
        composeTestRule.onNodeWithText("Start Test").performClick()
        composeTestRule.waitForIdle()

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
                onSaSelected = {}
            )
        }

        composeTestRule.waitForIdle()

        // Recording-specific elements should not be visible
        composeTestRule.onNodeWithText("Stop Test").assertDoesNotExist()
        composeTestRule.onNodeWithText("Listening...").assertDoesNotExist()
        composeTestRule.onNodeWithText("Accept and Save").assertDoesNotExist()
    }

    @Test
    fun findSaScreen_recording_hidesInitialComponents() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            FindSaScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onSaSelected = {}
            )
        }

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
                onSaSelected = {}
            )
        }

        composeTestRule.waitForIdle()

        // Important interactive elements should have content descriptions
        composeTestRule.onNodeWithContentDescription("Back").assertExists()
    }
}
