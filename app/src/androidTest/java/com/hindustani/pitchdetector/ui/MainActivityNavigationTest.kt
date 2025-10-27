package com.hindustani.pitchdetector.ui

import android.Manifest
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for MainActivity navigation
 * Tests navigation flows between different screens including training mode
 */
@RunWith(AndroidJUnit4::class)
class MainActivityNavigationTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO)

    @Test
    fun navigation_mainToTrainingMenu_navigatesSuccessfully() {
        composeTestRule.waitForIdle()

        // Click training button on main screen
        composeTestRule.onNodeWithText("Training").performClick()
        composeTestRule.waitForIdle()

        // Should navigate to training menu
        composeTestRule.onNodeWithText("Training", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Voice Training").assertIsDisplayed()
    }

    @Test
    fun navigation_trainingMenuToLevel1_navigatesWithCorrectArgument() {
        composeTestRule.waitForIdle()

        // Navigate to training menu
        composeTestRule.onNodeWithText("Training").performClick()
        composeTestRule.waitForIdle()

        // Click Level 1 button
        composeTestRule.onNodeWithText("Level 1").performClick()
        composeTestRule.waitForIdle()

        // Should navigate to training screen with level 1
        composeTestRule.onNodeWithText("Training: Level 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Get Ready").assertIsDisplayed()
    }

    @Test
    fun navigation_trainingMenuToLevel2_navigatesWithCorrectArgument() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Training").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Level 2").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Training: Level 2").assertIsDisplayed()
    }

    @Test
    fun navigation_trainingMenuToLevel3_navigatesWithCorrectArgument() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Training").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Level 3").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Training: Level 3").assertIsDisplayed()
    }

    @Test
    fun navigation_trainingMenuToLevel4_navigatesWithCorrectArgument() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Training").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Level 4").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Training: Level 4").assertIsDisplayed()
    }

    @Test
    fun navigation_trainingMenuBackButton_returnsToMainScreen() {
        composeTestRule.waitForIdle()

        // Navigate to training menu
        composeTestRule.onNodeWithText("Training").performClick()
        composeTestRule.waitForIdle()

        // Click back button
        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()
        composeTestRule.waitForIdle()

        // Should be back on main screen
        composeTestRule.onNodeWithText("Listen").assertIsDisplayed()
    }

    @Test
    fun navigation_trainingScreenBackButton_returnsToTrainingMenu() {
        composeTestRule.waitForIdle()

        // Handle custom permission screen if it appears
        // (GrantPermissionRule grants system permission but app may show custom screen)
        try {
            composeTestRule.onNodeWithText("Grant Permission").assertExists()
            composeTestRule.onNodeWithText("Grant Permission").performClick()
            composeTestRule.waitForIdle()
            // Wait for system permission dialog and grant it
            Thread.sleep(1000)
        } catch (e: AssertionError) {
            // Permission already granted or not needed, continue
        }

        // Navigate to training menu
        composeTestRule.onNodeWithText("Training").performClick()
        composeTestRule.waitForIdle()

        // Navigate to Level 1
        composeTestRule.onNodeWithText("Level 1").performClick()
        composeTestRule.waitForIdle()

        // Click back button on training screen
        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()
        composeTestRule.waitForIdle()

        // Should be back on training menu
        composeTestRule.onNodeWithText("Voice Training").assertIsDisplayed()
    }

    @Test
    fun navigation_mainToSettings_navigatesSuccessfully() {
        composeTestRule.waitForIdle()

        // Click settings icon
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.waitForIdle()

        // Should navigate to settings
        composeTestRule.onNodeWithText("Settings", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Tolerance:", substring = true).assertIsDisplayed()
    }
}
