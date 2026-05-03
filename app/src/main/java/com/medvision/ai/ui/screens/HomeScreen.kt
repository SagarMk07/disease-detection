package com.medvision.ai.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.medvision.ai.data.model.APP_DISCLAIMER
import com.medvision.ai.ui.components.DisclaimerBanner
import com.medvision.ai.ui.components.GlassCard
import com.medvision.ai.ui.components.ScreenHeader
import com.medvision.ai.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenSymptoms: () -> Unit,
    onOpenScan: () -> Unit,
    onOpenComparison: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenHistory: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val cards = listOf(
        Triple("Symptom Checker", Icons.Default.Psychology, onOpenSymptoms),
        Triple("Scan Disease", Icons.Default.CameraAlt, onOpenScan),
        Triple("Scan Comparison", Icons.Default.Compare, onOpenComparison),
        Triple("AI Health Chat", Icons.AutoMirrored.Filled.Chat, onOpenChat),
        Triple("Health History", Icons.Default.MonitorHeart, onOpenHistory)
    )
    val accent by animateColorAsState(targetValue = MaterialTheme.colorScheme.primary, label = "accent")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScreenHeader(
                title = "Hello, ${user?.name ?: "Guest"}",
                subtitle = "Track symptoms, scan images, and keep your health story in one place."
            )
        }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Daily Insight", style = MaterialTheme.typography.titleMedium, color = accent)
                    Text(
                        "Use MedVision AI for quick health insight summaries, then confirm concerns with a licensed professional.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        items(cards) { (title, icon, action) ->
            GlassActionCard(title = title, icon = icon, onClick = action)
        }
        item {
            DisclaimerBanner(text = APP_DISCLAIMER)
        }
    }
}

@Composable
private fun GlassActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    text = when (title) {
                        "Symptom Checker" -> "Type symptoms and receive AI-generated condition possibilities."
                        "Scan Disease" -> "Capture an image and run visual pattern analysis."
                        "Scan Comparison" -> "Compare earlier and newer images to track visible changes."
                        "AI Health Chat" -> "Ask follow-up questions about symptoms, care steps, and medicine safety."
                        else -> "Review your past symptom checks and scans."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
