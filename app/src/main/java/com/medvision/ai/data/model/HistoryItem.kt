package com.medvision.ai.data.model

data class HistoryItem(
    val id: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: HistoryType = HistoryType.SYMPTOM,
    val inputText: String = "",
    val imagePath: String? = null,
    val resultTitle: String = "",
    val resultDetail: String = ""
)

enum class HistoryType {
    SYMPTOM,
    SCAN,
    COMPARISON
}
