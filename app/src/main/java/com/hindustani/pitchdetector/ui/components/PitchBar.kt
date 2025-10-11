package com.hindustani.pitchdetector.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hindustani.pitchdetector.ui.theme.PitchFlat
import com.hindustani.pitchdetector.ui.theme.PitchPerfect
import com.hindustani.pitchdetector.ui.theme.PitchSharp
import kotlin.math.roundToInt

/**
 * Horizontal pitch accuracy bar with emoji feedback
 * Displays pitch deviation with smooth animations and visual feedback
 */
@Composable
fun PitchBar(
    centsDeviation: Double,
    tolerance: Double,
    isPerfect: Boolean,
    isFlat: Boolean,
    isSharp: Boolean,
    modifier: Modifier = Modifier
) {
    // Animate the indicator position for smooth movement
    val targetPosition = (centsDeviation / 50.0).coerceIn(-1.0, 1.0).toFloat()
    val animatedPosition by animateFloatAsState(
        targetValue = targetPosition,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "pitch_position"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Cents deviation text
        val indicatorColor = when {
            isPerfect -> PitchPerfect  // Warm green for perfect pitch
            isFlat -> PitchFlat        // Cool blue for flat/low pitch
            isSharp -> PitchSharp      // Warm red for sharp/high pitch
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        Text(
            text = "${centsDeviation.roundToInt()} cents",
            style = MaterialTheme.typography.titleLarge,
            color = indicatorColor,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal pitch bar
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 16.dp)
        ) {
            val barWidth = size.width
            val barHeight = 20.dp.toPx()
            val centerX = barWidth / 2
            val centerY = size.height / 2

            // Draw background gradient bar
            val gradientBrush = Brush.horizontalGradient(
                colors = listOf(
                    PitchFlat,     // Cool blue for flat (left)
                    PitchPerfect,  // Warm green for perfect (center)
                    PitchSharp     // Warm red for sharp (right)
                ),
                startX = 0f,
                endX = barWidth
            )

            drawRoundRect(
                brush = gradientBrush,
                topLeft = Offset(0f, centerY - barHeight / 2),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barHeight / 2, barHeight / 2),
                alpha = 0.3f
            )

            // Draw tolerance zone (green band in center)
            val toleranceWidth = (tolerance / 50.0 * barWidth / 2).toFloat()
            drawRoundRect(
                color = PitchPerfect,
                topLeft = Offset(centerX - toleranceWidth, centerY - barHeight / 2),
                size = Size(toleranceWidth * 2, barHeight),
                cornerRadius = CornerRadius(barHeight / 2, barHeight / 2),
                alpha = 0.4f
            )

            // Draw center line (dynamic color based on theme)
            drawLine(
                color = Color.Black.copy(alpha = 0.6f),
                start = Offset(centerX, centerY - barHeight / 2 - 10.dp.toPx()),
                end = Offset(centerX, centerY + barHeight / 2 + 10.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Draw tick marks for -25¢ and +25¢
            val tickOffset = barWidth / 4
            listOf(centerX - tickOffset, centerX + tickOffset).forEach { x ->
                drawLine(
                    color = Color.Gray.copy(alpha = 0.6f),
                    start = Offset(x, centerY - barHeight / 2 - 5.dp.toPx()),
                    end = Offset(x, centerY + barHeight / 2 + 5.dp.toPx()),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Draw animated indicator dot
            val indicatorX = centerX + animatedPosition * barWidth / 2

            // Outer glow
            drawCircle(
                color = indicatorColor.copy(alpha = 0.3f),
                radius = 20.dp.toPx(),
                center = Offset(indicatorX, centerY)
            )

            // Main indicator circle with border
            drawCircle(
                color = Color.White,
                radius = 14.dp.toPx(),
                center = Offset(indicatorX, centerY)
            )

            drawCircle(
                color = indicatorColor,
                radius = 12.dp.toPx(),
                center = Offset(indicatorX, centerY)
            )

            // Inner highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = 5.dp.toPx(),
                center = Offset(indicatorX - 3.dp.toPx(), centerY - 3.dp.toPx())
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Emoji status indicator
        val (emoji, statusText) = when {
            isPerfect -> "🟢" to "Perfect!"
            isFlat -> "➡️" to "Sharpen"
            isSharp -> "⬅️" to "Flatten"
            else -> "—" to "—"
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 24.sp
            )
            if (statusText != "—") {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium,
                    color = indicatorColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
