package com.hindustani.pitchdetector.ui.training

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hindustani.pitchdetector.constants.AppRoutes
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrainingMenuScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun trainingMenuScreen_findSaReminderIsDisplayed() {
        composeTestRule.setContent {
            TrainingMenuScreen(
                navController = rememberNavController(),
            )
        }

        composeTestRule.onNodeWithText("Have you found your Sa?").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Tip").assertIsDisplayed()
    }

    @Test
    fun trainingMenuScreen_tooltipActionButton_navigatesToFindSaScreen() {
        var navigatedToFindSa = false

        composeTestRule.setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = AppRoutes.TRAINING_MENU) {
                composable(AppRoutes.TRAINING_MENU) {
                    TrainingMenuScreen(navController = navController)
                }
                composable(AppRoutes.FIND_SA) {
                    navigatedToFindSa = true
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Tip").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Find My Sa").assertIsDisplayed()
        composeTestRule.onNodeWithText("Find My Sa").performClick()
        composeTestRule.waitForIdle()

        assert(navigatedToFindSa) { "Expected navigation to Find Sa screen" }
    }

    @Test
    fun trainingMenuScreen_displaysTrainingTitle() {
        composeTestRule.setContent {
            TrainingMenuScreen(
                navController = rememberNavController(),
            )
        }

        composeTestRule.onNodeWithText("Training").assertIsDisplayed()
    }

    @Test
    fun trainingMenuScreen_displaysVoiceTrainingCard() {
        composeTestRule.setContent {
            TrainingMenuScreen(
                navController = rememberNavController(),
            )
        }

        composeTestRule.onNodeWithText("Voice Training").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Practice swar-singing accuracy with real-time feedback")
            .assertIsDisplayed()
    }

    @Test
    fun trainingMenuScreen_displaysAllLevels() {
        composeTestRule.setContent {
            TrainingMenuScreen(
                navController = rememberNavController(),
            )
        }

        composeTestRule.onNodeWithText("Level 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Level 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Level 3").assertIsDisplayed()
        composeTestRule.onNodeWithText("Level 4").assertIsDisplayed()
    }

    @Test
    fun trainingMenuScreen_backButton_navigatesBack() {
        var popBackStackCalled = false

        composeTestRule.setContent {
            val navController = rememberNavController()
            navController.addOnDestinationChangedListener { _, destination, _ ->
                if (destination.route == null) {
                    popBackStackCalled = true
                }
            }

            NavHost(navController = navController, startDestination = AppRoutes.TRAINING_MENU) {
                composable(AppRoutes.TRAINING_MENU) {
                    TrainingMenuScreen(navController = navController)
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()
        composeTestRule.waitForIdle()

        assert(popBackStackCalled || composeTestRule.activity.isFinishing) {
            "Expected back navigation"
        }
    }

    @Test
    fun trainingMenuScreen_level1Button_navigatesToLevel1() {
        var navigatedToTraining1 = false

        composeTestRule.setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = AppRoutes.TRAINING_MENU) {
                composable(AppRoutes.TRAINING_MENU) {
                    TrainingMenuScreen(navController = navController)
                }
                composable("${AppRoutes.TRAINING}/1") {
                    navigatedToTraining1 = true
                }
            }
        }

        composeTestRule.onNodeWithText("Level 1").performClick()
        composeTestRule.waitForIdle()

        assert(navigatedToTraining1) { "Expected navigation to training level 1" }
    }

    @Test
    fun trainingMenuScreen_level2Button_navigatesToLevel2() {
        var navigatedToTraining2 = false

        composeTestRule.setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = AppRoutes.TRAINING_MENU) {
                composable(AppRoutes.TRAINING_MENU) {
                    TrainingMenuScreen(navController = navController)
                }
                composable("${AppRoutes.TRAINING}/2") {
                    navigatedToTraining2 = true
                }
            }
        }

        composeTestRule.onNodeWithText("Level 2").performClick()
        composeTestRule.waitForIdle()

        assert(navigatedToTraining2) { "Expected navigation to training level 2" }
    }

    @Test
    fun trainingMenuScreen_level3Button_navigatesToLevel3() {
        var navigatedToTraining3 = false

        composeTestRule.setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = AppRoutes.TRAINING_MENU) {
                composable(AppRoutes.TRAINING_MENU) {
                    TrainingMenuScreen(navController = navController)
                }
                composable("${AppRoutes.TRAINING}/3") {
                    navigatedToTraining3 = true
                }
            }
        }

        composeTestRule.onNodeWithText("Level 3").performClick()
        composeTestRule.waitForIdle()

        assert(navigatedToTraining3) { "Expected navigation to training level 3" }
    }

    @Test
    fun trainingMenuScreen_level4Button_navigatesToLevel4() {
        var navigatedToTraining4 = false

        composeTestRule.setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = AppRoutes.TRAINING_MENU) {
                composable(AppRoutes.TRAINING_MENU) {
                    TrainingMenuScreen(navController = navController)
                }
                composable("${AppRoutes.TRAINING}/4") {
                    navigatedToTraining4 = true
                }
            }
        }

        composeTestRule.onNodeWithText("Level 4").performClick()
        composeTestRule.waitForIdle()

        assert(navigatedToTraining4) { "Expected navigation to training level 4" }
    }
}
