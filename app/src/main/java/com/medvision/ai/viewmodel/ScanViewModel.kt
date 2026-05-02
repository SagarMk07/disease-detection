package com.medvision.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medvision.ai.data.model.DetectionResult
import com.medvision.ai.data.model.HistoryItem
import com.medvision.ai.data.model.HistoryType
import com.medvision.ai.data.repository.DetectionRepository
import com.medvision.ai.data.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class ScanUiState(
    val latestImagePath: String? = null,
    val result: DetectionResult? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ScanViewModel(
    private val detectionRepository: DetectionRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun setError(message: String) {
        _uiState.update { it.copy(error = message, isLoading = false) }
    }

    fun analyzeImage(imagePath: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    latestImagePath = imagePath,
                    isLoading = true,
                    error = null
                )
            }
            val result = withTimeoutOrNull(45_000) {
                detectionRepository.analyzeImage(imagePath)
            } ?: Result.failure(IllegalStateException("Visual analysis timed out. Please try again with a clearer, well-lit image."))
            val scan = result.getOrNull()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    result = scan,
                    error = result.exceptionOrNull()?.message
                )
            }
            if (scan != null) {
                viewModelScope.launch {
                    historyRepository.addHistory(
                        HistoryItem(
                            type = HistoryType.SCAN,
                            imagePath = imagePath,
                            resultTitle = scan.possibleCondition,
                            resultDetail = buildString {
                                append("Confidence ${scan.confidence}%")
                                if (scan.summary.isNotBlank()) {
                                    append("\n")
                                    append(scan.summary)
                                }
                            }
                        )
                    )
                }
            }
        }
    }
}
