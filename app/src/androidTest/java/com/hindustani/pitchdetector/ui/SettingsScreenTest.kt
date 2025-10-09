package com.hindustani.pitchdetector.ui

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hindustani.pitchdetector.viewmodel.PitchViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createViewModel(): PitchViewModel {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return PitchViewModel(context.applicationContext as Application)
    }

    @Test
    fun settingsScreen_displaysAllElements() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, onNavigateBack = {})
        }

        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithText("Default Sa (Tonic)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Select Default Sa").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tolerance: ±15 cents", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tuning System").assertIsDisplayed()
        composeTestRule.onNodeWithText("12 Notes (Just Intonation)").assertIsDisplayed()
        composeTestRule.onNodeWithText("22 Shruti System").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tanpura Volume:", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("About Tolerance").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_toleranceSliderDisplaysCorrectly() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, onNavigateBack = {})
        }

        // Verify tolerance slider is displayed with default value
        composeTestRule.onNodeWithText("Tolerance: ±15 cents", substring = true).assertIsDisplayed()

        // Verify slider labels are displayed
        composeTestRule.onNodeWithText("Expert (±5¢)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Beginner (±30¢)").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_tuningSystemOptionsDisplayed() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, onNavigateBack = {})
        }

        // Verify tuning system section is displayed
        composeTestRule.onNodeWithText("Tuning System").assertIsDisplayed()

        // Verify both options are displayed
        composeTestRule.onNodeWithText("12 Notes (Just Intonation)").assertIsDisplayed()
        composeTestRule.onNodeWithText("22 Shruti System").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_tanpuraVolumeSliderDisplaysCorrectly() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, onNavigateBack = {})
        }

        // Verify tanpura volume slider is displayed with default value (50%)
        composeTestRule.onNodeWithText("Tanpura Volume: 50%").assertIsDisplayed()

        // Verify slider labels are displayed
        composeTestRule.onNodeWithText("Silent (0%)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Full (100%)").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_defaultSaDropdownDisplayed() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, onNavigateBack = {})
        }

        // Verify default Sa section is displayed
        composeTestRule.onNodeWithText("Default Sa (Tonic)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Select Default Sa").assertIsDisplayed()

        // Verify default value is displayed (C3 is the default)
        composeTestRule.onNodeWithText("C3").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_defaultSaDropdownCanBeOpened() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, onNavigateBack = {})
        }

        // Verify dropdown is clickable and doesn't crash when opened
        composeTestRule.onNodeWithText("Select Default Sa").assertHasClickAction()
        composeTestRule.onNodeWithText("Select Default Sa").performClick()

        // Wait for any animations
        composeTestRule.waitForIdle()

        // Verify the field still exists after clicking (basic interaction test)
        composeTestRule.onNodeWithText("Select Default Sa").assertExists()
    }
}
