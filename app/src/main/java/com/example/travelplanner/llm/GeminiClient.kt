package com.example.travelplanner.llm

import com.example.travelplanner.domain.LLMActivitySuggestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import kotlin.math.min

class GeminiClient(
    private val apiKeyProvider: () -> String?
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun enrichItinerary(model: String, prompt: String): List<LLMActivitySuggestion> = withContext(Dispatchers.IO) {
        val key = apiKeyProvider()?.trim().orEmpty()
        if (key.isEmpty()) throw IllegalStateException("Gemini API key missing")

        val payload = """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": ${json.encodeToString(String.serializer(), prompt)}
                    }
                  ]
                }
              ],
              "generationConfig": {
                "temperature": 0.7,
                "maxOutputTokens": 2048
              }
            }
        """.trimIndent()

        val req = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$key")
            .addHeader("Content-Type", "application/json")
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()

        val resp = client.newCall(req).execute()
        val body = resp.body?.string().orEmpty()
        if (!resp.isSuccessful) {
            throw IllegalStateException("Gemini API failed: ${resp.code} $body")
        }

        android.util.Log.d("GeminiClient", "Raw response length: ${body.length}")
        val extracted = extractContent(body)
        android.util.Log.d("GeminiClient", "Extracted content: ${extracted.take(300)}...")
        val suggestions = parseSuggestions(extracted)
        android.util.Log.d("GeminiClient", "Parsed ${suggestions.size} suggestions")
        suggestions
    }

    private fun extractContent(rawJson: String): String {
        val parsed = runCatching {
            json.decodeFromString(GeminiResponse.serializer(), rawJson)
        }.getOrNull()

        return parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: rawJson
    }

    private fun parseSuggestions(text: String): List<LLMActivitySuggestion> {
        val trimmed = text.trim()

        var jsonPart = trimmed

        if (trimmed.contains("```json")) {
            val start = trimmed.indexOf("```json") + 7
            val end = trimmed.indexOf("```", start)
            if (end > start) {
                jsonPart = trimmed.substring(start, end).trim()
            }
        } else if (trimmed.contains("```")) {
            val start = trimmed.indexOf("```") + 3
            val end = trimmed.indexOf("```", start)
            if (end > start) {
                jsonPart = trimmed.substring(start, end).trim()
            }
        }

        val arrayStart = jsonPart.indexOf('[')
        val arrayEnd = jsonPart.lastIndexOf(']')

        if (arrayStart < 0 || arrayEnd <= arrayStart) {
            throw IllegalStateException("Could not find JSON array in response. Response: ${text.take(200)}")
        }

        jsonPart = jsonPart.substring(arrayStart, arrayEnd + 1)

        return try {
            val list = json.decodeFromString(ListSerializer(LLMActivitySuggestion.serializer()), jsonPart)
            list.take(min(list.size, 60))
        } catch (e: Exception) {
            throw IllegalStateException("Could not parse JSON from LLM response: ${e.message}\nJSON: ${jsonPart.take(200)}")
        }
    }

    @Serializable
    private data class GeminiResponse(
        val candidates: List<GeminiCandidate> = emptyList()
    )

    @Serializable
    private data class GeminiCandidate(
        val content: GeminiContent
    )

    @Serializable
    private data class GeminiContent(
        val parts: List<GeminiPart> = emptyList()
    )

    @Serializable
    private data class GeminiPart(
        val text: String
    )
}
