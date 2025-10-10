package com.hindustani.pitchdetector.ui

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hindustani.pitchdetector.viewmodel.PitchViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
    fun mainScreen_displaysAllElements() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            MainScreen(viewModel = viewModel, onNavigateToSettings = {})
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Sa:", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tanpura").assertIsDisplayed()
    }

    @Test
    fun mainScreen_saCanBeChanged() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            MainScreen(viewModel = viewModel, onNavigateToSettings = {})
        }

        composeTestRule.waitForIdle()

        // Get the current Sa to make test independent of default
        val currentSa = runBlocking { viewModel.pitchState.first().saNote }

        // Click on Sa dropdown and select a different note
        composeTestRule.onNodeWithText("Sa: $currentSa", substring = true).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("A3").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Sa: A3", substring = true).assertIsDisplayed()
    }

    @Test
    fun mainScreen_saSelectionIsPersisted() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            MainScreen(viewModel = viewModel, onNavigateToSettings = {})
        }

        composeTestRule.waitForIdle()

        // Get the current Sa
        val currentSa = runBlocking { viewModel.pitchState.first().saNote }

        // Change Sa in main screen to D3
        composeTestRule.onNodeWithText("Sa: $currentSa", substring = true).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("D3").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Sa: D3", substring = true).assertIsDisplayed()

        // Verify that the Sa persisted in settings
        val persistedSa = runBlocking { viewModel.settings.first().saNote }
        assert(persistedSa == "D3") {
            "Sa should be persisted. Expected: D3, Got: $persistedSa"
        }
    }
}
