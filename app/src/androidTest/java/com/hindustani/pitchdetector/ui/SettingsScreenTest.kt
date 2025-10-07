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
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createViewModel(): PitchViewModel {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return PitchViewModel(context.applicationContext as Application)
    }

    @Test
    fun settingsScreen_displaysTitle() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_hasBackButton() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_backButtonIsClickable() {
        val viewModel = createViewModel()
        var backClicked = false

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { backClicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assert(backClicked)
    }

    @Test
    fun settingsScreen_displaysSaInputField() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {}
            )
        }

        // Check for Sa input field
        composeTestRule.onNodeWithText("Sa (e.g., C4, A#3, Bb4)", substring = true).assertExists()
    }

    @Test
    fun settingsScreen_saInputFieldIsEditable() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {}
            )
        }

        // Find the text field and enter text
        composeTestRule.onNodeWithText("C4").performTextClearance()
        composeTestRule.onNodeWithText("Sa (e.g., C4, A#3, Bb4)", substring = true)
            .performTextInput("D4")

        // Wait for update
        composeTestRule.waitForIdle()

        // Verify ViewModel was updated
        assert(viewModel.settings.value.saNote == "D4")
    }

    @Test
    fun settingsScreen_displaysToleranceSlider() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {}
            )
        }

        // Check for tolerance label
        composeTestRule.onNodeWithText("Tolerance: ±15 cents", substring = true).assertExists()
    }

    @Test
    fun settingsScreen_displaysToleranceLabels() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {}
            )
        }

        // Check for expert and beginner labels
        composeTestRule.onNodeWithText("Expert (±5¢)", substring = true).assertExists()
        composeTestRule.onNodeWithText("Beginner (±30¢)", substring = true).assertExists()
    }

    @Test
    fun settingsScreen_displaysTuningSystemOptions() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {}
            )
        }

        // Check for tuning system options
        composeTestRule.onNodeWithText("Tuning System").assertExists()
        composeTestRule.onNodeWithText("12 Notes (Just Intonation)").assertExists()
        composeTestRule.onNodeWithText("22 Shruti System").assertExists()
    }

    @Test
    fun settingsScreen_radioButtonsAreClickable() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {}
            )
        }

        // Initially 12-note system should be selected
        assert(!viewModel.settings.value.use22Shruti)

        // Click 22-shruti option
        composeTestRule.onNodeWithText("22 Shruti System").performClick()

        // Wait for update
        composeTestRule.waitForIdle()

        // Verify ViewModel was updated
        assert(viewModel.settings.value.use22Shruti)
    }

    @Test
    fun settingsScreen_displaysToleranceInfoCard() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {}
            )
        }

        // Check for info card content
        composeTestRule.onNodeWithText("About Tolerance").assertExists()
        composeTestRule.onNodeWithText("Expert (±5-8¢): For advanced musicians", substring = true)
            .assertExists()
    }

    @Test
    fun settingsScreen_hasAllExpectedSections() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {}
            )
        }

        // Verify all sections are present
        composeTestRule.onNodeWithText("Tonic (Sa)").assertExists()
        composeTestRule.onNodeWithText("Tolerance:", substring = true).assertExists()
        composeTestRule.onNodeWithText("Tuning System").assertExists()
        composeTestRule.onNodeWithText("About Tolerance").assertExists()
    }

    @Test
    fun settingsScreen_saInputAcceptsValidNotation() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {}
            )
        }

        val validNotes = listOf("C4", "C#4", "Db4", "A3", "G#5")

        validNotes.forEach { note ->
            composeTestRule.onNodeWithText(viewModel.settings.value.saNote).performTextClearance()
            composeTestRule.onNodeWithText("Sa (e.g., C4, A#3, Bb4)", substring = true)
                .performTextInput(note)

            composeTestRule.waitForIdle()

            assert(viewModel.settings.value.saNote == note)
        }
    }

    @Test
    fun settingsScreen_switchingTuningSystemsUpdatesViewModel() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {}
            )
        }

        // Start with 12-note
        assert(!viewModel.settings.value.use22Shruti)

        // Switch to 22-shruti
        composeTestRule.onNodeWithText("22 Shruti System").performClick()
        composeTestRule.waitForIdle()
        assert(viewModel.settings.value.use22Shruti)

        // Switch back to 12-note
        composeTestRule.onNodeWithText("12 Notes (Just Intonation)").performClick()
        composeTestRule.waitForIdle()
        assert(!viewModel.settings.value.use22Shruti)
    }

    @Test
    fun settingsScreen_preservesOtherSettingsWhenChangingSa() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {}
            )
        }

        // Set initial tolerance
        val initialTolerance = viewModel.settings.value.toleranceCents

        // Change Sa
        composeTestRule.onNodeWithText("C4").performTextClearance()
        composeTestRule.onNodeWithText("Sa (e.g., C4, A#3, Bb4)", substring = true)
            .performTextInput("G4")

        composeTestRule.waitForIdle()

        // Tolerance should remain unchanged
        assert(viewModel.settings.value.toleranceCents == initialTolerance)
    }

    @Test
    fun settingsScreen_preservesOtherSettingsWhenChangingTuningSystem() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {}
            )
        }

        // Set initial values
        val initialSa = viewModel.settings.value.saNote
        val initialTolerance = viewModel.settings.value.toleranceCents

        // Change tuning system
        composeTestRule.onNodeWithText("22 Shruti System").performClick()
        composeTestRule.waitForIdle()

        // Other settings should remain unchanged
        assert(viewModel.settings.value.saNote == initialSa)
        assert(viewModel.settings.value.toleranceCents == initialTolerance)
    }
}
