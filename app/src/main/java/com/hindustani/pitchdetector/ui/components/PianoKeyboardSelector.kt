package com.hindustani.pitchdetector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hindustani.pitchdetector.music.SaParser
import kotlin.math.roundToInt

/**
 * Piano keyboard selector for Sa (tonic) note selection
 * Displays a visual keyboard spanning G#2 to A#3 (15 notes)
 */
@Composable
fun PianoKeyboardSelector(
    selectedSa: String,
    onSaSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Define the keyboard layout
    // White keys: A2, B2, C3, D3, E3, F3, G3, A3
    // Black keys positioned between white keys
    val whiteKeys = listOf("A2", "B2", "C3", "D3", "E3", "F3", "G3", "A3")
    val blackKeys = mapOf(
        0.5f to "A#2",  // Between A2 and B2
        2.5f to "C#3",  // Between C3 and D3
        3.5f to "D#3",  // Between D3 and E3
        5.5f to "F#3",  // Between F3 and G3
        6.5f to "G#3",  // Between G3 and A3
    )

    // Special case: G#2 appears before A2, A#3 appears after A3
    val specialBlackKeys = mapOf(
        -0.7f to "G#2",  // Before A2
        7.7f to "A#3"    // After A3
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 8.dp)
    ) {
        // White keys layer
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            whiteKeys.forEach { note ->
                WhiteKey(
                    note = note,
                    isSelected = note == selectedSa,
                    onClick = { onSaSelected(note) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Black keys layer (positioned absolutely)
        val keyWidth = 40.dp  // Approximate width per white key

        // Regular black keys
        blackKeys.forEach { (position, note) ->
            Box(
                modifier = Modifier
                    .offset(x = keyWidth * position)
                    .align(Alignment.TopStart)
            ) {
                BlackKey(
                    note = note,
                    isSelected = note == selectedSa,
                    onClick = { onSaSelected(note) }
                )
            }
        }

        // Special black keys (G#2 and A#3)
        specialBlackKeys.forEach { (position, note) ->
            Box(
                modifier = Modifier
                    .offset(x = keyWidth * position + 8.dp)  // Adjust for padding
                    .align(Alignment.TopStart)
            ) {
                BlackKey(
                    note = note,
                    isSelected = note == selectedSa,
                    onClick = { onSaSelected(note) }
                )
            }
        }
    }
}

@Composable
private fun WhiteKey(
    note: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val frequency = SaParser.parseToFrequency(note)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 1.dp)
            .shadow(
                elevation = if (isSelected) 0.dp else 2.dp,
                shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
            )
            .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
            .background(
                if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    Color.White
            )
            .border(
                width = 1.dp,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    Color.Gray,
                shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = note,
                fontSize = 11.sp,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    Color.Black
            )
            if (frequency != null && isSelected) {
                Text(
                    text = "${frequency.roundToInt()}Hz",
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun BlackKey(
    note: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val frequency = SaParser.parseToFrequency(note)

    Box(
        modifier = Modifier
            .width(28.dp)
            .height(60.dp)
            .shadow(
                elevation = if (isSelected) 0.dp else 4.dp,
                shape = RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp)
            )
            .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
            .background(
                if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    Color(0xFF1C1C1C)
            )
            .border(
                width = 1.dp,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    Color.Black,
                shape = RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = note,
                fontSize = 9.sp,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    Color.White
            )
            if (frequency != null && isSelected) {
                Text(
                    text = "${frequency.roundToInt()}Hz",
                    fontSize = 7.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
        }
    }
}
