package com.solclaw.app.chat

import java.util.UUID

enum class MessageSender {
    USER, AGENT
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val sender: MessageSender,
    val timestamp: Long = System.currentTimeMillis()
)
