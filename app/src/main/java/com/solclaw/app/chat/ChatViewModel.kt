package com.solclaw.app.chat

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val agentName: String = "SolClaw Agent",
    val agentStatus: AgentStatus = AgentStatus.ONLINE
)

class ChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        // Welcome message
        _uiState.update { state ->
            state.copy(
                messages = listOf(
                    ChatMessage(
                        content = "Hey! I'm your SolClaw agent. What can I do for you?",
                        sender = MessageSender.AGENT
                    )
                )
            )
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        // Add user message
        _uiState.update { state ->
            state.copy(
                messages = state.messages + ChatMessage(
                    content = content.trim(),
                    sender = MessageSender.USER
                ),
                agentStatus = AgentStatus.THINKING
            )
        }

        // Simulate agent response (placeholder — no backend yet)
        _uiState.update { state ->
            state.copy(
                messages = state.messages + ChatMessage(
                    content = "I heard you! Backend coming soon. ⚔️",
                    sender = MessageSender.AGENT
                ),
                agentStatus = AgentStatus.ONLINE
            )
        }
    }
}
