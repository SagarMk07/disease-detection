package com.medvision.ai.data.model

data class DetectionResult(
    val possibleCondition: String,
    val confidence: Int,
    val imagePath: String? = null,
    val summary: String = "",
    val disclaimer: String = APP_DISCLAIMER
)
