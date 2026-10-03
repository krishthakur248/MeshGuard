package com.example.meshguard.ui.navigation

/**
 * Type-safe navigation routes for MeshGuard.
 */
sealed class Screen(val route: String) {

    // Onboarding & Permissions Flow
    data object Onboarding : Screen("onboarding")
    data object Permissions : Screen("permissions")

    // Survivor / Civilian Core Screens
    data object Home : Screen("home")
    data object StatusPicker : Screen("status_picker")
    data object MedicalId : Screen("medical_id")
    data object MeshNetwork : Screen("mesh_network")
    data object Chat : Screen("chat")
    data object Breadcrumbs : Screen("breadcrumbs")

    // Responder / Rescue Worker Screens
    data object ResponderDashboard : Screen("responder_dashboard")
    data object RescuerMap : Screen("rescuer_map")   // Step 11
    data object SurvivorDetail : Screen("survivor_detail/{survivorId}") {
        const val ARG_SURVIVOR_ID = "survivorId"
        fun createRoute(survivorId: String): String = "survivor_detail/$survivorId"
    }

    // Common Settings
    data object Settings : Screen("settings")
}
