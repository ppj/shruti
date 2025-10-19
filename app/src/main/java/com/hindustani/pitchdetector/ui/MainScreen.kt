package com.hindustani.pitchdetector.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hindustani.pitchdetector.R
import com.hindustani.pitchdetector.music.HindustaniNoteConverter
import com.hindustani.pitchdetector.ui.components.HelpTooltip
import com.hindustani.pitchdetector.ui.components.NoteDisplay
import com.hindustani.pitchdetector.ui.components.PianoKeyboardSelector
import com.hindustani.pitchdetector.ui.components.PitchBar
import com.hindustani.pitchdetector.viewmodel.PitchViewModel
import kotlin.math.roundToInt

/**
 * Main screen for pitch detection
 */
@Composable
fun MainScreen(
    viewModel: PitchViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToFindSa: () -> Unit,
    onNavigateToTraining: (Int) -> Unit,
) {
    val pitchState by viewModel.pitchState.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val isTanpuraPlaying by viewModel.isTanpuraPlaying.collectAsState()
    val settings by viewModel.settings.collectAsState()

    // Dropdown state for tanpura string 1 selector
    var showTanpuraDropdown by remember { mutableStateOf(false) }
    val tanpuraAvailableNotes = remember { viewModel.getTanpuraAvailableNotes() }

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
                Text(
                    text = stringResource(R.string.text_sa_label, pitchState.saNote),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                HelpTooltip(
                    text = stringResource(R.string.tooltip_select_sa),
                    actionLabel = stringResource(R.string.button_find_my_sa),
                    onActionClick = onNavigateToFindSa,
                )
            }

            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = stringResource(R.string.content_description_settings),
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        PianoKeyboardSelector(
            selectedSa = pitchState.saNote,
            onSaSelected = { note -> viewModel.updateSa(note) },
        )

        Spacer(modifier = Modifier.height(16.dp))

        val currentNote = pitchState.currentNote
        val displaySwara =
            if (currentNote != null) {
                when (currentNote.octave) {
                    HindustaniNoteConverter.Octave.MANDRA -> ".${currentNote.swara}"
                    HindustaniNoteConverter.Octave.MADHYA -> currentNote.swara
                    HindustaniNoteConverter.Octave.TAAR -> "${currentNote.swara}'"
                }
            } else {
                stringResource(R.string.text_no_note)
            }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            NoteDisplay(
                swara = displaySwara,
            )
            Spacer(modifier = Modifier.width(8.dp))
            HelpTooltip(
                text = stringResource(R.string.tooltip_detected_swara),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        PitchBar(
            centsDeviation = pitchState.currentNote?.centsDeviation ?: 0.0,
            tolerance = pitchState.toleranceCents,
            isPerfect = pitchState.currentNote?.isPerfect ?: false,
            isFlat = pitchState.currentNote?.isFlat ?: false,
            isSharp = pitchState.currentNote?.isSharp ?: false,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        if (isTanpuraPlaying) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                ),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_tanpura),
                    contentDescription = stringResource(R.string.content_description_tanpura),
                    modifier =
                        Modifier
                            .size(60.dp)
                            .padding(end = 8.dp),
                )

                Box(
                    modifier = Modifier.weight(1f),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .clickable {
                                        showTanpuraDropdown = true
                                    }
                                    .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text =
                                    stringResource(
                                        R.string.text_tanpura_string_1,
                                        settings.tanpuraString1,
                                    ),
                                style = MaterialTheme.typography.bodyMedium,
                                color =
                                    if (isTanpuraPlaying) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription =
                                    stringResource(R.string.content_description_select_string_1),
                                tint =
                                    if (isTanpuraPlaying) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        HelpTooltip(
                            text = stringResource(R.string.tooltip_tanpura_tuning),
                        )
                    }

                    DropdownMenu(
                        expanded = showTanpuraDropdown,
                        onDismissRequest = { showTanpuraDropdown = false },
                    ) {
                        tanpuraAvailableNotes.forEach { note ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = note,
                                        style =
                                            if (note == settings.tanpuraString1) {
                                                MaterialTheme.typography.bodyLarge.copy(
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                            } else {
                                                MaterialTheme.typography.bodyLarge
                                            },
                                    )
                                },
                                onClick = {
                                    viewModel.updateTanpuraString1(note)
                                    showTanpuraDropdown = false
                                },
                            )
                        }
                    }
                }

                Switch(
                    checked = isTanpuraPlaying,
                    onCheckedChange = { viewModel.toggleTanpura() },
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { viewModel.toggleRecording() },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
        ) {
            Text(
                text =
                    if (isRecording) {
                        stringResource(R.string.button_stop)
                    } else {
                        stringResource(R.string.button_listen)
                    },
                style = MaterialTheme.typography.titleLarge,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Training buttons
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = { onNavigateToTraining(1) },
                modifier =
                    Modifier
                        .weight(1f)
                        .height(48.dp),
            ) {
                Text(
                    text = stringResource(R.string.button_training_level_1),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            OutlinedButton(
                onClick = { onNavigateToTraining(2) },
                modifier =
                    Modifier
                        .weight(1f)
                        .height(48.dp),
            ) {
                Text(
                    text = stringResource(R.string.button_training_level_2),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        // Confidence indicator (subtle) - always reserve space to prevent button movement
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(if (isRecording) 1f else 0f),
        ) {
            Text(
                text =
                    stringResource(
                        R.string.text_confidence,
                        (pitchState.confidence * 100).roundToInt(),
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
            )
            Spacer(modifier = Modifier.width(4.dp))
            HelpTooltip(
                text = stringResource(R.string.tooltip_confidence_level),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
