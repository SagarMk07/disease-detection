package com.medvision.ai.data.repository

import com.medvision.ai.data.model.AiAnalysisResponse
import com.medvision.ai.data.model.AnalysisResult
import com.medvision.ai.network.OpenAiRequest
import com.medvision.ai.network.OpenAiResponsesService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

class AiRepository(
    private val service: OpenAiResponsesService,
    private val model: String,
    private val apiKey: String
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun analyzeSymptoms(symptoms: String): Result<AnalysisResult> = runCatching {
        if (apiKey.isBlank()) {
            return@runCatching sampleResponse(symptoms)
        }

        val prompt = """
            User symptoms: $symptoms.
            Suggest possible conditions, risk level, and advice. Keep it short.
            Respond only in JSON with keys:
            possible_conditions (array of strings), risk_level (string), suggested_actions (string)
        """.trimIndent()

        val response = service.createResponse(
            request = OpenAiRequest(
                model = model,
                input = JsonPrimitive(prompt),
                text = OpenAiRequest.TextConfig(
                    format = OpenAiRequest.FormatConfig(type = "json_object")
                )
            )
        )

        val rawText = response.output.orEmpty()
            .flatMap { it.content.orEmpty() }
            .firstOrNull { it.type == "output_text" }
            ?.text
            ?: error("AI returned an empty response.")

        val parsed = json.decodeFromString<AiAnalysisResponse>(rawText)
        AnalysisResult(
            conditions = parsed.possibleConditions,
            riskLevel = parsed.riskLevel,
            advice = parsed.suggestedActions
        )
    }

    private fun sampleResponse(symptoms: String): AnalysisResult {
        val lowered = symptoms.lowercase()
        return when {
            "fever" in lowered && "cough" in lowered -> AnalysisResult(
                conditions = listOf("Viral infection", "Seasonal flu", "Upper respiratory irritation"),
                riskLevel = "Medium",
                advice = "Rest, hydrate, monitor temperature, and consult a clinician if symptoms worsen."
            )

            "rash" in lowered || "itch" in lowered -> AnalysisResult(
                conditions = listOf("Skin irritation", "Allergic reaction", "Eczema flare"),
                riskLevel = "Low",
                advice = "Avoid irritants, keep the area clean, and seek medical review if swelling or pain appears."
            )

            else -> AnalysisResult(
                conditions = listOf("General inflammation", "Mild infection", "Condition unclear"),
                riskLevel = "Low",
                advice = "Track symptoms and consult a professional if symptoms persist or intensify."
            )
        }
    }
}
