package com.example.rfexplorer.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.rfexplorer.ui.screens.*
import com.example.rfexplorer.ui.theme.*
import com.example.rfexplorer.ui.viewmodel.ExplorerViewModel

sealed class NavDestination(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : NavDestination("dashboard", "Overview", Icons.Default.Radar)
    object Scanner : NavDestination("scanner", "Scanner", Icons.Default.Memory)
    object FmHardware : NavDestination("fm", "FM Radio", Icons.Default.Radio)
    object Platform : NavDestination("platform", "Platform", Icons.Default.DeveloperBoard)
    object Activation : NavDestination("activation", "Activation", Icons.Default.Speed)
    object Graph : NavDestination("graph", "Graph", Icons.Default.Hub)
    object Reports : NavDestination("reports", "Reports", Icons.Default.Description)
    object Console : NavDestination("console", "Console", Icons.Default.Terminal)
}

val primaryDestinations = listOf(
    NavDestination.Dashboard,
    NavDestination.Scanner,
    NavDestination.FmHardware,
    NavDestination.Platform,
    NavDestination.Activation,
    NavDestination.Reports,
    NavDestination.Console
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationScaffold(viewModel: ExplorerViewModel) {
    val navController: NavHostController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavDestination.Dashboard.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val currentDest = primaryDestinations.find { it.route == currentRoute }
                    Text(
                        text = currentDest?.title?.let { "RF Explorer // $it" } ?: "RF Hardware Explorer",
                        style = MaterialTheme.typography.titleMedium.copy(color = NeonCyan, fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.runScan() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Scan", tint = NeonCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberObsidian)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CyberSurface,
                contentColor = NeonCyan
            ) {
                primaryDestinations.forEach { dest ->
                    val selected = currentRoute == dest.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != dest.route) {
                                navController.navigate(dest.route) {
                                    popUpTo(NavDestination.Dashboard.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.title) },
                        label = { Text(dest.title, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan,
                            unselectedIconColor = SlateText,
                            unselectedTextColor = SlateText
                        )
                    )
                }
            }
        },
        containerColor = CyberObsidian
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = NavDestination.Dashboard.route) {
                composable(NavDestination.Dashboard.route) {
                    DashboardScreen(viewModel = viewModel, onNavigate = { route ->
                        navController.navigate(route)
                    })
                }
                composable(NavDestination.Scanner.route) {
                    ScannerScreen(viewModel = viewModel)
                }
                composable(NavDestination.FmHardware.route) {
                    FmHardwareScreen(viewModel = viewModel)
                }
                composable(NavDestination.Platform.route) {
                    PlatformScreen(viewModel = viewModel)
                }
                composable(NavDestination.Activation.route) {
                    ActivationScreen(viewModel = viewModel)
                }
                composable(NavDestination.Graph.route) {
                    DependencyGraphScreen(viewModel = viewModel)
                }
                composable(NavDestination.Reports.route) {
                    ReportsScreen(viewModel = viewModel)
                }
                composable(NavDestination.Console.route) {
                    DeveloperConsoleScreen(viewModel = viewModel)
                }
            }
        }
    }
}
