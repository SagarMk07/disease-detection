package com.medvision.ai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.medvision.ai.ui.components.DisclaimerBanner
import com.medvision.ai.ui.components.GlassCard
import com.medvision.ai.ui.components.LoadingCard
import com.medvision.ai.ui.components.PrimaryActionButton
import com.medvision.ai.ui.components.ScreenHeader
import com.medvision.ai.viewmodel.SymptomCheckerViewModel

@Composable
fun SymptomCheckerScreen(
    viewModel: SymptomCheckerViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
        ScreenHeader(
            title = "AI Symptom Checker",
            subtitle = "Describe what you feel in natural language."
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.symptoms,
                    onValueChange = viewModel::updateSymptoms,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Symptoms") },
                    minLines = 5
                )
                state.error?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
                PrimaryActionButton(
                    text = "Analyze",
                    onClick = viewModel::analyze,
                    enabled = !state.isLoading
                )
            }
        }
        if (state.isLoading) {
            LoadingCard(message = "Analyzing your symptoms with AI...")
        }
        state.result?.let { result ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Possible conditions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(result.conditions.joinToString(separator = "\n• ", prefix = "• "))
                    Text("Risk level: ${result.riskLevel}", color = MaterialTheme.colorScheme.primary)
                    Text("Suggested actions: ${result.advice}")
                }
            }
            DisclaimerBanner(text = result.disclaimer)
        }
    }
}
