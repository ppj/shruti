package com.hindustani.pitchdetector.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hindustani.pitchdetector.R
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
    onNavigateBack: () -> Unit,
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_description_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text =
                        stringResource(
                            R.string.text_tolerance,
                            settings.toleranceCents.roundToInt(),
                        ),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.width(8.dp))
                HelpTooltip(
                    text =
                        "Tolerance level:\n\n" +
                            "• Expert (±5-8¢): For advanced musicians\n" +
                            "• Intermediate (±10-15¢): For regular practice\n" +
                            "• Beginner (±20-30¢): For those starting out",
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = settings.toleranceCents.toFloat(),
                onValueChange = { viewModel.updateTolerance(it.toDouble()) },
                valueRange = 5f..30f,
                steps = 24,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    stringResource(R.string.text_expert_tolerance),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(R.string.text_beginner_tolerance),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.text_tuning_system),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.width(8.dp))
                HelpTooltip(
                    text =
                        "Tuning system:\n\n" +
                            "12 Notes (JI ratios): Standard swars with komal and teevra Ma variants.\n" +
                            "S, r, R, g, m, M, P, d, D, n, N\n" +
                            "Recommended for general practice.\n\n" +
                            "22-Shruti: Shrutis with microtonal variations (ati-komal, komal, teevra & ati-teevra Ma).\n" +
                            "S, r1, r2, R1, R2, g1, g2, G1, G2, m1, m2, M1, M2, P, d1, d2, D1, D2, n1, n2, N1, N2\n" +
                            "Recommended for raags with stricter requirements of swars.",
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                RadioButton(
                    selected = !settings.use22Shruti,
                    onClick = { viewModel.updateTuningSystem(false) },
                )
                Text(
                    text = stringResource(R.string.text_12_notes_ji),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                RadioButton(
                    selected = settings.use22Shruti,
                    onClick = { viewModel.updateTuningSystem(true) },
                )
                Text(
                    text = stringResource(R.string.text_22_shruti_system),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text =
                        stringResource(
                            R.string.text_tanpura_volume,
                            (settings.tanpuraVolume * 100).roundToInt(),
                        ),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.width(8.dp))
                HelpTooltip(
                    text =
                        "Tanpura volume:\n\n" +
                            "Use lower volume when not using headphones to avoid interference.",
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = settings.tanpuraVolume,
                onValueChange = { viewModel.updateTanpuraVolume(it) },
                valueRange = 0f..1f,
                steps = 19,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    stringResource(R.string.text_silent_volume),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(R.string.text_full_volume),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.title_swar_notation),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.text_swar_notation_details),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}
