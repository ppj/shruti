package com.hindustani.pitchdetector.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hindustani.pitchdetector.R

/**
 * Reusable tanpura card component with toggle switch
 *
 * @param isTanpuraPlaying Whether tanpura is currently playing
 * @param onToggleTanpura Callback when tanpura switch is toggled
 * @param showString1Selector Whether to show the String 1 dropdown selector
 * @param string1Options Available String 1 notes for dropdown (required if showString1Selector is true)
 * @param selectedString1 Currently selected String 1 note (required if showString1Selector is true)
 * @param onString1Selected Callback when String 1 is selected (required if showString1Selector is true)
 * @param modifier Optional modifier
 */
@Composable
fun TanpuraCard(
    isTanpuraPlaying: Boolean,
    onToggleTanpura: () -> Unit,
    showString1Selector: Boolean = false,
    string1Options: List<String> = emptyList(),
    selectedString1: String = "",
    onString1Selected: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showDropdown by remember { mutableStateOf(false) }

    Card(
        modifier =
            modifier
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

            if (showString1Selector) {
                // Main screen mode: show String 1 selector with dropdown
                Box(
                    modifier = Modifier.weight(1f),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .clickable { showDropdown = true }
                                    .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.text_tanpura_string_1, selectedString1),
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
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false },
                    ) {
                        string1Options.forEach { note ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = note,
                                        style =
                                            if (note == selectedString1) {
                                                MaterialTheme.typography.bodyLarge.copy(
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                            } else {
                                                MaterialTheme.typography.bodyLarge
                                            },
                                    )
                                },
                                onClick = {
                                    onString1Selected(note)
                                    showDropdown = false
                                },
                            )
                        }
                    }
                }
            } else {
                // Training mode: no text display, just spacing
                Spacer(modifier = Modifier.weight(1f))
            }

            Switch(
                checked = isTanpuraPlaying,
                onCheckedChange = { onToggleTanpura() },
            )
        }
    }
}
