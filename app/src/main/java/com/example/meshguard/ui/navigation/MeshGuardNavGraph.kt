package com.example.meshguard.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.meshguard.R
import com.example.meshguard.data.model.UserRole
import com.example.meshguard.ui.AppStateManager
import com.example.meshguard.ui.screens.onboarding.OnboardingRoute
import com.example.meshguard.ui.screens.onboarding.PermissionsRoute
import com.example.meshguard.ui.screens.survivor.BreadcrumbChainRoute
import com.example.meshguard.ui.screens.survivor.ChatRoute
import com.example.meshguard.ui.screens.survivor.HomeScreen
import com.example.meshguard.ui.screens.survivor.MedicalIdRoute
import com.example.meshguard.ui.screens.survivor.MeshNetworkRoute
import com.example.meshguard.ui.screens.rescuer.ResponderDashboardRoute
import com.example.meshguard.ui.screens.rescuer.SurvivorDetailRoute
import com.example.meshguard.ui.screens.survivor.StatusPickerScreen
import com.example.meshguard.ui.theme.ColorBackgroundDark
import com.example.meshguard.ui.theme.ColorSurfaceDark
import com.example.meshguard.ui.theme.EmergencyRed
import com.example.meshguard.ui.theme.TextMuted
import com.example.meshguard.ui.theme.TextPrimary

private data class BottomNavItem(
    val titleRes: Int,
    val route: String,
    val icon: ImageVector
)

@Composable
fun MeshGuardNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val userRole by AppStateManager.userRole.collectAsState()

    val bottomNavItems = if (userRole == UserRole.RESCUER) {
        listOf(
            BottomNavItem(R.string.nav_responder_dashboard, Screen.ResponderDashboard.route, Icons.Default.Shield),
            BottomNavItem(R.string.nav_mesh, Screen.MeshNetwork.route, Icons.Default.Hub),
            BottomNavItem(R.string.nav_chat, Screen.Chat.route, Icons.Default.ChatBubble)
        )
    } else {
        listOf(
            BottomNavItem(R.string.nav_home, Screen.Home.route, Icons.Default.Sensors),
            BottomNavItem(R.string.nav_status, Screen.StatusPicker.route, Icons.Default.MedicalServices),
            BottomNavItem(R.string.nav_chat, Screen.Chat.route, Icons.Default.ChatBubble)
        )
    }

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ColorBackgroundDark,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = ColorSurfaceDark,
                    contentColor = TextPrimary
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = stringResource(item.titleRes)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(item.titleRes),
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EmergencyRed,
                                selectedTextColor = EmergencyRed,
                                indicatorColor = ColorSurfaceDark,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Onboarding & Permissions
            composable(Screen.Onboarding.route) {
                OnboardingRoute(
                    onNavigateToPermissions = {
                        navController.navigate(Screen.Permissions.route)
                    }
                )
            }

            composable(Screen.Permissions.route) {
                PermissionsRoute(
                    onNavigateToHome = {
                        val targetRoute = if (AppStateManager.userRole.value == UserRole.RESCUER) {
                            Screen.ResponderDashboard.route
                        } else {
                            Screen.Home.route
                        }
                        navController.navigate(targetRoute) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // Survivor Screens
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToStatusPicker = { navController.navigate(Screen.StatusPicker.route) },
                    onNavigateToMedicalId = { navController.navigate(Screen.MedicalId.route) },
                    onNavigateToMeshNetwork = {},
                    onNavigateToChat = { navController.navigate(Screen.Chat.route) },
                    onNavigateToBreadcrumbs = { navController.navigate(Screen.Breadcrumbs.route) }
                )
            }

            composable(Screen.StatusPicker.route) {
                StatusPickerScreen(
                    onConfirmComplete = { navController.navigate(Screen.Home.route) }
                )
            }

            composable(Screen.MedicalId.route) {
                MedicalIdRoute(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.MeshNetwork.route) {
                if (userRole != UserRole.RESCUER) {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                } else {
                    MeshNetworkRoute(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Chat.route) {
                ChatRoute(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Breadcrumbs.route) {
                BreadcrumbChainRoute(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Responder Screens
            composable(Screen.ResponderDashboard.route) {
                if (userRole != UserRole.RESCUER) {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                } else {
                    ResponderDashboardRoute(
                        onNavigateToSurvivorDetail = { survivorId ->
                            navController.navigate(Screen.SurvivorDetail.createRoute(survivorId))
                        }
                    )
                }
            }

            composable(
                route = Screen.SurvivorDetail.route,
                arguments = listOf(
                    navArgument(Screen.SurvivorDetail.ARG_SURVIVOR_ID) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val survivorId = backStackEntry.arguments?.getString(Screen.SurvivorDetail.ARG_SURVIVOR_ID) ?: ""
                if (userRole != UserRole.RESCUER) {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                } else {
                    SurvivorDetailRoute(
                        survivorId = survivorId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            // Settings
            composable(Screen.Settings.route) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ColorBackgroundDark)
                ) {
                    Text(
                        text = stringResource(R.string.nav_settings),
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
