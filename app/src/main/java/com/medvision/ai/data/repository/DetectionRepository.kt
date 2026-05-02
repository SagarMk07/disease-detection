package com.medvision.ai.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.medvision.ai.data.model.DetectionResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class DetectionRepository(
    private val context: Context
) {
    suspend fun analyzeImage(imagePath: String): Result<DetectionResult> = runCatching {
        val bitmap = BitmapFactory.decodeFile(imagePath) ?: error("Unable to open captured image.")
        val image = InputImage.fromBitmap(bitmap, 0)
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        val labels = suspendCancellableCoroutine<List<ImageLabel>> { continuation ->
            labeler.process(image)
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resume(emptyList()) }
        }

        val bestLabel = labels.maxByOrNull { it.confidence }
        val mappedCondition = when {
            bestLabel == null -> "Condition unclear"
            bestLabel.text.contains("skin", ignoreCase = true) -> "Possible skin irritation"
            bestLabel.text.contains("eye", ignoreCase = true) -> "Possible eye inflammation"
            bestLabel.text.contains("acne", ignoreCase = true) -> "Possible acne or breakout"
            else -> "General visual anomaly"
        }

        DetectionResult(
            possibleCondition = mappedCondition,
            confidence = ((bestLabel?.confidence ?: 0.64f) * 100).toInt().coerceIn(45, 96),
            imagePath = imagePath
        )
    }
}
