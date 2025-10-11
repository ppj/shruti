package com.hindustani.pitchdetector.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Info icon with tooltip that appears on tap
 * Provides contextual help without navigation
 */
@Composable
fun HelpTooltip(
    text: String,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Icon(
        imageVector = Icons.Default.Info,
        contentDescription = "Tip",
        modifier = modifier
            .size(20.dp)
            .clickable { showDialog = true },
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Tip") },
            text = { Text(text) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}
