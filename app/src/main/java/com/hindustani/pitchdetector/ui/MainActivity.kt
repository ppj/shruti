package com.hindustani.pitchdetector.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hindustani.pitchdetector.constants.AppRoutes
import com.hindustani.pitchdetector.ui.findsa.FindSaScreen
import com.hindustani.pitchdetector.ui.theme.ShrutiTheme
import com.hindustani.pitchdetector.ui.training.TrainingMenuScreen
import com.hindustani.pitchdetector.ui.training.TrainingScreen
import com.hindustani.pitchdetector.viewmodel.FindSaViewModel
import com.hindustani.pitchdetector.viewmodel.PitchViewModel
import com.hindustani.pitchdetector.viewmodel.TrainingViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: PitchViewModel by viewModels()
    private val findSaViewModel: FindSaViewModel by viewModels()
    private var hasPermission by mutableStateOf(false)

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            hasPermission = isGranted
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED

        setContent {
            ShrutiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    if (hasPermission) {
                        AppNavigation(viewModel, findSaViewModel)
                    } else {
                        PermissionScreen(
                            onRequestPermission = {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    viewModel: PitchViewModel,
    findSaViewModel: FindSaViewModel,
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppRoutes.MAIN) {
        composable(AppRoutes.MAIN) {
            MainScreen(
                viewModel = viewModel,
                onNavigateToSettings = {
                    navController.navigate(AppRoutes.SETTINGS)
                },
                onNavigateToFindSa = {
                    navController.navigate(AppRoutes.FIND_SA)
                },
                onNavigateToTraining = {
                    navController.navigate(AppRoutes.TRAINING_MENU)
                },
            )
        }
        composable(AppRoutes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }
        composable(AppRoutes.FIND_SA) {
            FindSaScreen(
                viewModel = findSaViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaSelected = { saNote ->
                    viewModel.updateSa(saNote)
                },
            )
        }
        composable(AppRoutes.TRAINING_MENU) {
            TrainingMenuScreen(
                navController = navController,
            )
        }
        composable(
            route = "${AppRoutes.TRAINING}/{${AppRoutes.NavArgs.LEVEL}}",
            arguments =
                listOf(
                    navArgument(AppRoutes.NavArgs.LEVEL) {
                        type = NavType.IntType
                    },
                ),
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getInt(AppRoutes.NavArgs.LEVEL) ?: 1
            val context = LocalContext.current
            val trainingViewModel: TrainingViewModel =
                viewModel(
                    factory = TrainingViewModel.provideFactory(level, viewModel, context),
                )
            TrainingScreen(
                navController = navController,
                viewModel = trainingViewModel,
            )
        }
    }
}

@Composable
fun PermissionScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Microphone Permission Required",
            style = MaterialTheme.typography.headlineLarge,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "This app needs access to your microphone to detect pitch and help you practice Hindustani classical music.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Grant Permission")
        }
    }
}
