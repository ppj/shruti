package com.hindustani.pitchdetector.ui.findsa

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hindustani.pitchdetector.viewmodel.FindSaViewModel
import kotlin.math.roundToInt

/**
 * Main screen for the Find Your Sa feature
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindSaScreen(
    viewModel: FindSaViewModel,
    onNavigateBack: () -> Unit,
    onSaSelected: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find Your Sa") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetTest()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Error message if present
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Render different UI based on current state
            when (val state = uiState.currentState) {
                is FindSaState.NotStarted -> NotStartedView(
                    onStartTest = { viewModel.startTest() }
                )
                is FindSaState.Recording -> RecordingView(
                    currentPitch = uiState.currentPitch,
                    samplesCount = uiState.collectedSamplesCount,
                    onStopTest = { viewModel.stopTest() }
                )
                is FindSaState.Analyzing -> AnalyzingView()
                is FindSaState.Finished -> ResultsView(
                    state = state,
                    onAdjust = { semitones -> viewModel.adjustRecommendation(semitones) },
                    onListen = { viewModel.playRecommendedSa() },
                    onStopListening = { viewModel.stopPlaying() },
                    onAccept = {
                        val saNote = viewModel.getRecommendedSaNote()
                        if (saNote != null) {
                            onSaSelected(saNote)
                            viewModel.resetTest()
                            onNavigateBack()
                        }
                    },
                    onTryAgain = { viewModel.resetTest() }
                )
            }
        }
    }
}

/**
 * Initial view with instructions
 */
@Composable
fun NotStartedView(onStartTest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Find Your Ideal Sa",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "How it works:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "1. Find a quiet place\n\n" +
                           "2. Press 'Start Test' and sing 'aaaaah'\n\n" +
                           "3. Glide to your lowest comfortable note first, hold it for a couple of seconds\n\n" +
                           "4. Then glide to your highest comfortable note (avoid falsetto), hold it for a couple of seconds\n\n" +
                           "5. We'll analyze your voice and recommend the ideal Sa for you",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStartTest,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Start Test",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

/**
 * Recording view with real-time feedback
 */
@Composable
fun RecordingView(
    currentPitch: Float,
    samplesCount: Int,
    onStopTest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Listening...",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "1. Start singing 'aaaaah'\n" +
                           "2. Glide to your lowest comfortable note & hold for a second\n" +
                           "3. Glide to your highest comfortable note (avoid falsetto), hold it for a second",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (currentPitch > 0) {
                    Text(
                        text = "${currentPitch.roundToInt()} Hz",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Samples collected: $samplesCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStopTest,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(
                text = "Stop Test",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sing for at least 10 seconds",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Analyzing view with progress indicator
 */
@Composable
fun AnalyzingView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Analyzing your voice...",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Results view with recommendation
 */
@Composable
fun ResultsView(
    state: FindSaState.Finished,
    onAdjust: (Int) -> Unit,
    onListen: () -> Unit,
    onStopListening: () -> Unit,
    onAccept: () -> Unit,
    onTryAgain: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your Recommended Sa",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Large Sa display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.recommendedSa.name,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${state.recommendedSa.frequency.roundToInt()} Hz",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Detected range info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Your comfortable range:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Lowest: ${state.lowestNote.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Highest: ${state.highestNote.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Adjustment controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { onAdjust(-1) },
                modifier = Modifier.weight(1f)
            ) {
                Text("-1")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (isPlaying) {
                        onStopListening()
                        isPlaying = false
                    } else {
                        onListen()
                        isPlaying = true
                    }
                },
                modifier = Modifier.weight(2f)
            ) {
                Text(if (isPlaying) "Stop" else "Listen")
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = { onAdjust(1) },
                modifier = Modifier.weight(1f)
            ) {
                Text("+1")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Primary action button
        Button(
            onClick = {
                if (isPlaying) onStopListening()
                onAccept()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Accept and Save",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Try again button
        TextButton(
            onClick = {
                if (isPlaying) {
                    onStopListening()
                    isPlaying = false
                }
                onTryAgain()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Try Again")
        }
    }
}
