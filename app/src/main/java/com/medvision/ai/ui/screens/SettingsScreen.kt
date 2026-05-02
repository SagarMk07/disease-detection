package com.medvision.ai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.medvision.ai.data.model.APP_DISCLAIMER
import com.medvision.ai.ui.components.DisclaimerBanner
import com.medvision.ai.ui.components.GlassCard
import com.medvision.ai.ui.components.PrimaryActionButton
import com.medvision.ai.ui.components.ScreenHeader
import com.medvision.ai.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val darkMode by viewModel.darkMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(
            title = "Settings",
            subtitle = "Tune the experience and learn more about the app."
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Dark Mode", style = MaterialTheme.typography.titleMedium)
                Switch(checked = darkMode, onCheckedChange = viewModel::setDarkMode)
            }
        }
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("About", style = MaterialTheme.typography.titleMedium)
                Text(
                    "MedVision AI combines secure authentication, symptom analysis, camera-based scanning, and health history tracking in a premium Compose interface."
                )
            }
        }
        PrimaryActionButton(text = "Logout", onClick = viewModel::logout)
        DisclaimerBanner(text = APP_DISCLAIMER)
    }
}
