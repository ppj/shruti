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
 * Fits within screen width without scrolling
 */
@Composable
fun PianoKeyboardSelector(
    selectedSa: String,
    onSaSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // All 15 notes in order: G#2, A2, A#2, B2, C3, C#3, D3, D#3, E3, F3, F#3, G3, G#3, A3, A#3
    // White keys: A2, B2, C3, D3, E3, F3, G3, A3 (8 keys)
    // Black keys: G#2, A#2, C#3, D#3, F#3, G#3, A#3 (7 keys)

    val whiteKeys = listOf("A2", "B2", "C3", "D3", "E3", "F3", "G3", "A3")

    // Black keys with their positions relative to white keys
    // Position 0 = before first white key (A2)
    // Position 0.5 = between A2 and B2, etc.
    val blackKeysWithPositions = listOf(
        -0.5f to "G#2",   // Before A2
        0.65f to "A#2",   // Between A2 and B2
        2.65f to "C#3",   // Between C3 and D3
        3.65f to "D#3",   // Between D3 and E3
        5.65f to "F#3",   // Between F3 and G3
        6.65f to "G#3",   // Between G3 and A3
        8.0f to "A#3"     // After A3
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(horizontal = 4.dp)
    ) {
        // White keys layer - fill the width evenly
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

        // Black keys layer - positioned absolutely over white keys
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val whiteKeyWidth = maxWidth / 8  // 8 white keys total

            blackKeysWithPositions.forEach { (position, note) ->
                Box(
                    modifier = Modifier
                        .offset(x = whiteKeyWidth * position)
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
}

@Composable
private fun WhiteKey(
    note: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 0.5.dp)
            .shadow(
                elevation = if (isSelected) 0.dp else 1.dp,
                shape = RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp)
            )
            .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
            .background(
                if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    Color.White
            )
            .border(
                width = 0.5.dp,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    Color(0xFFCCCCCC),
                shape = RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = note,
            fontSize = 10.sp,
            color = if (isSelected)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                Color.Black,
            modifier = Modifier.padding(bottom = 6.dp)
        )
    }
}

@Composable
private fun BlackKey(
    note: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(24.dp)
            .height(54.dp)
            .shadow(
                elevation = if (isSelected) 1.dp else 3.dp,
                shape = RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp)
            )
            .clip(RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
            .background(
                if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    Color(0xFF2C2C2C)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = note,
            fontSize = 8.sp,
            color = if (isSelected)
                MaterialTheme.colorScheme.onPrimary
            else
                Color.White,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}
