package com.hindustani.pitchdetector.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hindustani.pitchdetector.R

/**
 * Info icon with tooltip that appears on tap
 * Provides contextual help without navigation
 */
@Composable
fun HelpTooltip(
    text: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    var showDialog by remember { mutableStateOf(false) }

    IconButton(
        onClick = { showDialog = true },
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = stringResource(R.string.content_description_tip),
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.tooltip_title)) },
            text = { Text(text) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.tooltip_got_it))
                }
            },
            dismissButton =
                if (actionLabel != null && onActionClick != null) {
                    {
                        TextButton(
                            onClick = {
                                showDialog = false
                                onActionClick()
                            },
                        ) {
                            Text(actionLabel)
                        }
                    }
                } else {
                    null
                },
        )
    }
}
