package com.example.travelplanner.llm

import com.example.travelplanner.domain.LLMActivitySuggestion
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class MCPAggregatorClient(
    private val groqClient: GroqClient,
    private val geminiClient: GeminiClient,
    private val anthropicClient: AnthropicClient,
    private val openAIClient: OpenAIClient
) {
    suspend fun enrichItinerary(
        providers: List<String>,
        models: Map<String, String>,
        prompt: String
    ): List<LLMActivitySuggestion> = coroutineScope {
        val results = mutableListOf<List<LLMActivitySuggestion>>()

        providers.forEach { provider ->
            val model = models[provider] ?: return@forEach

            try {
                val suggestions = when (provider.uppercase()) {
                    "GROQ" -> groqClient.enrichItinerary(model, prompt)
                    "GEMINI" -> geminiClient.enrichItinerary(model, prompt)
                    "ANTHROPIC" -> anthropicClient.enrichItinerary(model, prompt)
                    "OPENAI" -> openAIClient.enrichItinerary(model, prompt)
                    else -> emptyList()
                }

                if (suggestions.isNotEmpty()) {
                    results.add(suggestions)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        mergeAndScore(results)
    }

    private fun mergeAndScore(results: List<List<LLMActivitySuggestion>>): List<LLMActivitySuggestion> {
        if (results.isEmpty()) return emptyList()
        if (results.size == 1) return results[0]

        val scored = mutableMapOf<String, Pair<LLMActivitySuggestion, Int>>()

        results.forEach { providerResults ->
            providerResults.forEach { suggestion ->
                val key = suggestionKey(suggestion)
                val current = scored[key]

                if (current == null) {
                    scored[key] = suggestion to 1
                } else {
                    val betterSuggestion = selectBetter(current.first, suggestion)
                    scored[key] = betterSuggestion to (current.second + 1)
                }
            }
        }

        return scored.values
            .sortedByDescending { it.second }
            .map { it.first }
            .distinctBy { suggestionKey(it) }
            .sortedWith(
                compareBy<LLMActivitySuggestion> { it.dayDateEpochMs }
                    .thenBy { it.timeSlot }
            )
    }

    private fun suggestionKey(suggestion: LLMActivitySuggestion): String {
        return "${suggestion.dayDateEpochMs}_${suggestion.timeSlot}_${normalizeTitle(suggestion.title)}"
    }

    private fun normalizeTitle(title: String): String {
        return title.lowercase()
            .replace(Regex("[^a-z0-9 ]"), "")
            .trim()
            .split(Regex("\\s+"))
            .take(4)
            .joinToString(" ")
    }

    private fun selectBetter(a: LLMActivitySuggestion, b: LLMActivitySuggestion): LLMActivitySuggestion {
        val scoreA = a.notes.length + a.title.length
        val scoreB = b.notes.length + b.title.length
        return if (scoreB > scoreA) b else a
    }
}
