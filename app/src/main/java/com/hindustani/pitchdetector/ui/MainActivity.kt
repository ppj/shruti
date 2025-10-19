package com.hindustani.pitchdetector.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hindustani.pitchdetector.constants.AppRoutes
import com.hindustani.pitchdetector.ui.findsa.FindSaScreen
import com.hindustani.pitchdetector.ui.theme.ShrutiTheme
import com.hindustani.pitchdetector.ui.training.TrainingScreen
import com.hindustani.pitchdetector.viewmodel.FindSaViewModel
import com.hindustani.pitchdetector.viewmodel.PitchViewModel
import com.hindustani.pitchdetector.viewmodel.TrainingViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: PitchViewModel by viewModels()
    private val findSaViewModel: FindSaViewModel by viewModels()
    private val trainingViewModel: TrainingViewModel by viewModels()
    private var hasPermission by mutableStateOf(false)

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            hasPermission = isGranted
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check permission status
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
                        AppNavigation(viewModel, findSaViewModel, trainingViewModel)
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
    trainingViewModel: TrainingViewModel,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Bottom nav items (only Main and Training)
    val bottomNavItems =
        listOf(
            BottomNavItem(AppRoutes.MAIN, "Detect", Icons.Default.Home),
            BottomNavItem(AppRoutes.TRAINING, "Train", Icons.Default.School),
        )

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                // Pop up to the start destination to avoid building up a large back stack
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.MAIN,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppRoutes.MAIN) {
                MainScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = {
                        navController.navigate(AppRoutes.SETTINGS)
                    },
                    onNavigateToFindSa = {
                        navController.navigate(AppRoutes.FIND_SA)
                    },
                )
            }
            composable(AppRoutes.TRAINING) {
                TrainingScreen(viewModel = trainingViewModel)
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
                        // Update the Sa in PitchViewModel and persist it
                        viewModel.updateSa(saNote)
                    },
                )
            }
        }
    }
}

/**
 * Data class for bottom navigation items
 */
private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

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
