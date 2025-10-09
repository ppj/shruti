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
    fun mainScreen_displaysAllElements() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            MainScreen(viewModel = viewModel, onNavigateToSettings = {})
        }

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

        composeTestRule.onNodeWithText("Sa: C3", substring = true).performClick()
        composeTestRule.onNodeWithText("A3").performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Sa: A3", substring = true).assertIsDisplayed()
    }
}
