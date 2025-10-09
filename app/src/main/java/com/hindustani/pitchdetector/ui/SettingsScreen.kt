package com.hindustani.pitchdetector.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hindustani.pitchdetector.music.SaParser
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
    var isSaDropdownExpanded by remember { mutableStateOf(false) }

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
                .padding(16.dp)
        ) {
            // Default Sa Note
            Text(
                text = "Default Sa (Tonic)",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = isSaDropdownExpanded,
                onExpandedChange = { isSaDropdownExpanded = !isSaDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = settings.defaultSaNote,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Default Sa") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSaDropdownExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor() // Anchor the dropdown menu
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = isSaDropdownExpanded,
                    onDismissRequest = { isSaDropdownExpanded = false }
                ) {
                    SaParser.getSaOptions().forEach { saNote ->
                        DropdownMenuItem(
                            text = { Text(saNote) },
                            onClick = {
                                viewModel.updateDefaultSa(saNote)
                                isSaDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))


            // Tolerance Slider
            Text(
                text = "Tolerance: ±${settings.toleranceCents.roundToInt()} cents",
                style = MaterialTheme.typography.titleMedium
            )
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

            // Tuning System
            Text(
                text = "Tuning System",
                style = MaterialTheme.typography.titleMedium
            )
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

            // Explanation text
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "About Tolerance",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Expert (±5-8¢): For advanced musicians\n" + 
                                "• Intermediate (±10-15¢): For regular practice\n" + 
                                "• Beginner (±20-30¢): For those starting out",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}