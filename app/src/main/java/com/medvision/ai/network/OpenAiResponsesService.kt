package com.medvision.ai.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenAiResponsesService {
    @POST("v1/responses")
    suspend fun createResponse(@Body request: OpenAiRequest): OpenAiResponse
}

@Serializable
data class OpenAiRequest(
    val model: String,
    val input: JsonElement,
    val text: TextConfig? = null
) {
    @Serializable
    data class TextConfig(
        val format: FormatConfig
    )

    @Serializable
    data class FormatConfig(
        val type: String
    )
}

@Serializable
data class OpenAiResponse(
    val output: List<OpenAiOutput>? = null
)

@Serializable
data class OpenAiOutput(
    val content: List<OpenAiContent>? = null
)

@Serializable
data class OpenAiContent(
    val type: String? = null,
    val text: String? = null,
    @SerialName("refusal") val refusal: String? = null
)
