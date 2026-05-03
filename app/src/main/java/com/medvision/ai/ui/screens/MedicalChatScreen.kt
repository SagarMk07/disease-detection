package com.medvision.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.medvision.ai.data.model.APP_DISCLAIMER
import com.medvision.ai.data.repository.MedicalChatTurn
import com.medvision.ai.ui.components.DisclaimerBanner
import com.medvision.ai.ui.components.GlassCard
import com.medvision.ai.ui.components.LoadingCard
import com.medvision.ai.ui.components.ScreenHeader
import com.medvision.ai.viewmodel.MedicalChatViewModel

@Composable
fun MedicalChatScreen(
    viewModel: MedicalChatViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        ScreenHeader(
            title = "AI Health Chat",
            subtitle = "Ask about symptoms, care steps, and medicine safety questions."
        )
        DisclaimerBanner(text = APP_DISCLAIMER)
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.messages) { message ->
                    ChatBubble(message = message)
                }
                if (state.isLoading) {
                    item {
                        LoadingCard(message = "Thinking through your question...")
                    }
                }
            }
        }
        state.error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.input,
                    onValueChange = viewModel::updateInput,
                    modifier = Modifier.weight(1f),
                    label = { Text("Message") },
                    minLines = 1,
                    maxLines = 4,
                    enabled = !state.isLoading
                )
                IconButton(
                    onClick = viewModel::sendMessage,
                    enabled = state.input.isNotBlank() && !state.isLoading
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: MedicalChatTurn) {
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bubbleColor = when {
        message.isUser -> MaterialTheme.colorScheme.primary
        isDark -> Color.White.copy(alpha = 0.10f)
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    }
    val textColor = if (message.isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .background(bubbleColor, RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (message.isUser) "You" else "MedVision AI",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor.copy(alpha = 0.78f)
            )
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }
}
