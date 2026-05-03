package com.medvision.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medvision.ai.data.model.HistoryItem
import com.medvision.ai.data.model.HistoryType
import com.medvision.ai.data.model.ScanComparisonResult
import com.medvision.ai.data.repository.DetectionRepository
import com.medvision.ai.data.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class ScanComparisonUiState(
    val earlierImagePath: String? = null,
    val newerImagePath: String? = null,
    val result: ScanComparisonResult? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ScanComparisonViewModel(
    private val detectionRepository: DetectionRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScanComparisonUiState())
    val uiState: StateFlow<ScanComparisonUiState> = _uiState.asStateFlow()

    fun setEarlierImage(path: String) {
        _uiState.update {
            it.copy(
                earlierImagePath = path,
                result = null,
                error = null
            )
        }
    }

    fun setNewerImage(path: String) {
        _uiState.update {
            it.copy(
                newerImagePath = path,
                result = null,
                error = null
            )
        }
    }

    fun setError(message: String) {
        _uiState.update { it.copy(error = message, isLoading = false) }
    }

    fun compare() {
        val earlier = _uiState.value.earlierImagePath
        val newer = _uiState.value.newerImagePath
        if (earlier.isNullOrBlank() || newer.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Please select both earlier and newer images.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    result = null
                )
            }

            val result = withTimeoutOrNull(45_000) {
                detectionRepository.compareImages(earlier, newer)
            } ?: Result.failure(IllegalStateException("Image comparison timed out. Try clearer photos with similar lighting and angle."))

            val comparison = result.getOrNull()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    result = comparison,
                    error = result.exceptionOrNull()?.message
                )
            }

            if (comparison != null) {
                historyRepository.addHistory(
                    HistoryItem(
                        type = HistoryType.COMPARISON,
                        imagePath = newer,
                        resultTitle = "Comparison: ${comparison.trend}",
                        resultDetail = buildString {
                            append("Confidence ${comparison.confidence}%")
                            if (comparison.summary.isNotBlank()) {
                                append("\n")
                                append(comparison.summary)
                            }
                            if (comparison.suggestedAction.isNotBlank()) {
                                append("\n")
                                append(comparison.suggestedAction)
                            }
                        }
                    )
                )
            }
        }
    }
}
