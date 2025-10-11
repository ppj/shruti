package com.hindustani.pitchdetector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Displays the current detected note
 */
@Composable
fun NoteDisplay(
    swara: String,
    modifier: Modifier = Modifier
) {
    // Compact note display - just the swara text
    Text(
        text = swara,
        style = MaterialTheme.typography.displayLarge,
        fontSize = 64.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}
