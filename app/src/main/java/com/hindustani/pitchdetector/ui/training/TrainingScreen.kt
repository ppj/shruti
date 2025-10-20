package com.hindustani.pitchdetector.ui.training

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.hindustani.pitchdetector.ui.components.PianoKeyboardSelector
import com.hindustani.pitchdetector.ui.components.TanpuraCard
import com.hindustani.pitchdetector.viewmodel.TrainingViewModel

/**
 * Training screen where users practice holding swaras accurately
 * Layout matches MainScreen for consistency
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
    val isTanpuraPlaying by viewModel.isTanpuraPlaying.collectAsState()
    val saNote by viewModel.saNote.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top bar with back button, Sa display, and level indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.content_description_navigate_back),
                    )
                }

                Text(
                    text = stringResource(R.string.text_sa_label, saNote),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Text(
                text = stringResource(R.string.text_training_level, state.level),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Piano keyboard selector (display only, not interactive)
        PianoKeyboardSelector(
            selectedSa = saNote,
            onSaSelected = {}, // Empty lambda - display only, not clickable
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Countdown or instructions (replaces detected swara section)
        if (state.countdown > 0) {
            // Show countdown
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.text_get_ready),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.countdown.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Circular progress indicator with target note (replaces PitchBar)
        if (state.countdown == 0 && !state.isSessionComplete) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Next note preview above circle
                state.nextSwara?.let { nextNote ->
                    Text(
                        text = stringResource(R.string.text_next_note, nextNote),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Circular progress with target note in center
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
                        progress = { state.holdProgress },
                        modifier = Modifier.size(200.dp),
                        strokeWidth = 16.dp,
                        color = if (state.isHoldingCorrectly) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    )

                    // Target swara in center
                    Text(
                        text = state.currentSwara ?: "—",
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Feedback text
                Text(
                    text =
                        if (state.isHoldingCorrectly) {
                            stringResource(R.string.text_holding_correctly)
                        } else {
                            stringResource(R.string.text_sing_to_start)
                        },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (state.isHoldingCorrectly) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (state.isHoldingCorrectly) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tanpura control
        TanpuraCard(
            isTanpuraPlaying = isTanpuraPlaying,
            onToggleTanpura = { viewModel.toggleTanpura() },
            showString1Selector = false,
        )

        Spacer(modifier = Modifier.weight(1f))

        // Session complete or progress info
        if (state.isSessionComplete) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CompletionDialog(
                    level = state.level,
                    onDismiss = { navController.popBackStack() },
                    onRepeat = { viewModel.resetSession() },
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
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
