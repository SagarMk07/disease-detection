package com.medvision.ai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.medvision.ai.data.model.HistoryType
import com.medvision.ai.ui.components.GlassCard
import com.medvision.ai.ui.components.ScreenHeader
import com.medvision.ai.utils.formatTimestamp
import com.medvision.ai.viewmodel.HistoryViewModel

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val items by viewModel.items.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenHeader(
                title = "Health History",
                subtitle = "Your previous symptom checks and image scans."
            )
        }
        if (items.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No history yet. Run a symptom check or scan to build your timeline.",
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(items) { item ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = when (item.type) {
                                HistoryType.SYMPTOM -> "Symptom analysis"
                                HistoryType.SCAN -> "Scan analysis"
                                HistoryType.COMPARISON -> "Scan comparison"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(formatTimestamp(item.timestamp), color = MaterialTheme.colorScheme.primary)
                        if (!item.imagePath.isNullOrBlank()) {
                            AsyncImage(
                                model = item.imagePath,
                                contentDescription = "History image",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (item.inputText.isNotBlank()) {
                            Text("Input: ${item.inputText}")
                        }
                        Text("Result: ${item.resultTitle}")
                        Text(item.resultDetail, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
