package com.medvision.ai.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.medvision.ai.ui.components.CameraPreview
import com.medvision.ai.ui.components.DisclaimerBanner
import com.medvision.ai.ui.components.GlassCard
import com.medvision.ai.ui.components.LoadingCard
import com.medvision.ai.ui.components.PrimaryActionButton
import com.medvision.ai.ui.components.ScreenHeader
import com.medvision.ai.ui.components.capturePhoto
import com.medvision.ai.ui.components.copyGalleryImageToCache
import com.medvision.ai.viewmodel.ScanViewModel

@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var hasPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            copyGalleryImageToCache(
                context = context,
                uri = it,
                onSuccess = viewModel::analyzeImage,
                onError = viewModel::setError
            )
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        ScreenHeader(
            title = "Disease Scan",
            subtitle = "Capture a visible concern like skin or eye irritation for on-device analysis."
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (hasPermission) {
                    CameraPreview(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        onImageCaptureReady = { imageCapture = it }
                    )
                    PrimaryActionButton(
                        text = "Capture & Analyze",
                        onClick = {
                            imageCapture?.let {
                                capturePhoto(
                                    context = context,
                                    imageCapture = it,
                                    executor = ContextCompat.getMainExecutor(context),
                                    onSuccess = viewModel::analyzeImage,
                                    onError = viewModel::setError
                                )
                            }
                        },
                        enabled = imageCapture != null && !state.isLoading
                    )
                } else {
                    Text("Camera permission is required to scan images.", color = MaterialTheme.colorScheme.error)
                }
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Text(
                        text = "Upload from Gallery",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                state.error?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        if (state.isLoading) {
            LoadingCard(message = "Running visual analysis...")
        }
        state.latestImagePath?.takeIf { it.isNotBlank() }?.let { imagePath ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = imagePath,
                    contentDescription = "Captured image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(12.dp)
                )
            }
        }
        state.result?.let { result ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Possible condition: ${result.possibleCondition}", style = MaterialTheme.typography.titleLarge)
                    Text("Confidence: ${result.confidence}%")
                    if (result.summary.isNotBlank()) {
                        Text(result.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            DisclaimerBanner(text = result.disclaimer)
        }
    }
}
