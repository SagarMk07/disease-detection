package com.medvision.ai.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.medvision.ai.ui.components.DisclaimerBanner
import com.medvision.ai.ui.components.GlassCard
import com.medvision.ai.ui.components.LoadingCard
import com.medvision.ai.ui.components.PrimaryActionButton
import com.medvision.ai.ui.components.ScreenHeader
import com.medvision.ai.ui.components.copyGalleryImageToCache
import com.medvision.ai.viewmodel.ScanComparisonViewModel

@Composable
fun ScanComparisonScreen(
    viewModel: ScanComparisonViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val earlierLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            copyGalleryImageToCache(
                context = context,
                uri = it,
                onSuccess = viewModel::setEarlierImage,
                onError = viewModel::setError
            )
        }
    }
    val newerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            copyGalleryImageToCache(
                context = context,
                uri = it,
                onSuccess = viewModel::setNewerImage,
                onError = viewModel::setError
            )
        }
    }

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
            title = "Scan Comparison",
            subtitle = "Compare an earlier and newer skin or eye image to track visible changes."
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ImagePickerCard(
                    title = "Earlier image",
                    imagePath = state.earlierImagePath,
                    onPick = { earlierLauncher.launch("image/*") }
                )
                ImagePickerCard(
                    title = "Newer image",
                    imagePath = state.newerImagePath,
                    onPick = { newerLauncher.launch("image/*") }
                )
                PrimaryActionButton(
                    text = "Compare Scans",
                    onClick = viewModel::compare,
                    enabled = !state.isLoading &&
                        !state.earlierImagePath.isNullOrBlank() &&
                        !state.newerImagePath.isNullOrBlank()
                )
                state.error?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        if (state.isLoading) {
            LoadingCard(message = "Comparing visible changes...")
        }
        state.result?.let { result ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Compare, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = result.trend,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("Confidence: ${result.confidence}%")
                    if (result.summary.isNotBlank()) {
                        Text(result.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (result.suggestedAction.isNotBlank()) {
                        Text("Suggested action: ${result.suggestedAction}")
                    }
                }
            }
            DisclaimerBanner(text = result.disclaimer)
        }
    }
}

@Composable
private fun ImagePickerCard(
    title: String,
    imagePath: String?,
    onPick: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (!imagePath.isNullOrBlank()) {
                AsyncImage(
                    model = imagePath,
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
            OutlinedButton(
                onClick = onPick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Text(
                    text = if (imagePath.isNullOrBlank()) "Choose from Gallery" else "Change Image",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
