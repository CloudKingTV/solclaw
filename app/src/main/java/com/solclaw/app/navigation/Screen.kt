package com.solclaw.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Chat : Screen("chat", "Chat", Icons.Filled.Email)
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Home)
    data object Marketplace : Screen("marketplace", "Market", Icons.Filled.ShoppingCart)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)

    companion object {
        val all = listOf(Chat, Dashboard, Marketplace, Settings)
    }
}
