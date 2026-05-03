package com.medvision.ai.data.repository

import com.medvision.ai.network.GeminiContent
import com.medvision.ai.network.GeminiGenerationConfig
import com.medvision.ai.network.GeminiPart
import com.medvision.ai.network.GeminiRequest
import com.medvision.ai.network.GeminiService
import retrofit2.HttpException

class MedicalChatRepository(
    private val service: GeminiService,
    private val model: String,
    private val apiKey: String
) {
    suspend fun sendMessage(
        userMessage: String,
        previousMessages: List<MedicalChatTurn>
    ): Result<String> {
        return try {
            if (apiKey.isBlank()) {
                return Result.success(
                    "Gemini API key is missing. Add GEMINI_API_KEY in local.properties, rebuild the app, and I can answer health questions here."
                )
            }

            val transcript = previousMessages
                .takeLast(10)
                .joinToString(separator = "\n") { turn ->
                    "${if (turn.isUser) "User" else "Assistant"}: ${turn.text}"
                }

            val prompt = """
                You are MedVision AI Health Chat, a cautious medical information assistant.

                Rules:
                - Do not diagnose, prescribe, or replace a clinician.
                - You may explain possible causes, self-care steps, questions to ask a doctor, and common over-the-counter medicine categories.
                - For medicines, mention that the user must follow the package label or clinician/pharmacist advice. Do not give personalized dosage.
                - Ask about age, pregnancy, allergies, existing conditions, current medicines, symptom duration, and severity when relevant.
                - Urgently recommend emergency care for red flags such as chest pain, trouble breathing, stroke symptoms, severe allergic reaction, severe dehydration, fainting, severe pain, high fever in infants, suicidal thoughts, or rapidly worsening symptoms.
                - Keep answers clear, practical, and brief.

                Conversation so far:
                $transcript

                User: $userMessage
                Assistant:
            """.trimIndent()

            val response = service.generateContent(
                model = model,
                apiKey = apiKey,
                request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                    ),
                    generationConfig = GeminiGenerationConfig()
                )
            )

            val text = response.candidates.orEmpty()
                .flatMap { it.content?.parts.orEmpty() }
                .firstOrNull { !it.text.isNullOrBlank() }
                ?.text
                ?.trim()
                ?: error("Gemini returned an empty chat response.")

            Result.success(text)
        } catch (exception: Exception) {
            Result.failure(IllegalStateException(exception.toFriendlyChatMessage(), exception))
        }
    }

    private fun Exception.toFriendlyChatMessage(): String {
        val httpException = this as? HttpException
        val errorBody = httpException?.response()?.errorBody()?.string().orEmpty()
        return when {
            errorBody.contains("API_KEY_INVALID", ignoreCase = true) ||
                errorBody.contains("API key not valid", ignoreCase = true) ->
                "Gemini API key is invalid. Add a fresh key in local.properties and rebuild the app."
            httpException?.code() == 429 || errorBody.contains("quota", ignoreCase = true) ->
                "Gemini quota is exhausted for this API key. Try again later or use another key."
            httpException != null && errorBody.isNotBlank() ->
                "Gemini chat failed (${httpException.code()}): ${errorBody.take(220)}"
            httpException != null ->
                "Gemini chat failed (${httpException.code()}). Check the model name and API key."
            message.isNullOrBlank() -> "AI chat failed. Please try again."
            else -> message.orEmpty()
        }
    }
}

data class MedicalChatTurn(
    val text: String,
    val isUser: Boolean
)
