package com.solclaw.app.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solclaw.app.accessibility.SolClawAccessibilityService
import com.solclaw.app.ui.theme.SolClawGreen
import com.solclaw.app.ui.theme.SolClawPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val isServiceRunning by SolClawAccessibilityService.isRunning.collectAsState()
    val foregroundApp by SolClawAccessibilityService.foregroundApp.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Accessibility Service Section
            Text(
                text = "Accessibility Service",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Status:",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = if (isServiceRunning) "Enabled" else "Disabled",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isServiceRunning) SolClawGreen else MaterialTheme.colorScheme.error
                        )
                    }

                    Text(
                        text = "SolClaw needs accessibility access to control apps on your behalf — navigate, tap, type, and read screens.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    Button(
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isServiceRunning)
                                MaterialTheme.colorScheme.surface
                            else
                                SolClawPurple
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isServiceRunning)
                                "Open Accessibility Settings"
                            else
                                "Enable Accessibility Service",
                            color = if (isServiceRunning)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            // Foreground App Monitor (visible when service is running)
            if (isServiceRunning) {
                Text(
                    text = "Foreground App Monitor",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoRow("Package", foregroundApp.packageName.ifEmpty { "—" })
                        InfoRow("Class", foregroundApp.className.ifEmpty { "—" })
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // App info
            Text(
                text = "SolClaw v0.1.0 • Phase 1",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text(
            text = "$label: ",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = SolClawGreen
        )
    }
}
