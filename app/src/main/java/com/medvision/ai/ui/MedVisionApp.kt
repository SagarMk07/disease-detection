package com.medvision.ai.ui

import android.app.Application
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.medvision.ai.ui.components.GradientBackground
import com.medvision.ai.ui.screens.AuthScreen
import com.medvision.ai.ui.screens.HistoryScreen
import com.medvision.ai.ui.screens.HomeScreen
import com.medvision.ai.ui.screens.MedicalChatScreen
import com.medvision.ai.ui.screens.ScanScreen
import com.medvision.ai.ui.screens.SettingsScreen
import com.medvision.ai.ui.screens.SymptomCheckerScreen
import com.medvision.ai.viewmodel.AppViewModelFactory
import com.medvision.ai.viewmodel.AuthViewModel
import com.medvision.ai.viewmodel.HistoryViewModel
import com.medvision.ai.viewmodel.HomeViewModel
import com.medvision.ai.viewmodel.MedicalChatViewModel
import com.medvision.ai.viewmodel.ScanViewModel
import com.medvision.ai.viewmodel.SettingsViewModel
import com.medvision.ai.viewmodel.SymptomCheckerViewModel

@Composable
fun MedVisionApp(application: Application) {
    val navController = rememberNavController()
    val factory = AppViewModelFactory(application)

    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val homeViewModel: HomeViewModel = viewModel(factory = factory)
    val symptomViewModel: SymptomCheckerViewModel = viewModel(factory = factory)
    val scanViewModel: ScanViewModel = viewModel(factory = factory)
    val medicalChatViewModel: MedicalChatViewModel = viewModel(factory = factory)
    val historyViewModel: HistoryViewModel = viewModel(factory = factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)

    val authState by authViewModel.uiState.collectAsState()
    val items = listOf(
        BottomNavItem("home", "Home", Icons.Default.Home),
        BottomNavItem("history", "History", Icons.Default.History),
        BottomNavItem("settings", "Settings", Icons.Default.Settings)
    )

    GradientBackground {
        AnimatedContent(
            targetState = authState.user != null,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "root_switch"
        ) { signedIn ->
            if (!signedIn) {
                AuthScreen(
                    state = authState,
                    onEmailChange = authViewModel::updateEmail,
                    onPasswordChange = authViewModel::updatePassword,
                    onNameChange = authViewModel::updateName,
                    onToggleMode = authViewModel::toggleMode,
                    onSubmit = authViewModel::submit
                )
            } else {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    bottomBar = {
                        val backStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = backStackEntry?.destination
                        val showBottomBar = currentDestination?.route in items.map { it.route }
                        if (showBottomBar) {
                            NavigationBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)) {
                                items.forEach { item ->
                                    NavigationBarItem(
                                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(item.icon, contentDescription = item.label) },
                                        label = { Text(item.label) }
                                    )
                                }
                            }
                        }
                    }
                ) { padding ->
                    MedVisionNavHost(
                        navController = navController,
                        padding = padding,
                        homeViewModel = homeViewModel,
                        symptomViewModel = symptomViewModel,
                        scanViewModel = scanViewModel,
                        medicalChatViewModel = medicalChatViewModel,
                        historyViewModel = historyViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun MedVisionNavHost(
    navController: androidx.navigation.NavHostController,
    padding: PaddingValues,
    homeViewModel: HomeViewModel,
    symptomViewModel: SymptomCheckerViewModel,
    scanViewModel: ScanViewModel,
    medicalChatViewModel: MedicalChatViewModel,
    historyViewModel: HistoryViewModel,
    settingsViewModel: SettingsViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(padding)
    ) {
        composable("home") {
            HomeScreen(
                viewModel = homeViewModel,
                onOpenSymptoms = { navController.navigate("symptoms") },
                onOpenScan = { navController.navigate("scan") },
                onOpenChat = { navController.navigate("medical_chat") },
                onOpenHistory = { navController.navigate("history") }
            )
        }
        composable("symptoms") {
            SymptomCheckerScreen(
                viewModel = symptomViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("scan") {
            ScanScreen(
                viewModel = scanViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("medical_chat") {
            MedicalChatScreen(
                viewModel = medicalChatViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("history") {
            HistoryScreen(viewModel = historyViewModel)
        }
        composable("settings") {
            SettingsScreen(viewModel = settingsViewModel)
        }
    }
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
