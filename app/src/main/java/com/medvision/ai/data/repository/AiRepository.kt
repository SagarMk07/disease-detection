package com.medvision.ai.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.medvision.ai.data.model.AiAnalysisResponse
import com.medvision.ai.data.model.AnalysisResult
import com.medvision.ai.network.GeminiContent
import com.medvision.ai.network.GeminiGenerationConfig
import com.medvision.ai.network.GeminiPart
import com.medvision.ai.network.GeminiRequest
import com.medvision.ai.network.GeminiService
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

class AiRepository(
    private val context: Context,
    private val service: GeminiService,
    private val model: String,
    private val apiKey: String
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun analyzeSymptoms(symptoms: String): Result<AnalysisResult> {
        return try {
            if (apiKey.isBlank()) {
                return Result.success(sampleResponse(symptoms))
            }
            if (!isInternetAvailable()) {
                return Result.failure(NoInternetException())
            }

            val prompt = """
                You are MedVision AI Symptom Checker, a cautious medical information assistant.

                Rules:
                - Do not diagnose, prescribe medicine, or replace a clinician.
                - Suggest possible broad causes, a simple risk level, and practical next steps.
                - Recommend urgent care for red flags such as chest pain, trouble breathing, stroke symptoms, severe allergic reaction, severe dehydration, fainting, severe pain, high fever in infants, suicidal thoughts, or rapidly worsening symptoms.
                - Keep it short.
                - Respond only in JSON with keys:
                  possible_conditions (array of strings), risk_level (string), suggested_actions (string)

                User symptoms: $symptoms
            """.trimIndent()

            val response = service.generateContent(
                model = model,
                apiKey = apiKey,
                request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                    ),
                    generationConfig = GeminiGenerationConfig(
                        responseMimeType = "application/json"
                    )
                )
            )
            Log.d(TAG, "Gemini symptom response candidates=${response.candidates?.size ?: 0}")

            val rawText = response.candidates.orEmpty()
                .flatMap { it.content?.parts.orEmpty() }
                .firstOrNull { !it.text.isNullOrBlank() }
                ?.text
                ?.trim()
                ?: error("Gemini returned an empty symptom analysis.")

            Log.d(TAG, "Raw symptom response: $rawText")
            val parsed = json.decodeFromString<AiAnalysisResponse>(rawText.extractJsonPayload())
            Result.success(
                AnalysisResult(
                    conditions = parsed.possibleConditions,
                    riskLevel = parsed.riskLevel,
                    advice = parsed.suggestedActions
                )
            )
        } catch (exception: Exception) {
            Log.e(TAG, "Gemini symptom analysis failed", exception)
            Result.failure(IllegalStateException(exception.toFriendlySymptomMessage(), exception))
        }
    }

    private fun String.extractJsonPayload(): String {
        val trimmed = trim()
        val withoutFence = trimmed
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val start = withoutFence.indexOf('{')
        val end = withoutFence.lastIndexOf('}')
        return if (start >= 0 && end >= start) {
            withoutFence.substring(start, end + 1)
        } else {
            withoutFence
        }
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

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun Exception.toFriendlySymptomMessage(): String {
        if (this is NoInternetException) {
            return "No internet connection"
        }
        val httpException = findHttpException()
        val errorBody = httpException?.response()?.errorBody()?.string().orEmpty()
        return when {
            errorBody.contains("API_KEY_INVALID", ignoreCase = true) ||
                errorBody.contains("API key not valid", ignoreCase = true) ->
                "Gemini API key is invalid. Add a fresh GEMINI_API_KEY in local.properties and rebuild the app."
            httpException?.code() == 429 || errorBody.contains("quota", ignoreCase = true) ->
                "Server is busy or rate limited. Please try again in a moment."
            httpException?.code() == 503 || errorBody.contains("UNAVAILABLE", ignoreCase = true) ->
                "Server is busy. Please try again in a moment."
            this is IOException ->
                "Network request failed. Please check your connection."
            httpException != null ->
                "Gemini symptom analysis failed (${httpException.code()}). Check the model name and API key."
            message.isNullOrBlank() -> "AI symptom analysis failed. Please try again."
            else -> message.orEmpty()
        }
    }

    private fun Throwable.findHttpException(): HttpException? {
        var current: Throwable? = this
        while (current != null) {
            if (current is HttpException) {
                return current
            }
            current = current.cause
        }
        return null
    }

    private companion object {
        const val TAG = "AiRepository"
    }
}

class NoInternetException : IOException("No internet connection")
