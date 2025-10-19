package com.hindustani.pitchdetector.ui.findsa

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hindustani.pitchdetector.R
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
            is FindSaState.NotStarted -> stringResource(R.string.title_choose_test_method)
            else -> stringResource(R.string.title_find_your_sa)
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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_description_back),
                        )
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
            text = stringResource(R.string.title_choose_test_method),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Speaking Only Mode
        ModeCard(
            title = stringResource(R.string.title_speaking_voice),
            duration = stringResource(R.string.text_duration_10_seconds),
            description = stringResource(R.string.description_speaking_voice),
            isSelected = selectedMode == TestMode.SPEAKING_ONLY,
            onClick = { onModeSelected(TestMode.SPEAKING_ONLY) },
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Singing Only Mode
        ModeCard(
            title = stringResource(R.string.title_singing_range),
            duration = stringResource(R.string.text_duration_20_seconds),
            description = stringResource(R.string.description_singing_range),
            isSelected = selectedMode == TestMode.SINGING_ONLY,
            onClick = { onModeSelected(TestMode.SINGING_ONLY) },
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Both Mode
        ModeCard(
            title = stringResource(R.string.title_both_recommended),
            duration = stringResource(R.string.text_duration_30_seconds),
            description = stringResource(R.string.description_both_recommended),
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
                    text = stringResource(R.string.text_recommended),
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
            text = stringResource(R.string.text_find_your_ideal_sa),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Show selected test method
        val methodText =
            when (testMode) {
                TestMode.SPEAKING_ONLY -> stringResource(R.string.text_speaking_voice_method)
                TestMode.SINGING_ONLY -> stringResource(R.string.text_singing_range_method)
                TestMode.BOTH -> stringResource(R.string.text_speaking_singing_method)
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
                    text = stringResource(R.string.text_how_it_works),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                Spacer(modifier = Modifier.height(12.dp))

                val instructions =
                    when (testMode) {
                        TestMode.SPEAKING_ONLY -> stringResource(R.string.instructions_speaking_only)
                        TestMode.SINGING_ONLY -> stringResource(R.string.instructions_singing_only)
                        TestMode.BOTH -> stringResource(R.string.instructions_both)
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
                text = stringResource(R.string.button_start_test),
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
            text = stringResource(R.string.text_phase_1_speaking),
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
                    text = stringResource(R.string.text_count_upwards),
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
                TestMode.SPEAKING_ONLY -> stringResource(R.string.button_stop_test)
                TestMode.BOTH -> stringResource(R.string.button_next_vocal_range)
                else -> stringResource(R.string.button_next)
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
                TestMode.SPEAKING_ONLY -> stringResource(R.string.helper_wait_for_ready_stop)
                else -> stringResource(R.string.helper_keep_counting)
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
            text = stringResource(R.string.text_phase_2_singing_range),
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
                    text = stringResource(R.string.instructions_singing_range_detailed),
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
                    readyText = stringResource(R.string.text_ready_to_analyze),
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
                text = stringResource(R.string.button_stop_test),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.helper_wait_for_ready_stop),
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
            text = stringResource(R.string.text_analyzing_voice),
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
            text = stringResource(R.string.text_recommended_sa, state.originalSa.name),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Test mode indicator
        val testMethodText =
            when (state.testMode) {
                TestMode.SPEAKING_ONLY -> stringResource(R.string.text_based_on_speaking)
                TestMode.SINGING_ONLY -> stringResource(R.string.text_based_on_singing)
                TestMode.BOTH -> stringResource(R.string.text_based_on_both)
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
            text = stringResource(R.string.text_frequency_hz, state.recommendedSa.frequency.roundToInt()),
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
                    text = stringResource(R.string.text_your_comfortable_range),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.text_lowest_note, state.lowestNote.name),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(R.string.text_highest_note, state.highestNote.name),
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
                Text(stringResource(R.string.button_minus_1))
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
                Text(
                    if (isPlaying) {
                        stringResource(R.string.button_stop)
                    } else {
                        stringResource(R.string.button_listen)
                    },
                )
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
                Text(stringResource(R.string.button_plus_1))
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
                text = stringResource(R.string.button_accept_and_save),
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
            Text(stringResource(R.string.button_try_again))
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
            text = stringResource(R.string.text_pitch_hz, currentPitch.roundToInt()),
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
    readyText: String = stringResource(R.string.text_ready),
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
                progress = { progress },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.text_collecting_samples),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
