package com.example.data.ai

data class AIRequest(
    val prompt: String,
    val systemInstruction: String? = null,
    val model: String? = null,
    val temperature: Float = 0.7f,
    val isThinkingEnabled: Boolean = false,
    val contextHistory: List<Pair<String, String>> = emptyList() // role to content
)

data class AIStreamChunk(
    val text: String,
    val thinkingText: String? = null,
    val isDone: Boolean = false,
    val errorMessage: String? = null
)

sealed class AIResult {
    data class Success(val text: String, val thinkingProcess: String? = null) : AIResult()
    data class Error(val message: String) : AIResult()
}
