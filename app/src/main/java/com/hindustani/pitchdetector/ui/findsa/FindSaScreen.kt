package com.hindustani.pitchdetector.ui.findsa

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hindustani.pitchdetector.ui.components.PianoKeyboardSelector
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
    onSaSelected: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle system back button (phone back button)
    BackHandler {
        if (uiState.currentState is FindSaState.NotStarted) {
            // Go back to mode selection
            viewModel.resetToModeSelection()
        } else {
            viewModel.stopPlaying()
            viewModel.resetTest()
            onNavigateBack()
        }
    }

    // Dynamic title based on state
    val topBarTitle =
        when (uiState.currentState) {
            is FindSaState.NotStarted -> "Choose Test Method"
            else -> "Find Your Sa"
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topBarTitle) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.currentState is FindSaState.NotStarted) {
                            // Go back to mode selection
                            viewModel.resetToModeSelection()
                        } else {
                            viewModel.stopPlaying()
                            viewModel.resetTest()
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Error message if present
            uiState.error?.let { error ->
                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            // Render different UI based on current state
            when (val state = uiState.currentState) {
                is FindSaState.SelectingMode ->
                    ModeSelectionView(
                        selectedMode = uiState.testMode,
                        onModeSelected = { mode -> viewModel.setTestMode(mode) },
                    )
                is FindSaState.NotStarted ->
                    NotStartedView(
                        testMode = uiState.testMode,
                        onStartTest = { viewModel.startTest() },
                    )
                is FindSaState.RecordingSpeech ->
                    SpeechRecordingView(
                        currentPitch = uiState.currentPitch,
                        samplesCount = uiState.collectedSamplesCount,
                        testMode = uiState.testMode,
                        onNext = { viewModel.stopSpeechTest() },
                    )
                is FindSaState.RecordingSinging ->
                    RecordingView(
                        currentPitch = uiState.currentPitch,
                        samplesCount = uiState.collectedSamplesCount,
                        onStopTest = { viewModel.stopTest() },
                    )
                is FindSaState.Analyzing -> AnalyzingView()
                is FindSaState.Finished ->
                    ResultsView(
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
                        onTryAgain = { viewModel.resetTest() },
                    )
            }
        }
    }
}

/**
 * Mode selection view - lets user choose test mode
 */
@Composable
fun ModeSelectionView(
    selectedMode: TestMode,
    onModeSelected: (TestMode) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Choose Test Method",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Speaking Only Mode
        ModeCard(
            title = "Speaking Voice",
            duration = "~10 seconds",
            description = "Quick test based on your natural speaking pitch. Best for finding a comfortable Sa for long practice sessions.",
            isSelected = selectedMode == TestMode.SPEAKING_ONLY,
            onClick = { onModeSelected(TestMode.SPEAKING_ONLY) },
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Singing Only Mode
        ModeCard(
            title = "Singing Range",
            duration = "~20 seconds",
            description = "Traditional method analyzing your full vocal range. Best for performance-oriented training.",
            isSelected = selectedMode == TestMode.SINGING_ONLY,
            onClick = { onModeSelected(TestMode.SINGING_ONLY) },
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Both Mode
        ModeCard(
            title = "Both (Recommended)",
            duration = "~30 seconds",
            description = "Combines both methods for the most accurate recommendation. Analyzes both speaking pitch and vocal range.",
            isSelected = selectedMode == TestMode.BOTH,
            onClick = { onModeSelected(TestMode.BOTH) },
            isRecommended = true,
        )
    }
}

/**
 * Reusable mode selection card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeCard(
    title: String,
    duration: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isRecommended: Boolean = false,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
            ),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color =
                        if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
                Text(
                    text = duration,
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            }

            if (isRecommended) {
                Text(
                    text = "Recommended",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color =
                    if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
        }
    }
}

/**
 * Initial view with instructions
 */
@Composable
fun NotStartedView(
    testMode: TestMode,
    onStartTest: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Find Your Ideal Sa",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Show selected test method
        val methodText =
            when (testMode) {
                TestMode.SPEAKING_ONLY -> "Speaking Voice Method"
                TestMode.SINGING_ONLY -> "Singing Range Method"
                TestMode.BOTH -> "Speaking + Singing Method"
            }
        Text(
            text = methodText,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "How it works:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                Spacer(modifier = Modifier.height(12.dp))

                val instructions =
                    when (testMode) {
                        TestMode.SPEAKING_ONLY ->
                            "1. Find a quiet place\n\n" +
                                "2. Press 'Start Test' to begin\n\n" +
                                "3. Count upwards slowly in your natural speaking voice\n\n" +
                                "4. Continue for about 10 seconds until the indicator shows 'Ready'\n\n" +
                                "5. We'll analyze your speaking pitch to recommend a comfortable Sa"

                        TestMode.SINGING_ONLY ->
                            "1. Find a quiet place\n\n" +
                                "2. Press 'Start Test' to begin\n\n" +
                                "3. Sing 'aaaaah' and glide to your lowest comfortable note, hold it\n\n" +
                                "4. Then glide to your highest comfortable note (avoid falsetto), hold it\n\n" +
                                "5. We'll analyze your vocal range to recommend the ideal Sa"

                        TestMode.BOTH ->
                            "1. Find a quiet place\n\n" +
                                "2. Press 'Start Test' to begin\n\n" +
                                "Phase 1: Speaking Voice\n" +
                                "• Count upwards slowly in your natural speaking voice\n\n" +
                                "Phase 2: Singing Range\n" +
                                "• Sing 'aaaaah' and glide to your lowest comfortable note, hold it\n" +
                                "• Then glide to your highest comfortable note (avoid falsetto), hold it\n\n" +
                                "3. We'll analyze both your speaking pitch and vocal range to recommend the ideal Sa for you"
                    }

                Text(
                    text = instructions,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStartTest,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
        ) {
            Text(
                text = "Start Test",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

/**
 * Speech recording view for capturing natural speaking voice
 */
@Composable
fun SpeechRecordingView(
    currentPitch: Float,
    samplesCount: Int,
    testMode: TestMode,
    onNext: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Phase 1: Speaking",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Count upwards slowly and naturally",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )

                Spacer(modifier = Modifier.height(32.dp))

                PitchDisplay(currentPitch)

                Spacer(modifier = Modifier.height(16.dp))

                SampleCollectionProgress(
                    samplesCount = samplesCount,
                    requiredSamples = FindSaViewModel.MIN_SPEECH_SAMPLES,
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        val buttonText =
            when (testMode) {
                TestMode.SPEAKING_ONLY -> "Stop Test"
                TestMode.BOTH -> "Next: Vocal Range"
                else -> "Next"
            }

        val buttonColors =
            if (testMode == TestMode.SPEAKING_ONLY) {
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            } else {
                ButtonDefaults.buttonColors()
            }

        Button(
            onClick = onNext,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            enabled = samplesCount >= FindSaViewModel.MIN_SPEECH_SAMPLES,
            colors = buttonColors,
        ) {
            Text(
                text = buttonText,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val helperText =
            when (testMode) {
                TestMode.SPEAKING_ONLY -> "Wait for the 'Ready' indicator before stopping"
                else -> "Keep counting until ready"
            }

        Text(
            text = helperText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Singing recording view with real-time feedback
 */
@Composable
fun RecordingView(
    currentPitch: Float,
    samplesCount: Int,
    onStopTest: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Phase 2: Singing Range",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text =
                        "1. Start singing 'aaaaah'\n" +
                            "2. Glide to your lowest comfortable note & hold\n" +
                            "3. Glide to your highest comfortable note (avoid falsetto) & hold\n" +
                            "4. Keep singing until the indicator shows 'Ready'",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )

                Spacer(modifier = Modifier.height(24.dp))

                PitchDisplay(currentPitch)

                Spacer(modifier = Modifier.height(16.dp))

                SampleCollectionProgress(
                    samplesCount = samplesCount,
                    requiredSamples = FindSaViewModel.MIN_SINGING_SAMPLES,
                    readyText = "✓ Ready to analyze",
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStopTest,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
        ) {
            Text(
                text = "Stop Test",
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Wait for the 'Ready' indicator before stopping",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Analyzing your voice...",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
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
    onTryAgain: () -> Unit,
) {
    var isPlaying by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Recommended Sa: ${state.originalSa.name}",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Test mode indicator
        val testMethodText =
            when (state.testMode) {
                TestMode.SPEAKING_ONLY -> "Based on speaking voice"
                TestMode.SINGING_ONLY -> "Based on singing range"
                TestMode.BOTH -> "Based on speaking & singing"
            }
        Text(
            text = testMethodText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Frequency display
        Text(
            text = "${state.recommendedSa.frequency.roundToInt()} Hz",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        PianoKeyboardSelector(
            selectedSa = state.recommendedSa.name,
            onSaSelected = {},
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Detected range info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "Your comfortable range:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Lowest: ${state.lowestNote.name}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "Highest: ${state.highestNote.name}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Adjustment controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = {
                    onAdjust(-1)
                    // If tanpura is playing, update it to the new Sa
                    if (isPlaying) {
                        onListen()
                    }
                },
                modifier = Modifier.weight(1f),
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
                modifier = Modifier.weight(2f),
            ) {
                Text(if (isPlaying) "Stop" else "Listen")
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = {
                    onAdjust(1)
                    // If tanpura is playing, update it to the new Sa
                    if (isPlaying) {
                        onListen()
                    }
                },
                modifier = Modifier.weight(1f),
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
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
        ) {
            Text(
                text = "Accept and Save",
                style = MaterialTheme.typography.titleMedium,
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
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Try Again")
        }
    }
}

/**
 * Reusable composable for displaying current pitch or loading indicator
 */
@Composable
private fun PitchDisplay(currentPitch: Float) {
    if (currentPitch > 0) {
        Text(
            text = "${currentPitch.roundToInt()} Hz",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    } else {
        CircularProgressIndicator(modifier = Modifier.size(64.dp))
    }
}

/**
 * Reusable composable for showing sample collection progress
 */
@Composable
private fun SampleCollectionProgress(
    samplesCount: Int,
    requiredSamples: Int,
    readyText: String = "✓ Ready",
) {
    val isReady = samplesCount >= requiredSamples
    val progress =
        if (samplesCount < requiredSamples) {
            samplesCount.toFloat() / requiredSamples
        } else {
            1f
        }

    if (isReady) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = readyText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LinearProgressIndicator(
                progress = progress,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Collecting samples...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
