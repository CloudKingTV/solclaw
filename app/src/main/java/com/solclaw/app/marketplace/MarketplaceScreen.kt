package com.solclaw.app.marketplace

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solclaw.app.ui.theme.SolClawPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Marketplace", fontWeight = FontWeight.Bold) },
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
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🛒", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Agent Marketplace",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = SolClawPurple
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Browse, buy, and sell trained AI agents.\nComing in Phase 3.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}
