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
import com.hindustani.pitchdetector.ui.components.LabelledSlider
import com.hindustani.pitchdetector.ui.components.SettingsSectionHeader
import com.hindustani.pitchdetector.viewmodel.PitchViewModel
import kotlin.math.roundToInt

private object SettingsConstants {
    const val TOLERANCE_MIN = 5f
    const val TOLERANCE_MAX = 30f
    const val TOLERANCE_STEPS = 24

    const val VOLUME_MIN = 0f
    const val VOLUME_MAX = 1f
    const val VOLUME_STEPS = 19
}

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
            SettingsSectionHeader(
                title =
                    stringResource(
                        R.string.text_tolerance,
                        settings.toleranceCents.roundToInt(),
                    ),
                tooltipText = stringResource(R.string.tooltip_tolerance_level),
            )

            LabelledSlider(
                value = settings.toleranceCents.toFloat(),
                onValueChange = { viewModel.updateTolerance(it.toDouble()) },
                valueRange = SettingsConstants.TOLERANCE_MIN..SettingsConstants.TOLERANCE_MAX,
                steps = SettingsConstants.TOLERANCE_STEPS,
                startLabel = stringResource(R.string.text_expert_tolerance),
                endLabel = stringResource(R.string.text_beginner_tolerance),
            )

            Spacer(modifier = Modifier.height(32.dp))

            SettingsSectionHeader(
                title = stringResource(R.string.text_tuning_system),
                tooltipText = stringResource(R.string.tooltip_tuning_system),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                RadioButton(
                    selected = !settings.use22Shruti,
                    onClick = { viewModel.updateTuningSystem(false) },
                )
                Text(
                    text = stringResource(R.string.text_12_swars_ji),
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

            SettingsSectionHeader(
                title =
                    stringResource(
                        R.string.text_tanpura_volume,
                        (settings.tanpuraVolume * 100).roundToInt(),
                    ),
                tooltipText = stringResource(R.string.tooltip_tanpura_volume),
            )

            LabelledSlider(
                value = settings.tanpuraVolume,
                onValueChange = { viewModel.updateTanpuraVolume(it) },
                valueRange = SettingsConstants.VOLUME_MIN..SettingsConstants.VOLUME_MAX,
                steps = SettingsConstants.VOLUME_STEPS,
                startLabel = stringResource(R.string.text_silent_volume),
                endLabel = stringResource(R.string.text_full_volume),
            )

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
