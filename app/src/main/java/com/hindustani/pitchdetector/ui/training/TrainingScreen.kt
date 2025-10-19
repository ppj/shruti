package com.hindustani.pitchdetector.ui.training

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hindustani.pitchdetector.viewmodel.TrainingViewModel

/**
 * Training screen for ear training exercises.
 * TODO: Implement full UI in Phase 5
 */
@Composable
fun TrainingScreen(viewModel: TrainingViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Training Mode - Coming Soon",
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}
