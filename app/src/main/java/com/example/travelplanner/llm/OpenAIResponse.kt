package com.example.travelplanner.llm

import com.example.travelplanner.domain.LLMActivitySuggestion
import kotlinx.serialization.Serializable

@Serializable
data class OpenAIResponse(
    val output: List<OpenAIOutputBlock> = emptyList()
)

@Serializable
data class OpenAIOutputBlock(
    val content: List<OpenAIContentBlock> = emptyList()
)

@Serializable
data class OpenAIContentBlock(
    val type: String? = null,
    val text: String? = null
)

@Serializable
data class SuggestionsWrapper(
    val suggestions: List<LLMActivitySuggestion>
)
