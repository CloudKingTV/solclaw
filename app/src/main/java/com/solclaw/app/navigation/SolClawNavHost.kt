package com.solclaw.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.solclaw.app.chat.ChatScreen
import com.solclaw.app.dashboard.DashboardScreen
import com.solclaw.app.marketplace.MarketplaceScreen
import com.solclaw.app.settings.SettingsScreen

@Composable
fun SolClawNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Chat.route,
        modifier = modifier
    ) {
        composable(Screen.Chat.route) { ChatScreen() }
        composable(Screen.Dashboard.route) { DashboardScreen() }
        composable(Screen.Marketplace.route) { MarketplaceScreen() }
        composable(Screen.Settings.route) { SettingsScreen() }
    }
}
