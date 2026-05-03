package com.medvision.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medvision.ai.data.repository.MedicalChatRepository
import com.medvision.ai.data.repository.MedicalChatTurn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MedicalChatUiState(
    val input: String = "",
    val messages: List<MedicalChatTurn> = listOf(
        MedicalChatTurn(
            text = "Hi, I can help you understand symptoms, possible next steps, and common medicine categories. Tell me what is bothering you, how long it has been happening, and your age.",
            isUser = false
        )
    ),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MedicalChatViewModel(
    private val repository: MedicalChatRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MedicalChatUiState())
    val uiState: StateFlow<MedicalChatUiState> = _uiState.asStateFlow()

    fun updateInput(value: String) {
        _uiState.update { it.copy(input = value, error = null) }
    }

    fun sendMessage() {
        val message = _uiState.value.input.trim()
        if (message.isBlank() || _uiState.value.isLoading) return

        val previousMessages = _uiState.value.messages
        _uiState.update {
            it.copy(
                input = "",
                messages = it.messages + MedicalChatTurn(text = message, isUser = true),
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            val result = repository.sendMessage(
                userMessage = message,
                previousMessages = previousMessages
            )
            _uiState.update { state ->
                result.fold(
                    onSuccess = { reply ->
                        state.copy(
                            messages = state.messages + MedicalChatTurn(text = reply, isUser = false),
                            isLoading = false
                        )
                    },
                    onFailure = { error ->
                        state.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                )
            }
        }
    }
}
