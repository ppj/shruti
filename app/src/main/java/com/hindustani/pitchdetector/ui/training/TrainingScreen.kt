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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.hindustani.pitchdetector.R
import com.hindustani.pitchdetector.ui.components.PianoKeyboardSelector
import com.hindustani.pitchdetector.ui.components.TanpuraCard
import com.hindustani.pitchdetector.ui.theme.TrainingCorrect
import com.hindustani.pitchdetector.viewmodel.TrainingViewModel

private const val FULL_PROGRESS = 1f
private val PROGRESS_INDICATOR_SIZE = 200.dp
private val PROGRESS_INDICATOR_STROKE_WIDTH = 16.dp
private val TARGET_NOTE_FONT_SIZE = 72.sp
private const val MIN_COMBO_TO_DISPLAY = 2

/**
 * Training screen where users practice holding swars accurately
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

            Column(
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = stringResource(R.string.text_training_level, state.level),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (state.countdown == 0 && !state.isSessionComplete) {
                    Text(
                        text = stringResource(R.string.text_score, state.currentScore),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        PianoKeyboardSelector(
            selectedSa = saNote,
            onSaSelected = {}, // Display only, not clickable
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.countdown > 0) {
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

        if (state.countdown == 0 && !state.isSessionComplete) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        progress = { FULL_PROGRESS },
                        modifier = Modifier.size(PROGRESS_INDICATOR_SIZE),
                        strokeWidth = PROGRESS_INDICATOR_STROKE_WIDTH,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    )

                    CircularProgressIndicator(
                        progress = { state.holdProgress },
                        modifier = Modifier.size(PROGRESS_INDICATOR_SIZE),
                        strokeWidth = PROGRESS_INDICATOR_STROKE_WIDTH,
                        color = if (state.isHoldingCorrectly) TrainingCorrect else MaterialTheme.colorScheme.primary,
                    )

                    Text(
                        text = state.currentSwar ?: stringResource(R.string.text_no_note),
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = TARGET_NOTE_FONT_SIZE,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (state.comboCount >= MIN_COMBO_TO_DISPLAY) {
                    Text(
                        text = stringResource(R.string.text_combo, state.comboCount),
                        style = MaterialTheme.typography.titleLarge,
                        color = TrainingCorrect,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text =
                        when {
                            state.isHoldingCorrectly -> stringResource(R.string.text_holding_correctly)
                            state.isFlat -> stringResource(R.string.text_pitch_flat)
                            state.isSharp -> stringResource(R.string.text_pitch_sharp)
                            else -> stringResource(R.string.text_sing_to_start)
                        },
                    style = MaterialTheme.typography.titleMedium,
                    color =
                        when {
                            state.isHoldingCorrectly -> TrainingCorrect
                            state.isFlat || state.isSharp -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    fontWeight = if (state.isHoldingCorrectly || state.isFlat || state.isSharp) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TanpuraCard(
            isTanpuraPlaying = isTanpuraPlaying,
            onToggleTanpura = { viewModel.toggleTanpura() },
            showString1Selector = false,
        )

        Spacer(modifier = Modifier.weight(1f))

        if (state.isSessionComplete) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CompletionDialog(
                    level = state.level,
                    earnedStars = state.earnedStars,
                    finalScore = state.currentScore,
                    sessionBestScore = state.sessionBestScore,
                    onDismiss = { navController.popBackStack() },
                    onRepeat = { viewModel.resetSession() },
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun CompletionDialog(
    level: Int,
    earnedStars: Int,
    finalScore: Int,
    sessionBestScore: Int,
    onDismiss: () -> Unit,
    onRepeat: () -> Unit,
) {
    val isNewBest = finalScore == sessionBestScore && finalScore > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.text_congratulations),
                style = MaterialTheme.typography.headlineMedium,
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    repeat(3) { index ->
                        Icon(
                            imageVector = if (index < earnedStars) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription =
                                if (index < earnedStars) {
                                    stringResource(R.string.content_description_star_filled)
                                } else {
                                    stringResource(R.string.content_description_star_outlined)
                                },
                            tint = if (index < earnedStars) TrainingCorrect else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.text_level_complete, level),
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.text_final_score, finalScore),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                if (isNewBest) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.text_new_session_best, sessionBestScore),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TrainingCorrect,
                        fontWeight = FontWeight.Bold,
                    )
                } else if (sessionBestScore > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.text_session_best, sessionBestScore),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
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
