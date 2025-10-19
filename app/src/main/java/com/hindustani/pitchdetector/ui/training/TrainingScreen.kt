package com.hindustani.pitchdetector.ui.training

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.hindustani.pitchdetector.R
import com.hindustani.pitchdetector.viewmodel.TrainingViewModel

/**
 * Training screen where users practice holding swaras accurately
 *
 * @param navController Navigation controller for back navigation
 * @param viewModel TrainingViewModel instance
 */
@Composable
fun TrainingScreen(
    navController: NavController,
    viewModel: TrainingViewModel,
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top bar with back button and level indicator
        Row(
            modifier = Modifier.padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.content_description_navigate_back),
                )
            }

            Text(
                text = stringResource(R.string.text_training_level, state.level),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        // Main content area
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.countdown > 0 -> {
                    // Show countdown before starting
                    CountdownDisplay(countdown = state.countdown)
                }
                state.isSessionComplete -> {
                    // Show completion dialog
                    CompletionDialog(
                        level = state.level,
                        onDismiss = { navController.popBackStack() },
                        onRepeat = { viewModel.resetSession() },
                    )
                }
                else -> {
                    // Main training UI with progress indicator
                    TrainingContent(
                        currentSwara = state.currentSwara ?: "",
                        nextSwara = state.nextSwara,
                        holdProgress = state.holdProgress,
                        isHoldingCorrectly = state.isHoldingCorrectly,
                    )
                }
            }
        }
    }
}

/**
 * Countdown display shown before training starts
 */
@Composable
private fun CountdownDisplay(countdown: Int) {
    val animatedScale by animateFloatAsState(
        targetValue = 1.2f,
        animationSpec = tween(durationMillis = 300),
        label = "countdown_scale",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.text_get_ready),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = countdown.toString(),
            style = MaterialTheme.typography.displayLarge,
            fontSize = 96.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * Main training content with circular progress indicator and swara display
 */
@Composable
private fun TrainingContent(
    currentSwara: String,
    nextSwara: String?,
    holdProgress: Float,
    isHoldingCorrectly: Boolean,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Next note preview
        Text(
            text = if (nextSwara != null) {
                stringResource(R.string.text_next_note, nextSwara)
            } else {
                stringResource(R.string.text_last_note)
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Circular progress indicator with target swara in center
        Box(
            contentAlignment = Alignment.Center,
        ) {
            // Background circle
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(200.dp),
                strokeWidth = 16.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
            )

            // Progress circle
            CircularProgressIndicator(
                progress = { holdProgress },
                modifier = Modifier.size(200.dp),
                strokeWidth = 16.dp,
                color = if (isHoldingCorrectly) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
            )

            // Target swara text in center
            Text(
                text = currentSwara,
                style = MaterialTheme.typography.displayLarge,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Feedback text
        Text(
            text = if (isHoldingCorrectly) {
                stringResource(R.string.text_holding_correctly)
            } else {
                stringResource(R.string.text_sing_and_hold)
            },
            style = MaterialTheme.typography.titleMedium,
            color = if (isHoldingCorrectly) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isHoldingCorrectly) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

/**
 * Completion dialog shown when all notes are completed
 */
@Composable
private fun CompletionDialog(
    level: Int,
    onDismiss: () -> Unit,
    onRepeat: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.text_congratulations),
                style = MaterialTheme.typography.headlineMedium,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.text_level_complete, level),
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_back_to_main))
            }
        },
        dismissButton = {
            TextButton(onClick = onRepeat) {
                Text(stringResource(R.string.button_repeat_level))
            }
        },
    )
}
