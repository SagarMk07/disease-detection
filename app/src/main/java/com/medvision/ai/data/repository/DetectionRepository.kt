package com.medvision.ai.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.medvision.ai.data.model.DetectionResult
import com.medvision.ai.network.GeminiContent
import com.medvision.ai.network.GeminiGenerationConfig
import com.medvision.ai.network.GeminiInlineData
import com.medvision.ai.network.GeminiPart
import com.medvision.ai.network.GeminiRequest
import com.medvision.ai.network.GeminiService
import java.io.ByteArrayOutputStream
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.HttpException

class DetectionRepository(
    private val context: Context,
    private val service: GeminiService,
    private val model: String,
    private val apiKey: String
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun analyzeImage(imagePath: String): Result<DetectionResult> {
        return try {
            Result.success(analyzeImageWithOpenAi(imagePath))
        } catch (exception: Exception) {
            Result.failure(IllegalStateException(exception.toFriendlyAnalysisMessage(), exception))
        }
    }

    private suspend fun analyzeImageWithOpenAi(imagePath: String): DetectionResult {
        val originalBitmap = BitmapFactory.decodeFile(imagePath) ?: error("Unable to open captured image.")

        if (apiKey.isBlank()) {
            return DetectionResult(
                possibleCondition = "Visual analysis unavailable",
                confidence = 0,
                imagePath = imagePath,
                summary = "Add a Gemini API key to run image analysis."
            )
        }

        val prompt = """
            You are reviewing a user-submitted health image for general visual triage.
            Do not diagnose. Do not claim certainty.
            Identify broad visible possibilities only, such as rash-like irritation, ring-shaped lesion, acne-like breakout, eye redness, unclear image, or non-medical object.
            If the image is unclear or not a health concern, say so.
            Respond only in JSON with keys:
            possible_condition (string), confidence (integer 0-100), summary (string)
        """.trimIndent()

        val response = service.generateContent(
            model = model,
            apiKey = apiKey,
            request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = prompt),
                            GeminiPart(
                                inlineData = GeminiInlineData(
                                    mimeType = "image/jpeg",
                                    data = originalBitmap.toJpegBase64()
                                )
                            )
                        )
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    responseMimeType = "application/json"
                )
            )
        )

        val rawText = response.candidates.orEmpty()
            .flatMap { it.content?.parts.orEmpty() }
            .firstOrNull { !it.text.isNullOrBlank() }
            ?.text
            ?: error("Gemini returned an empty image analysis.")

        val parsed = json.decodeFromString<VisualAnalysisResponse>(rawText)
        return DetectionResult(
            possibleCondition = parsed.possibleCondition.ifBlank { "Visual finding unclear" },
            confidence = parsed.confidence.coerceIn(0, 100),
            imagePath = imagePath,
            summary = parsed.summary
        )
    }

    private fun Bitmap.toJpegBase64(): String {
        val scaled = scaleForAnalysis()
        val output = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 82, output)
        if (scaled !== this) {
            scaled.recycle()
        }
        return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    }

    private fun Bitmap.scaleForAnalysis(): Bitmap {
        val maxSide = 1024
        val largestSide = maxOf(width, height)
        if (largestSide <= maxSide) return this

        val scale = maxSide.toFloat() / largestSide.toFloat()
        val targetWidth = (width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(this, targetWidth, targetHeight, true)
    }

    private fun Exception.toFriendlyAnalysisMessage(): String {
        val httpException = this as? HttpException
        val errorBody = httpException?.response()?.errorBody()?.string().orEmpty()
        return when {
            errorBody.contains("API_KEY_INVALID", ignoreCase = true) ||
                errorBody.contains("API key not valid", ignoreCase = true) ->
                "Gemini API key is invalid. Copy a fresh key from Google AI Studio using the Copy key button, paste it into local.properties, and rebuild the app."
            httpException?.code() == 429 || errorBody.contains("quota", ignoreCase = true) ->
                "Gemini quota is exhausted for this API key. Wait for the free-tier limit to reset, add billing in Google AI Studio, or use another key."
            httpException != null && errorBody.isNotBlank() ->
                "Gemini image analysis failed (${httpException.code()}): ${errorBody.take(240)}"
            httpException != null ->
                "Gemini image analysis failed (${httpException.code()}). Check the model name, API key, and image request."
            message.isNullOrBlank() ->
                "Visual analysis failed. Please try again."
            else -> message.orEmpty()
        }
    }
}

@Serializable
private data class VisualAnalysisResponse(
    @SerialName("possible_condition") val possibleCondition: String,
    val confidence: Int,
    val summary: String = ""
)
