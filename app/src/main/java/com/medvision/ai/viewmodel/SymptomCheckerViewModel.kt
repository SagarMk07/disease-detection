package com.medvision.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medvision.ai.data.model.AnalysisResult
import com.medvision.ai.data.model.HistoryItem
import com.medvision.ai.data.model.HistoryType
import com.medvision.ai.data.repository.AiRepository
import com.medvision.ai.data.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SymptomCheckerUiState(
    val symptoms: String = "",
    val result: AnalysisResult? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class SymptomCheckerViewModel(
    private val aiRepository: AiRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SymptomCheckerUiState())
    val uiState: StateFlow<SymptomCheckerUiState> = _uiState.asStateFlow()

    fun updateSymptoms(value: String) = _uiState.update { it.copy(symptoms = value, error = null) }

    fun analyze() {
        val input = _uiState.value.symptoms.trim()
        if (input.isBlank()) {
            _uiState.update { it.copy(error = "Please describe your symptoms.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = aiRepository.analyzeSymptoms(input)
            val analysis = result.getOrNull()
            if (analysis != null) {
                historyRepository.addHistory(
                    HistoryItem(
                        type = HistoryType.SYMPTOM,
                        inputText = input,
                        resultTitle = analysis.conditions.joinToString(),
                        resultDetail = "${analysis.riskLevel} risk • ${analysis.advice}"
                    )
                )
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    result = analysis,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }
}
