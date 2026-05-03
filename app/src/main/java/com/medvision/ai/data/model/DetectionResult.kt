package com.medvision.ai.data.model

data class DetectionResult(
    val possibleCondition: String,
    val confidence: Int,
    val imagePath: String? = null,
    val summary: String = "",
    val disclaimer: String = APP_DISCLAIMER
)

data class ScanComparisonResult(
    val trend: String,
    val confidence: Int,
    val summary: String,
    val suggestedAction: String,
    val disclaimer: String = APP_DISCLAIMER
)
