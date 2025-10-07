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
import com.hindustani.pitchdetector.music.SaParser
import com.hindustani.pitchdetector.ui.components.NoteDisplay
import com.hindustani.pitchdetector.ui.components.PitchIndicator
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

    // Dropdown state for Sa selector
    var showSaDropdown by remember { mutableStateOf(false) }
    val saOptions = remember { SaParser.getSaOptionsInRange() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with Sa dropdown selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sa dropdown selector
            Box {
                Row(
                    modifier = Modifier
                        .clickable {
                            // Stop recording when opening dropdown
                            if (isRecording) {
                                viewModel.toggleRecording()
                            }
                            showSaDropdown = true
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sa: ${pitchState.saNote}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Sa",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = showSaDropdown,
                    onDismissRequest = { showSaDropdown = false }
                ) {
                    saOptions.forEach { (note, frequency) ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = note,
                                        style = if (note == pitchState.saNote)
                                            MaterialTheme.typography.bodyLarge.copy(
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        else
                                            MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "${frequency.roundToInt()} Hz",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }
                            },
                            onClick = {
                                viewModel.updateSa(note)
                                showSaDropdown = false
                            }
                        )
                    }
                }
            }

            IconButton(onClick = onNavigateToSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

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
            swara = displaySwara,
            centsDeviation = currentNote?.centsDeviation ?: 0.0
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Pitch accuracy indicator
        PitchIndicator(
            centsDeviation = pitchState.currentNote?.centsDeviation ?: 0.0,
            tolerance = pitchState.toleranceCents,
            isPerfect = pitchState.currentNote?.isPerfect ?: false,
            isFlat = pitchState.currentNote?.isFlat ?: false,
            isSharp = pitchState.currentNote?.isSharp ?: false
        )

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

        Spacer(modifier = Modifier.height(16.dp))
    }
}
