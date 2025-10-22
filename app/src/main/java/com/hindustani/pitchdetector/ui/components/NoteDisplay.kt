package com.hindustani.pitchdetector.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

/**
 * Displays the current detected note
 */
@Composable
fun NoteDisplay(
    swar: String,
    modifier: Modifier = Modifier,
) {
    // Compact note display - just the swar text
    Text(
        text = swar,
        style = MaterialTheme.typography.displayLarge,
        fontSize = 64.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}
