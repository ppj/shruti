package com.hindustani.pitchdetector.ui

import android.Manifest
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.hindustani.pitchdetector.testutil.TestViewModelFactory
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO)

    @Test
    fun settingsScreen_displaysAllElements() {
        val viewModel = TestViewModelFactory.createPitchViewModel()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, onNavigateBack = {})
        }

        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tolerance: ±15 cents", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tuning System").assertIsDisplayed()
        composeTestRule.onNodeWithText("12 Swars (Just Intonation)").assertIsDisplayed()
        composeTestRule.onNodeWithText("22 Shruti System").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tanpura Volume:", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Swar Notation").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_toleranceSliderDisplaysCorrectly() {
        val viewModel = TestViewModelFactory.createPitchViewModel()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, onNavigateBack = {})
        }

        composeTestRule.onNodeWithText("Tolerance: ±15 cents", substring = true).assertIsDisplayed()

        // Verify slider labels are displayed
        composeTestRule.onNodeWithText("Expert (±5¢)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Beginner (±30¢)").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_tuningSystemOptionsDisplayed() {
        val viewModel = TestViewModelFactory.createPitchViewModel()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, onNavigateBack = {})
        }

        // Verify tuning system section is displayed
        composeTestRule.onNodeWithText("Tuning System").assertIsDisplayed()

        // Verify both options are displayed
        composeTestRule.onNodeWithText("12 Swars (Just Intonation)").assertIsDisplayed()
        composeTestRule.onNodeWithText("22 Shruti System").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_tanpuraVolumeSliderDisplaysCorrectly() {
        val viewModel = TestViewModelFactory.createPitchViewModel()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, onNavigateBack = {})
        }

        // Verify tanpura volume slider is displayed (scroll into view first)
        composeTestRule.onNodeWithText("Tanpura Volume:", substring = true)
            .performScrollTo()
            .assertIsDisplayed()

        // Verify slider labels are displayed (scroll into view)
        composeTestRule.onNodeWithText("Silent (0%)")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Full (100%)")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
