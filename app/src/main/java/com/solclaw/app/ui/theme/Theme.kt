package com.solclaw.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SolClawPurple,
    secondary = SolClawGreen,
    background = SolClawDark,
    surface = SolClawSurface,
    onSurface = SolClawOnSurface,
    onBackground = SolClawOnSurface,
    onPrimary = SolClawOnSurface,
)

@Composable
fun SolClawTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
