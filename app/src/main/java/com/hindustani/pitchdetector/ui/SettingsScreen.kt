package com.hindustani.pitchdetector.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hindustani.pitchdetector.ui.components.HelpTooltip
import com.hindustani.pitchdetector.viewmodel.PitchViewModel
import kotlin.math.roundToInt

/**
 * Settings screen for configuring pitch detection parameters
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: PitchViewModel,
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Tolerance Slider with help
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tolerance: ±${settings.toleranceCents.roundToInt()} cents",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                HelpTooltip(
                    text = "Tolerance level:\n\n" +
                           "• Expert (±5-8¢): For advanced musicians\n" +
                           "• Intermediate (±10-15¢): For regular practice\n" +
                           "• Beginner (±20-30¢): For those starting out"
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = settings.toleranceCents.toFloat(),
                onValueChange = { viewModel.updateTolerance(it.toDouble()) },
                valueRange = 5f..30f,
                steps = 24,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Expert (±5¢)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Beginner (±30¢)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tuning System with help
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tuning System",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                HelpTooltip(
                    text = "Tuning system:\n\n" +
                    "12 Notes (JI ratios): Standard swars with komal and teevra Ma variants.\n" +
                    "S, r, R, g, m, M, P, d, D, n, N\n" +
                    "Recommended for general practice.\n\n" +
                    "22-Shruti: Shrutis with microtonal variations (ati-komal, komal, teevra & ati-teevra Ma).\n" +
                    "S, r1, r2, R1, R2, g1, g2, G1, G2, m1, m2, M1, M2, P, d1, d2, D1, D2, n1, n2, N1, N2\n" +
                    "Recommended for raags with stricter requirements of swars."
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = !settings.use22Shruti,
                    onClick = { viewModel.updateTuningSystem(false) }
                )
                Text(
                    text = "12 Notes (Just Intonation)",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = settings.use22Shruti,
                    onClick = { viewModel.updateTuningSystem(true) }
                )
                Text(
                    text = "22 Shruti System",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tanpura Volume Slider with tips
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tanpura Volume: ${(settings.tanpuraVolume * 100).roundToInt()}%",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                HelpTooltip(
                    text = "Tanpura volume:\n\n" +
                        "Use lower volume when not using headphones to avoid interference."
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = settings.tanpuraVolume,
                onValueChange = { viewModel.updateTanpuraVolume(it) },
                valueRange = 0f..1f,
                steps = 19,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Silent (0%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Full (100%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Swar notation reference
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Swar Notation",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Capital letters indicate shuddha variants (except Ma)\n" +
                               "• Small letters indicate komal variants (except Ma)\n" +
                               "• m indicates shuddha Ma\n" +
                               "• M indicates teevra Ma\n" +
                               "• . before the note indicates mandra saptak (lower octave)\n" +
                               "• ' after the note indicates taar saptak (higher octave)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
