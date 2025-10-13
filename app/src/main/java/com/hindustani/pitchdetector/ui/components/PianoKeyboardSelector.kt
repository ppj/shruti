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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hindustani.pitchdetector.ui.theme.WarmGold

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
    val whiteKeys = listOf("A2", "B2", "C3", "D3", "E3", "F3", "G3", "A3")

    // Black keys centered between neighboring white keys
    // Grouping pattern comes from gaps where no black keys exist (B-C and E-F)

    val blackKeysWithPositions = listOf(
        1.0f to "A#2",
        3.0f to "C#3",
        4.0f to "D#3",
        6.0f to "F#3",
        7.0f to "G#3"
    )

    val edgeBlackKeys = listOf(
        0.0f to "G#2",
        8.0f to "A#3"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DummyWhiteKey(modifier = Modifier.weight(0.5f))

            whiteKeys.forEach { note ->
                WhiteKey(
                    note = note,
                    isSelected = note == selectedSa,
                    onClick = { onSaSelected(note) },
                    modifier = Modifier.weight(1f)
                )
            }

            DummyWhiteKey(modifier = Modifier.weight(0.5f))
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val totalKeys = 9f
            val whiteKeyWidth = maxWidth / totalKeys
            val blackKeyWidth = whiteKeyWidth * 2f / 3f
            val blackKeyHalfWidth = blackKeyWidth / 2

            blackKeysWithPositions.forEach { (position, note) ->
                Box(
                    modifier = Modifier
                        .offset(x = whiteKeyWidth * (0.5f + position) - blackKeyHalfWidth)
                        .align(Alignment.TopStart)
                ) {
                    BlackKey(
                        note = note,
                        isSelected = note == selectedSa,
                        onClick = { onSaSelected(note) },
                        width = blackKeyWidth
                    )
                }
            }

            edgeBlackKeys.forEach { (position, note) ->
                Box(
                    modifier = Modifier
                        .offset(x = whiteKeyWidth * (0.5f + position) - blackKeyHalfWidth)
                        .align(Alignment.TopStart)
                ) {
                    BlackKey(
                        note = note,
                        isSelected = note == selectedSa,
                        onClick = { onSaSelected(note) },
                        width = blackKeyWidth
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
                    WarmGold
                else
                    Color.White
            )
            .border(
                width = 0.5.dp,
                color = if (isSelected)
                    WarmGold.copy(alpha = 0.8f)
                else
                    Color(0xFFCCCCCC),
                shape = RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = note,
            fontSize = 14.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = Color.Black,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier
                .padding(bottom = 6.dp)
                .graphicsLayer(rotationZ = 90f)
        )
    }
}

@Composable
private fun DummyWhiteKey(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 0.5.dp)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp)
            )
            .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
            .background(Color.White)
            .border(
                width = 0.5.dp,
                color = Color.Gray,  // Grey borders
                shape = RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp)
            )
    )
}

@Composable
private fun BlackKey(
    note: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    width: Dp
) {
    Box(
        modifier = Modifier
            .width(width)  // Dynamic width: 2/3 of white key width
            .height(54.dp)
            .shadow(
                elevation = if (isSelected) 1.dp else 3.dp,
                shape = RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp)
            )
            .clip(RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
            .background(
                if (isSelected)
                    WarmGold
                else
                    Color(0xFF2C2C2C)
            )
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 6.dp)
                .graphicsLayer(clip = false) // Don't clip rotated text
        ) {
            Text(
                text = note,
                fontSize = 12.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = if (isSelected)
                    Color.Black
                else
                    Color.White,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.graphicsLayer(rotationZ = 90f)
            )
        }
    }
}
