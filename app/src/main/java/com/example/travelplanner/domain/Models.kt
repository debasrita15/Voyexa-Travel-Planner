package com.example.travelplanner.domain

import kotlinx.serialization.Serializable

enum class TripType { DOMESTIC, INTERNATIONAL }
enum class TripStatus { DRAFT, PLANNED, COMPLETED }
enum class TravelPace { LOW, MEDIUM, HIGH }
enum class BudgetCategory { TRANSPORT, STAY, FOOD, LOCAL_COMMUTE, TICKETS, BUFFER }
enum class PackingCategory { ESSENTIALS, CLOTHING, FOOD, HEALTH, ENTERTAINMENT, DOCUMENTS }
enum class LLMProviderType { LOCAL, OPENAI, GROQ, GEMINI, ANTHROPIC, MCP }

@Serializable
data class TripExportPayload(
    val title: String,
    val destination: String,
    val startDateEpochMs: Long,
    val endDateEpochMs: Long,
    val type: TripType,
    val budgetCap: Double
)

data class TransportRecommendation(
    val from: String,
    val to: String,
    val distanceKm: Int,
    val recommendedMode: String,
    val reason: String
)

@Serializable
data class LLMActivitySuggestion(
    val dayDateEpochMs: Long? = null,
    val timeSlot: String,
    val title: String,
    val notes: String
)
