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
                modifier = Modifier.padding(8.dp),
            ) {
                Text(
                    text = "Sa: ${pitchState.saNote}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                HelpTooltip(
                    text =
                        "Select your Sa (tonic/base note) on the keyboard.\n\n" +
                            "Not sure which Sa is right for you? Let us help you discover your ideal starting pitch!",
                    actionLabel = "Find My Sa",
                    onActionClick = onNavigateToFindSa,
                )
            }

            IconButton(onClick = onNavigateToSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
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
                "—"
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
                text =
                    "Detected swar/shruti (in listening mode):\n" +
                        "• Lower octave - .S, .N, .n, .D ...\n" +
                        "• Middle octave - S, r, R, g, G ...\n" +
                        "• Higher octave - S', r', R', g' ...",
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
                    contentDescription = "Tanpura",
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
                                text = "String 1: ${settings.tanpuraString1}",
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
                                contentDescription = "Select string 1 note",
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
                            text =
                                "String 1 tuning:\n\n" +
                                    "• P: Yaman, Bhoop, Bhairav etc. (most common)\n" +
                                    "• m: Malkauns, Lalit, Bageshree, etc.\n" +
                                    "• N: Marva, Pooriya, Sohni, etc.\n" +
                                    "• M: Very rare (to be removed soon)\n" +
                                    "• S: Very rare (to be removed soon)",
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
                text = if (isRecording) "Stop" else "Listen",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        // Confidence indicator (subtle) - always reserve space to prevent button movement
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(if (isRecording) 1f else 0f),
        ) {
            Text(
                text = "Confidence: ${(pitchState.confidence * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
            )
            Spacer(modifier = Modifier.width(4.dp))
            HelpTooltip(
                text =
                    "Confidence level based on audio signal quality:\n\n" +
                        "Higher confidence means more reliable pitch detection. " +
                        "A quiet environment and wired headphones with a decent mic will improve confidence level.",
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
