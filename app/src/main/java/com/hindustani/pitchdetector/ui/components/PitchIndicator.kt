package com.hindustani.pitchdetector.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Visual pitch accuracy indicator with needle display
 */
@Composable
fun PitchIndicator(
    centsDeviation: Double,
    tolerance: Double,
    isPerfect: Boolean,
    isFlat: Boolean,
    isSharp: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Determine color based on accuracy
        val indicatorColor = when {
            isPerfect -> Color(0xFF4CAF50)  // Green
            isFlat -> Color(0xFF2196F3)     // Blue
            isSharp -> Color(0xFFF44336)    // Red
            else -> Color.Gray
        }

        // Needle-style meter
        Canvas(modifier = Modifier.size(300.dp, 120.dp)) {
            val centerX = size.width / 2
            val centerY = size.height - 20.dp.toPx()
            val radius = size.width / 2 - 20.dp.toPx()

            // Draw background arc
            drawArc(
                color = Color.LightGray,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw tolerance zone (green zone)
            val toleranceAngle = (tolerance / 50.0 * 90.0).toFloat()
            drawArc(
                color = Color(0xFF4CAF50).copy(alpha = 0.3f),
                startAngle = 180f + 90f - toleranceAngle,
                sweepAngle = toleranceAngle * 2,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw center mark
            val centerMarkAngle = Math.toRadians(270.0)
            val centerMarkStart = Offset(
                x = centerX + (radius - 15.dp.toPx()) * cos(centerMarkAngle).toFloat(),
                y = centerY + (radius - 15.dp.toPx()) * sin(centerMarkAngle).toFloat()
            )
            val centerMarkEnd = Offset(
                x = centerX + (radius + 15.dp.toPx()) * cos(centerMarkAngle).toFloat(),
                y = centerY + (radius + 15.dp.toPx()) * sin(centerMarkAngle).toFloat()
            )
            drawLine(
                color = Color.Black,
                start = centerMarkStart,
                end = centerMarkEnd,
                strokeWidth = 3.dp.toPx()
            )

            // Draw tick marks
            for (i in -2..2) {
                if (i != 0) {
                    val tickAngle = Math.toRadians(270.0 + i * 22.5)
                    val tickStart = Offset(
                        x = centerX + (radius - 10.dp.toPx()) * cos(tickAngle).toFloat(),
                        y = centerY + (radius - 10.dp.toPx()) * sin(tickAngle).toFloat()
                    )
                    val tickEnd = Offset(
                        x = centerX + (radius + 10.dp.toPx()) * cos(tickAngle).toFloat(),
                        y = centerY + (radius + 10.dp.toPx()) * sin(tickAngle).toFloat()
                    )
                    drawLine(
                        color = Color.Gray,
                        start = tickStart,
                        end = tickEnd,
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            // Draw needle
            val needleAngle = (centsDeviation / 50.0 * 90.0).coerceIn(-90.0, 90.0)
            val angleRad = Math.toRadians(270.0 + needleAngle)
            val needleLength = radius - 10.dp.toPx()

            val needleEnd = Offset(
                x = centerX + needleLength * cos(angleRad).toFloat(),
                y = centerY + needleLength * sin(angleRad).toFloat()
            )

            // Draw needle shadow
            drawLine(
                color = Color.Black.copy(alpha = 0.2f),
                start = Offset(centerX + 2.dp.toPx(), centerY + 2.dp.toPx()),
                end = Offset(needleEnd.x + 2.dp.toPx(), needleEnd.y + 2.dp.toPx()),
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Draw needle
            drawLine(
                color = indicatorColor,
                start = Offset(centerX, centerY),
                end = needleEnd,
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Draw center dot
            drawCircle(
                color = indicatorColor,
                radius = 8.dp.toPx(),
                center = Offset(centerX, centerY)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cents deviation text
        Text(
            text = "${centsDeviation.roundToInt()} cents",
            style = MaterialTheme.typography.titleLarge,
            color = indicatorColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Status text
        val statusText = when {
            isPerfect -> "Perfect!"
            isFlat -> "Flat ↓"
            isSharp -> "Sharp ↑"
            else -> "—"
        }

        Text(
            text = statusText,
            style = MaterialTheme.typography.headlineLarge,
            color = indicatorColor
        )
    }
}
