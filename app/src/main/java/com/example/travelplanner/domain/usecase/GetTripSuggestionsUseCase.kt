package com.example.travelplanner.domain.usecase

import com.example.travelplanner.data.prefs.AppSettings
import com.example.travelplanner.domain.TripSuggestion
import com.example.travelplanner.services.OnlineTripSuggestionService
import com.example.travelplanner.services.TripSuggestionService

interface GetTripSuggestionsUseCase {
    suspend operator fun invoke(
        homeCountry: String,
        month: Int,
        budget: Double,
        currency: String,
        useOnlineApi: Boolean
    ): Result<List<TripSuggestion>>
}

class GetTripSuggestionsUseCaseImpl(
    private val onlineService: OnlineTripSuggestionService,
    private val offlineService: TripSuggestionService
) : GetTripSuggestionsUseCase {

    override suspend fun invoke(
        homeCountry: String,
        month: Int,
        budget: Double,
        currency: String,
        useOnlineApi: Boolean
    ): Result<List<TripSuggestion>> {
        return try {
            val validHomeCountry = homeCountry.ifBlank {
                java.util.Locale.getDefault().displayCountry.ifBlank { "India" }
            }

            val suggestions = if (useOnlineApi) {
                fetchOnlineSuggestions(validHomeCountry, month, budget, currency)
            } else {
                fetchOfflineSuggestions(validHomeCountry, month, budget)
            }

            Result.success(suggestions)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private suspend fun fetchOnlineSuggestions(
        homeCountry: String,
        month: Int,
        budget: Double,
        currency: String
    ): List<TripSuggestion> {
        return try {
            onlineService.getDestinationSuggestions(homeCountry, budget, currency)
                .filter { it.bestMonths.contains(month) }
                .filter { it.approxBudgetMin <= budget }
        } catch (e: Exception) {
            fetchOfflineSuggestions(homeCountry, month, budget)
        }
    }

    private fun fetchOfflineSuggestions(
        homeCountry: String,
        month: Int,
        budget: Double
    ): List<TripSuggestion> {
        val defaultSettings = createDefaultSettings(homeCountry)
        return offlineService.suggestTrips(defaultSettings, month, budget)
    }

    private fun createDefaultSettings(homeCountry: String) = AppSettings(
        year = 2026,
        yearlyBudget = 200000.0,
        reservedBufferPercent = 15.0,
        homeCity = "",
        homeCountry = homeCountry,
        currency = "USD",
        adultsCount = 2,
        kidAgeYears = 5,
        kidNapRequired = true,
        kidFriendlyPreference = 80,
        travelPace = com.example.travelplanner.domain.TravelPace.MEDIUM,
        llmProvider = com.example.travelplanner.domain.LLMProviderType.LOCAL,
        openAIModel = "gpt-4o-mini",
        groqModel = "llama-3.3-70b-versatile",
        geminiModel = "gemini-2.0-flash-exp",
        anthropicModel = "claude-3-5-haiku-20241022",
        mcpProviders = listOf("GROQ", "GEMINI")
    )
}

