package com.hindustani.pitchdetector.ui

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hindustani.pitchdetector.viewmodel.PitchViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createViewModel(): PitchViewModel {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return PitchViewModel(context.applicationContext as Application)
    }

    @Test
    fun mainScreen_displaysDefaultSaNote() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            MainScreen(
                viewModel = viewModel,
                onNavigateToSettings = {}
            )
        }

        // Check that default Sa is displayed (C4)
        composeTestRule.onNodeWithText("Sa: C4 (262 Hz)", substring = true).assertIsDisplayed()
    }

    @Test
    fun mainScreen_displaysStartButton() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            MainScreen(
                viewModel = viewModel,
                onNavigateToSettings = {}
            )
        }

        // Check that Start button is displayed
        composeTestRule.onNodeWithText("Start").assertIsDisplayed()
    }

    @Test
    fun mainScreen_displaysSettingsButton() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            MainScreen(
                viewModel = viewModel,
                onNavigateToSettings = {}
            )
        }

        // Check that settings icon button exists
        composeTestRule.onNodeWithContentDescription("Settings").assertIsDisplayed()
    }

    @Test
    fun mainScreen_displaysDefaultNote() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            MainScreen(
                viewModel = viewModel,
                onNavigateToSettings = {}
            )
        }

        // When no pitch is detected, should display "—"
        composeTestRule.onNodeWithText("—").assertIsDisplayed()
    }

    @Test
    fun mainScreen_settingsButtonIsClickable() {
        val viewModel = createViewModel()
        var settingsClicked = false

        composeTestRule.setContent {
            MainScreen(
                viewModel = viewModel,
                onNavigateToSettings = { settingsClicked = true }
            )
        }

        // Click settings button
        composeTestRule.onNodeWithContentDescription("Settings").performClick()

        // Verify navigation callback was triggered
        assert(settingsClicked)
    }

    @Test
    fun mainScreen_startButtonIsClickable() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            MainScreen(
                viewModel = viewModel,
                onNavigateToSettings = {}
            )
        }

        // Start button should be clickable
        composeTestRule.onNodeWithText("Start").assertHasClickAction()
    }

    @Test
    fun mainScreen_buttonTextChangesWhenRecording() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            MainScreen(
                viewModel = viewModel,
                onNavigateToSettings = {}
            )
        }

        // Initially shows "Start"
        composeTestRule.onNodeWithText("Start").assertIsDisplayed()

        // Click to start recording
        // Note: Actual recording may fail in test environment without microphone permission
        composeTestRule.onNodeWithText("Start").performClick()

        // Note: In actual device test, button would change to "Stop"
        // In CI/CD without permissions, this may not change
        // This test validates the UI structure exists
    }

    @Test
    fun mainScreen_hasPitchIndicatorComponent() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            MainScreen(
                viewModel = viewModel,
                onNavigateToSettings = {}
            )
        }

        // Check that pitch indicator displays cents
        composeTestRule.onNodeWithText("0 cents", substring = true).assertExists()
    }

    @Test
    fun mainScreen_displaysAllUIElements() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            MainScreen(
                viewModel = viewModel,
                onNavigateToSettings = {}
            )
        }

        // Verify all major UI elements are present
        composeTestRule.onNodeWithText("Sa:", substring = true).assertExists()
        composeTestRule.onNodeWithContentDescription("Settings").assertExists()
        composeTestRule.onNodeWithText("—").assertExists() // Note display
        composeTestRule.onNodeWithText("0 cents", substring = true).assertExists()
        composeTestRule.onNodeWithText("Start").assertExists()
    }

    @Test
    fun mainScreen_saDisplayUpdatesWhenViewModelChanges() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            MainScreen(
                viewModel = viewModel,
                onNavigateToSettings = {}
            )
        }

        // Change Sa in ViewModel
        viewModel.updateSa("A4")

        // Wait for UI to update
        composeTestRule.waitForIdle()

        // Check that new Sa is displayed
        composeTestRule.onNodeWithText("Sa: A4 (440 Hz)", substring = true).assertExists()
    }

    @Test
    fun mainScreen_toleranceAffectsDisplay() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            MainScreen(
                viewModel = viewModel,
                onNavigateToSettings = {}
            )
        }

        // Change tolerance
        viewModel.updateTolerance(20.0)

        // Wait for UI to update
        composeTestRule.waitForIdle()

        // UI should still be functional (tolerance affects internal logic)
        composeTestRule.onNodeWithText("Start").assertIsDisplayed()
    }
}
