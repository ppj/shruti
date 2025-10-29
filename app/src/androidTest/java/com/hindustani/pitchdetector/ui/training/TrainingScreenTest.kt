package com.hindustani.pitchdetector.ui.training

import android.Manifest
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.hindustani.pitchdetector.data.TrainingState
import com.hindustani.pitchdetector.viewmodel.TrainingViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for TrainingScreen
 * Tests that UI correctly reflects training state and handles user interactions
 */
@RunWith(AndroidJUnit4::class)
class TrainingScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO)

    @Test
    fun trainingScreen_countdownDisplayed_whenCountdownGreaterThanZero() {
        val viewModel =
            createMockViewModel(
                state = TrainingState(level = 1, countdown = 3),
            )

        composeTestRule.setContent {
            TrainingScreen(
                navController = rememberNavController(),
                viewModel = viewModel,
            )
        }

        composeTestRule.onNodeWithText("Get Ready").assertIsDisplayed()
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun trainingScreen_trainingViewDisplayed_whenCountdownIsZero() {
        val viewModel =
            createMockViewModel(
                state =
                    TrainingState(
                        level = 1,
                        countdown = 0,
                        currentSwar = "S",
                        holdProgress = 0.5f,
                        isHoldingCorrectly = false,
                    ),
            )

        composeTestRule.setContent {
            TrainingScreen(
                navController = rememberNavController(),
                viewModel = viewModel,
            )
        }

        composeTestRule.waitForIdle()

        // Target swar should be displayed
        composeTestRule.onNodeWithText("S").assertIsDisplayed()

        // Instruction text should be displayed
        composeTestRule.onNodeWithText("Sing the note to start timer").assertIsDisplayed()
    }

    @Test
    fun trainingScreen_correctFeedback_whenHoldingCorrectly() {
        val viewModel =
            createMockViewModel(
                state =
                    TrainingState(
                        level = 1,
                        countdown = 0,
                        currentSwar = "R",
                        holdProgress = 0.7f,
                        isHoldingCorrectly = true,
                    ),
            )

        composeTestRule.setContent {
            TrainingScreen(
                navController = rememberNavController(),
                viewModel = viewModel,
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Great! Keep holding...").assertIsDisplayed()
    }

    @Test
    fun trainingScreen_flatFeedback_whenSingingCorrectSwarButFlat() {
        val viewModel =
            createMockViewModel(
                state =
                    TrainingState(
                        level = 1,
                        countdown = 0,
                        currentSwar = "G",
                        detectedSwar = "G",
                        isFlat = true,
                        isSharp = false,
                        isHoldingCorrectly = false,
                    ),
            )

        composeTestRule.setContent {
            TrainingScreen(
                navController = rememberNavController(),
                viewModel = viewModel,
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("⬆️ Sharpen").assertIsDisplayed()
    }

    @Test
    fun trainingScreen_sharpFeedback_whenSingingCorrectSwarButSharp() {
        val viewModel =
            createMockViewModel(
                state =
                    TrainingState(
                        level = 1,
                        countdown = 0,
                        currentSwar = "P",
                        detectedSwar = "P",
                        isFlat = false,
                        isSharp = true,
                        isHoldingCorrectly = false,
                    ),
            )

        composeTestRule.setContent {
            TrainingScreen(
                navController = rememberNavController(),
                viewModel = viewModel,
            )
        }

        composeTestRule.waitForIdle()

        // Should show "⬇️ Flatten"
        composeTestRule.onNodeWithText("⬇️ Flatten").assertIsDisplayed()
    }

    @Test
    fun trainingScreen_noFlatSharpFeedback_whenSingingWrongSwar() {
        val viewModel =
            createMockViewModel(
                state =
                    TrainingState(
                        level = 1,
                        countdown = 0,
                        currentSwar = "S",
                        detectedSwar = "R",
                        isFlat = false,
                        isSharp = false,
                        isHoldingCorrectly = false,
                    ),
            )

        composeTestRule.setContent {
            TrainingScreen(
                navController = rememberNavController(),
                viewModel = viewModel,
            )
        }

        composeTestRule.waitForIdle()

        // Should show default instruction, not flat/sharp feedback
        composeTestRule.onNodeWithText("Sing the note to start timer").assertIsDisplayed()
        composeTestRule.onNodeWithText("⬆️ Sharpen").assertDoesNotExist()
        composeTestRule.onNodeWithText("⬇️ Flatten").assertDoesNotExist()
    }

    @Test
    fun trainingScreen_completionDialog_whenSessionComplete() {
        val viewModel =
            createMockViewModel(
                state =
                    TrainingState(
                        level = 1,
                        countdown = 0,
                        currentSwar = "N",
                        isSessionComplete = true,
                    ),
            )

        composeTestRule.setContent {
            TrainingScreen(
                navController = rememberNavController(),
                viewModel = viewModel,
            )
        }

        composeTestRule.waitForIdle()

        // Completion dialog should be visible
        composeTestRule.onNodeWithText("Congratulations!").assertIsDisplayed()
        composeTestRule.onNodeWithText("You have completed Level 1 successfully!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Back to Main").assertIsDisplayed()
        composeTestRule.onNodeWithText("Repeat Level").assertIsDisplayed()
    }

    @Test
    fun trainingScreen_repeatLevelButton_callsResetSession() {
        val viewModel =
            createMockViewModel(
                state =
                    TrainingState(
                        level = 2,
                        countdown = 0,
                        isSessionComplete = true,
                    ),
            )

        composeTestRule.setContent {
            TrainingScreen(
                navController = rememberNavController(),
                viewModel = viewModel,
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Repeat Level").performClick()
        composeTestRule.waitForIdle()

        verify { viewModel.resetSession() }
    }

    @Test
    fun trainingScreen_tanpuraToggle_callsToggleTanpura() {
        val viewModel =
            createMockViewModel(
                state = TrainingState(level = 1, countdown = 0),
                isTanpuraPlaying = false,
            )

        composeTestRule.setContent {
            TrainingScreen(
                navController = rememberNavController(),
                viewModel = viewModel,
            )
        }

        composeTestRule.waitForIdle()

        // Find and click the tanpura switch
        composeTestRule.onNodeWithTag("TanpuraToggle").performClick()
        composeTestRule.waitForIdle()

        verify { viewModel.toggleTanpura() }
    }

    @Test
    fun trainingScreen_displaysSaNote() {
        val viewModel =
            createMockViewModel(
                state = TrainingState(level = 1),
                saNote = "D3",
            )

        composeTestRule.setContent {
            TrainingScreen(
                navController = rememberNavController(),
                viewModel = viewModel,
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Sa: D3", substring = true).assertIsDisplayed()
    }

    @Test
    fun trainingScreen_displaysLevelNumber() {
        val viewModel =
            createMockViewModel(
                state = TrainingState(level = 3),
            )

        composeTestRule.setContent {
            TrainingScreen(
                navController = rememberNavController(),
                viewModel = viewModel,
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Training: Level 3").assertIsDisplayed()
    }

    @Test
    fun trainingScreen_displaysScore_duringTraining() {
        val viewModel =
            createMockViewModel(
                state =
                    TrainingState(
                        level = 1,
                        countdown = 0,
                        currentScore = 350,
                    ),
            )

        composeTestRule.setContent {
            TrainingScreen(
                navController = rememberNavController(),
                viewModel = viewModel,
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Score: 350").assertIsDisplayed()
    }

    @Test
    fun trainingScreen_displaysCombo_whenComboIsTwo() {
        val viewModel =
            createMockViewModel(
                state =
                    TrainingState(
                        level = 1,
                        countdown = 0,
                        comboCount = 2,
                    ),
            )

        composeTestRule.setContent {
            TrainingScreen(
                navController = rememberNavController(),
                viewModel = viewModel,
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("×2 Combo!").assertIsDisplayed()
    }

    @Test
    fun trainingScreen_hidesCombo_whenComboIsOne() {
        val viewModel =
            createMockViewModel(
                state =
                    TrainingState(
                        level = 1,
                        countdown = 0,
                        comboCount = 1,
                    ),
            )

        composeTestRule.setContent {
            TrainingScreen(
                navController = rememberNavController(),
                viewModel = viewModel,
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("×1 Combo!").assertDoesNotExist()
    }

    @Test
    fun trainingScreen_displaysStars_inCompletionDialog() {
        val viewModel =
            createMockViewModel(
                state =
                    TrainingState(
                        level = 1,
                        countdown = 0,
                        isSessionComplete = true,
                        earnedStars = 2,
                    ),
            )

        composeTestRule.setContent {
            TrainingScreen(
                navController = rememberNavController(),
                viewModel = viewModel,
            )
        }

        composeTestRule.waitForIdle()

        // Should show 2 filled stars and 1 outlined star
        composeTestRule.onAllNodesWithContentDescription("Star filled").assertCountEquals(2)
        composeTestRule.onAllNodesWithContentDescription("Star outlined").assertCountEquals(1)
    }

    @Test
    fun trainingScreen_displaysFinalScore_inCompletionDialog() {
        val viewModel =
            createMockViewModel(
                state =
                    TrainingState(
                        level = 1,
                        countdown = 0,
                        isSessionComplete = true,
                        currentScore = 1500,
                    ),
            )

        composeTestRule.setContent {
            TrainingScreen(
                navController = rememberNavController(),
                viewModel = viewModel,
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Final Score: 1500").assertIsDisplayed()
    }

    @Test
    fun trainingScreen_displaysNewSessionBest_whenBestScoreAchieved() {
        val viewModel =
            createMockViewModel(
                state =
                    TrainingState(
                        level = 1,
                        countdown = 0,
                        isSessionComplete = true,
                        currentScore = 2000,
                        sessionBestScore = 2000,
                    ),
            )

        composeTestRule.setContent {
            TrainingScreen(
                navController = rememberNavController(),
                viewModel = viewModel,
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("New Session Best: 2000").assertIsDisplayed()
    }

    @Test
    fun trainingScreen_displaysSessionBest_whenNotBeatingPrevious() {
        val viewModel =
            createMockViewModel(
                state =
                    TrainingState(
                        level = 1,
                        countdown = 0,
                        isSessionComplete = true,
                        currentScore = 1500,
                        sessionBestScore = 2000,
                    ),
            )

        composeTestRule.setContent {
            TrainingScreen(
                navController = rememberNavController(),
                viewModel = viewModel,
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Session Best: 2000").assertIsDisplayed()
        composeTestRule.onNodeWithText("New Session Best: 2000").assertDoesNotExist()
    }

    @Test
    fun trainingScreen_playReferenceNoteButton_callsViewModelPlayReferenceNote() {
        val viewModel =
            createMockViewModel(
                state =
                    TrainingState(
                        level = 1,
                        countdown = 0,
                        currentSwar = "S",
                    ),
            )

        composeTestRule.setContent {
            TrainingScreen(
                navController = rememberNavController(),
                viewModel = viewModel,
            )
        }

        composeTestRule.waitForIdle()

        // Find and click the play reference note button by its text
        composeTestRule.onNodeWithText("🔊 Play Note").performClick()
        composeTestRule.waitForIdle()

        verify { viewModel.playReferenceNote() }
    }

    // Helper function to create a mock ViewModel
    private fun createMockViewModel(
        state: TrainingState = TrainingState(level = 1),
        isTanpuraPlaying: Boolean = false,
        saNote: String = "C3",
    ): TrainingViewModel {
        val viewModel = mockk<TrainingViewModel>(relaxed = true)
        every { viewModel.state } returns MutableStateFlow(state)
        every { viewModel.isTanpuraPlaying } returns MutableStateFlow(isTanpuraPlaying)
        every { viewModel.saNote } returns MutableStateFlow(saNote)
        return viewModel
    }
}
