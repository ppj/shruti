package com.hindustani.pitchdetector.ui.training

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hindustani.pitchdetector.viewmodel.TrainingViewModel
import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Training screen for ear training exercises.
 * Users tune a virtual slider to match a target note.
 */
@Composable
fun TrainingScreen(viewModel: TrainingViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val progress by viewModel.progress.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header with level, score, and progress
            HeaderSection(
                level = uiState.currentLevel,
                tolerance = uiState.toleranceCents,
                totalScore = progress.totalScore,
                successCount = uiState.successfulExercisesThisLevel,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Target note display
            TargetNoteDisplay(targetNote = uiState.targetNote)

            Spacer(modifier = Modifier.height(24.dp))

            // Timer
            if (uiState.startTime != null && !uiState.showResult) {
                TimerDisplay(startTime = uiState.startTime!!)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tuning controls
            TuningControls(
                currentFrequency = uiState.currentSliderFrequency,
                targetFrequency = uiState.targetFrequency ?: 261.63,
                onFrequencyChange = viewModel::onSliderChanged,
                onReplay = viewModel::onReplayNote,
                enabled = !uiState.showResult,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Check button
            val sliderMoved =
                uiState.detunedFrequency != null &&
                    kotlin.math.abs(uiState.currentSliderFrequency - uiState.detunedFrequency!!) > 0.01

            Button(
                onClick = viewModel::onCheckTuning,
                enabled = sliderMoved && !uiState.showResult,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Check My Tuning")
            }
        }

        // Result overlay
        AnimatedVisibility(
            visible = uiState.showResult,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        ) {
            ResultOverlay(
                isCorrect = uiState.isCorrect ?: false,
                deviationCents = uiState.deviationCents ?: 0.0,
                score = uiState.scoreThisRound,
                onNext = viewModel::onNextExercise,
            )
        }
    }
}

@Composable
private fun HeaderSection(
    level: Int,
    tolerance: Double,
    totalScore: Int,
    successCount: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Level $level",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Tolerance: ±${tolerance.roundToInt()}¢",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Score",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = totalScore.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress to next level
            Column {
                Text(
                    text = "Progress: $successCount/10",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { successCount / 10f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun TargetNoteDisplay(targetNote: String?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Tune to:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = targetNote ?: "—",
            style = MaterialTheme.typography.displayLarge,
            fontSize = 80.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun TimerDisplay(startTime: Long) {
    var elapsedSeconds by remember { mutableLongStateOf(0L) }

    LaunchedEffect(startTime) {
        while (true) {
            elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000
            delay(100)
        }
    }

    Text(
        text = "Time: ${elapsedSeconds}s",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun TuningControls(
    currentFrequency: Double,
    targetFrequency: Double,
    onFrequencyChange: (Double) -> Unit,
    onReplay: () -> Unit,
    enabled: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Current: ${currentFrequency.roundToInt()} Hz",
                style = MaterialTheme.typography.bodyMedium,
            )

            IconButton(
                onClick = onReplay,
                enabled = enabled,
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Replay note",
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Frequency slider (±100 cents range)
        val minFreq = targetFrequency * 2.0.pow(-100.0 / 1200.0)
        val maxFreq = targetFrequency * 2.0.pow(100.0 / 1200.0)

        Slider(
            value = currentFrequency.toFloat(),
            onValueChange = { onFrequencyChange(it.toDouble()) },
            valueRange = minFreq.toFloat()..maxFreq.toFloat(),
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ResultOverlay(
    isCorrect: Boolean,
    deviationCents: Double,
    score: Int,
    onNext: () -> Unit,
) {
    val backgroundColor =
        when {
            isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.95f) // Green
            kotlin.math.abs(deviationCents) < 30 -> Color(0xFFFF9800).copy(alpha = 0.95f) // Orange
            else -> Color(0xFFF44336).copy(alpha = 0.95f) // Red
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.padding(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Success/Fail icon
                Icon(
                    imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = if (isCorrect) "Success" else "Try again",
                    modifier = Modifier.size(64.dp),
                    tint = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Result text
                Text(
                    text = if (isCorrect) "Excellent!" else "Try Again",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Deviation
                Text(
                    text = "Deviation:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                )
                Text(
                    text = "${if (deviationCents > 0) "+" else ""}${deviationCents.roundToInt()}¢",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Score
                Text(
                    text = "Score:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                )
                Text(
                    text = "+$score",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50),
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Next button
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Next Exercise")
                }
            }
        }
    }
}
