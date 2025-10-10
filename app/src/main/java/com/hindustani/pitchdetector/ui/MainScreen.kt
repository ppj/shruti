package com.hindustani.pitchdetector.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hindustani.pitchdetector.music.HindustaniNoteConverter
import com.hindustani.pitchdetector.ui.components.NoteDisplay
import com.hindustani.pitchdetector.ui.components.PitchBar
import com.hindustani.pitchdetector.ui.components.PianoKeyboardSelector
import com.hindustani.pitchdetector.viewmodel.PitchViewModel
import kotlin.math.roundToInt

/**
 * Main screen for pitch detection
 */
@Composable
fun MainScreen(
    viewModel: PitchViewModel,
    onNavigateToSettings: () -> Unit
) {
    val pitchState by viewModel.pitchState.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val isTanpuraPlaying by viewModel.isTanpuraPlaying.collectAsState()
    val settings by viewModel.settings.collectAsState()

    // Dropdown state for tanpura string 1 selector
    var showTanpuraDropdown by remember { mutableStateOf(false) }
    val tanpuraAvailableNotes = remember { viewModel.getTanpuraAvailableNotes() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with Settings button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sa: ${pitchState.saNote}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )

            IconButton(onClick = onNavigateToSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Piano keyboard Sa selector
        PianoKeyboardSelector(
            selectedSa = pitchState.saNote,
            onSaSelected = { note -> viewModel.updateSa(note) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Current note display
        val currentNote = pitchState.currentNote
        val displaySwara = if (currentNote != null) {
            // Format swara with octave notation
            when (currentNote.octave) {
                HindustaniNoteConverter.Octave.MANDRA -> ".${currentNote.swara}"
                HindustaniNoteConverter.Octave.MADHYA -> currentNote.swara
                HindustaniNoteConverter.Octave.TAAR -> "${currentNote.swara}'"
            }
        } else {
            "—"
        }

        NoteDisplay(
            swara = displaySwara
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Pitch accuracy bar
        PitchBar(
            centsDeviation = pitchState.currentNote?.centsDeviation ?: 0.0,
            tolerance = pitchState.toleranceCents,
            isPerfect = pitchState.currentNote?.isPerfect ?: false,
            isFlat = pitchState.currentNote?.isFlat ?: false,
            isSharp = pitchState.currentNote?.isSharp ?: false
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tanpura controls
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isTanpuraPlaying)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tanpura label and string 1 selector
                Column {
                    Text(
                        text = "Tanpura",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isTanpuraPlaying)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // String 1 selector
                    Box {
                        Row(
                            modifier = Modifier
                                .clickable(enabled = !isTanpuraPlaying) {
                                    showTanpuraDropdown = true
                                }
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "String 1: ${settings.tanpuraString1}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isTanpuraPlaying)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select string 1 note",
                                tint = if (isTanpuraPlaying)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showTanpuraDropdown,
                            onDismissRequest = { showTanpuraDropdown = false }
                        ) {
                            tanpuraAvailableNotes.forEach { note ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = note,
                                            style = if (note == settings.tanpuraString1)
                                                MaterialTheme.typography.bodyLarge.copy(
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            else
                                                MaterialTheme.typography.bodyLarge
                                        )
                                    },
                                    onClick = {
                                        viewModel.updateTanpuraString1(note)
                                        showTanpuraDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Toggle button
                Switch(
                    checked = isTanpuraPlaying,
                    onCheckedChange = { viewModel.toggleTanpura() }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Recording controls
        Button(
            onClick = { viewModel.toggleRecording() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRecording) Color(0xFFF44336) else Color(0xFF4CAF50)
            )
        ) {
            Text(
                text = if (isRecording) "Stop" else "Start",
                style = MaterialTheme.typography.titleLarge
            )
        }

        // Confidence indicator (subtle)
        if (isRecording) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Confidence: ${(pitchState.confidence * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
