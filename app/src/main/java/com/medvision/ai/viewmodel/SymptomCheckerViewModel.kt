package com.medvision.ai.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medvision.ai.data.model.AnalysisResult
import com.medvision.ai.data.model.HistoryItem
import com.medvision.ai.data.model.HistoryType
import com.medvision.ai.data.repository.AiRepository
import com.medvision.ai.data.repository.HistoryRepository
import com.medvision.ai.data.repository.NoInternetException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

data class SymptomCheckerUiState(
    val symptoms: String = "",
    val result: AnalysisResult? = null,
    val isLoading: Boolean = false,
    val retryCount: Int = 0,
    val statusMessage: String? = null,
    val error: String? = null
)

class SymptomCheckerViewModel(
    private val aiRepository: AiRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SymptomCheckerUiState())
    val uiState: StateFlow<SymptomCheckerUiState> = _uiState.asStateFlow()

    fun updateSymptoms(value: String) = _uiState.update {
        it.copy(symptoms = value, error = null, statusMessage = null)
    }

    fun analyze() {
        if (_uiState.value.isLoading) return

        val input = _uiState.value.symptoms.trim()
        if (input.isBlank()) {
            _uiState.update { it.copy(error = "Please describe your symptoms.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    retryCount = 0,
                    statusMessage = "Analyzing...",
                    error = null,
                    result = null
                )
            }

            try {
                val analysis = withTimeout(ANALYSIS_TIMEOUT_MS) {
                    requestSymptomAnalysis(input)
                }
                Log.d(TAG, "Symptom analysis succeeded: $analysis")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        retryCount = 0,
                        statusMessage = null,
                        result = analysis,
                        error = null
                    )
                }
                saveSymptomHistorySafely(input, analysis)
            } catch (failure: Throwable) {
                Log.e(TAG, "Symptom analysis failed; showing fallback", failure)
                val fallback = fallbackResponse(input)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        retryCount = 0,
                        statusMessage = "Showing basic guidance while AI is unavailable.",
                        result = fallback,
                        error = failure.toSymptomErrorMessage()
                    )
                }
                saveSymptomHistorySafely(input, fallback)
            }
        }
    }

    private suspend fun requestSymptomAnalysis(input: String): AnalysisResult {
        var finalFailure: Throwable? = null

        repeat(MAX_RETRY_ATTEMPTS + 1) { attempt ->
            if (attempt > 0) {
                _uiState.update {
                    it.copy(
                        retryCount = attempt,
                        statusMessage = "Server is busy. Retrying ($attempt/$MAX_RETRY_ATTEMPTS)...",
                        error = null
                    )
                }
                delay(attempt * RETRY_DELAY_STEP_MS)
            }

            val result = aiRepository.analyzeSymptoms(input)
            result.getOrNull()?.let { analysis ->
                Log.d(TAG, "Received symptom analysis on attempt ${attempt + 1}: $analysis")
                return analysis
            }

            val failure = result.exceptionOrNull()
            finalFailure = failure
            Log.w(TAG, "Symptom analysis failed on attempt ${attempt + 1}", failure)

            if (failure?.isRetryableFailure() != true) {
                throw failure ?: IllegalStateException("AI symptom analysis failed.")
            }
        }

        throw finalFailure ?: IllegalStateException("AI symptom analysis failed.")
    }

    private suspend fun saveSymptomHistory(input: String, analysis: AnalysisResult) {
        historyRepository.addHistory(
            HistoryItem(
                type = HistoryType.SYMPTOM,
                inputText = input,
                resultTitle = analysis.conditions.joinToString(),
                resultDetail = "${analysis.riskLevel} risk - ${analysis.advice}"
            )
        )
    }

    private suspend fun saveSymptomHistorySafely(input: String, analysis: AnalysisResult) {
        runCatching {
            saveSymptomHistory(input, analysis)
        }.onFailure { failure ->
            Log.w(TAG, "Failed to save symptom analysis history", failure)
        }
    }

    private fun fallbackResponse(symptoms: String): AnalysisResult {
        return AnalysisResult(
            conditions = listOf("Stress or fatigue", "Dehydration", "Minor infection"),
            riskLevel = "Low",
            advice = "For symptoms like $symptoms, common causes may include stress, dehydration, or minor infections. Rest, hydration, and monitoring symptoms is recommended. This is not medical advice. Consult a doctor."
        )
    }

    private fun Throwable?.toSymptomErrorMessage(): String {
        if (this == null) return "AI symptom analysis failed. Please try again."

        val rawMessage = message.orEmpty()
        return when {
            isNoInternetFailure() ->
                "No internet connection"
            this is TimeoutCancellationException ->
                "The request timed out. Showing basic guidance instead."
            rawMessage.contains("HTTP 429", ignoreCase = true) ->
                "Server is busy. Please try again in a moment."
            rawMessage.contains("HTTP 503", ignoreCase = true) ||
                rawMessage.contains("temporarily busy", ignoreCase = true) ||
                rawMessage.contains("UNAVAILABLE", ignoreCase = true) ->
                "Server is busy. Please try again in a moment."
            rawMessage.contains("timeout", ignoreCase = true) ||
                rawMessage.contains("timed out", ignoreCase = true) ->
                "The request timed out. Please try again."
            rawMessage.contains("network", ignoreCase = true) ->
                "Network request failed. Please check your connection."
            rawMessage.isBlank() -> "AI symptom analysis failed. Please try again."
            else -> rawMessage
        }
    }

    private fun Throwable.isRetryableFailure(): Boolean {
        val httpException = findCause<HttpException>()
        return when {
            findCause<SocketTimeoutException>() != null -> true
            findCause<IOException>() != null && !isNoInternetFailure() -> true
            httpException?.code() == 429 -> true
            httpException?.code() in 500..599 -> true
            message.orEmpty().contains("temporarily busy", ignoreCase = true) -> true
            message.orEmpty().contains("UNAVAILABLE", ignoreCase = true) -> true
            message.orEmpty().contains("Server is busy", ignoreCase = true) -> true
            else -> false
        }
    }

    private fun Throwable.isNoInternetFailure(): Boolean {
        return findCause<NoInternetException>() != null ||
            message.orEmpty().equals("No internet connection", ignoreCase = true)
    }

    private inline fun <reified T : Throwable> Throwable.findCause(): T? {
        var current: Throwable? = this
        while (current != null) {
            if (current is T) return current
            current = current.cause
        }
        return null
    }

    private companion object {
        const val TAG = "SymptomCheckerVM"
        const val MAX_RETRY_ATTEMPTS = 1
        const val RETRY_DELAY_STEP_MS = 1_000L
        const val ANALYSIS_TIMEOUT_MS = 15_000L
    }
}
