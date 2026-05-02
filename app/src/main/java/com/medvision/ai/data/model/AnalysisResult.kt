package com.medvision.ai.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnalysisResult(
    val conditions: List<String>,
    val riskLevel: String,
    val advice: String,
    val disclaimer: String = APP_DISCLAIMER
)

@Serializable
data class AiAnalysisResponse(
    @SerialName("possible_conditions") val possibleConditions: List<String>,
    @SerialName("risk_level") val riskLevel: String,
    @SerialName("suggested_actions") val suggestedActions: String
)

const val APP_DISCLAIMER =
    "This app provides general health insights and is not a substitute for professional medical advice."
