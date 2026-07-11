package com.example.travelplanner.services

import com.example.travelplanner.domain.TripSuggestion
import com.example.travelplanner.domain.TripType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class OnlineTripSuggestionService(
    private val fallbackService: TripSuggestionService = TripSuggestionService()
) {

    // Constants
    private companion object {
        const val API_URL = "https://restcountries.com/v3.1/all"
        const val CONNECT_TIMEOUT_SECONDS = 10L
        const val READ_TIMEOUT_SECONDS = 15L
        const val MAX_SUGGESTIONS = 15
        const val MAX_COUNTRIES_TO_PROCESS = 20
        const val BUDGET_FRIENDLY_THRESHOLD = 0.5
        const val PRICE_RANGE_MULTIPLIER = 2.5

        const val BASE_COST_INTERNATIONAL = 2000.0
        const val BASE_COST_DOMESTIC = 800.0
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    suspend fun getDestinationSuggestions(
        homeCountry: String,
        budget: Double,
        currency: String
    ): List<TripSuggestion> = withContext(Dispatchers.IO) {
        try {
            val suggestions = fetchOnlineSuggestions(homeCountry, budget, currency)

            if (suggestions.isNotEmpty()) {
                suggestions.take(MAX_SUGGESTIONS)
            } else {
                getFallbackSuggestions(homeCountry, budget)
            }
        } catch (e: Exception) {
            handleError(e, homeCountry, budget)
        }
    }

    private suspend fun fetchOnlineSuggestions(
        homeCountry: String,
        budget: Double,
        currency: String
    ): List<TripSuggestion> {
        val countries = fetchCountriesFromApi() ?: return emptyList()
        val homeRegion = findHomeRegion(countries, homeCountry)

        return countries
            .filterEligibleCountries(homeCountry)
            .shuffled()
            .take(MAX_COUNTRIES_TO_PROCESS)
            .mapNotNull { country ->
                createTripSuggestion(country, homeRegion, budget, currency)
            }
    }

    private fun fetchCountriesFromApi(): List<RestCountry>? {
        val request = Request.Builder().url(API_URL).build()
        val response = httpClient.newCall(request).execute()

        if (!response.isSuccessful) return null

        val body = response.body?.string() ?: return null
        return json.decodeFromString<List<RestCountry>>(body)
    }

    private fun findHomeRegion(countries: List<RestCountry>, homeCountry: String): String {
        return countries
            .find { it.name.common.contains(homeCountry, ignoreCase = true) }
            ?.region
            ?: "Unknown"
    }

    private fun List<RestCountry>.filterEligibleCountries(homeCountry: String): List<RestCountry> {
        return this
            .filter { !it.name.common.contains(homeCountry, ignoreCase = true) }
            .filter { it.capital.isNotEmpty() }
    }

    private fun createTripSuggestion(
        country: RestCountry,
        homeRegion: String,
        budget: Double,
        currency: String
    ): TripSuggestion? {
        val capital = country.capital.firstOrNull() ?: return null
        val isInternational = country.region != homeRegion
        val tripType = if (isInternational) TripType.INTERNATIONAL else TripType.DOMESTIC

        val (minCost, maxCost) = estimateTripCost(
            isInternational = isInternational,
            currency = currency,
            region = country.region
        )

        if (minCost > budget) return null

        return TripSuggestion(
            destination = "$capital, ${country.name.common}",
            type = tripType,
            bestMonths = getBestMonthsForRegion(country.region, country.subregion),
            approxBudgetMin = minCost,
            approxBudgetMax = maxCost,
            tags = generateTags(country, minCost, budget)
        )
    }

    private fun generateTags(country: RestCountry, minCost: Double, budget: Double): List<String> {
        return buildList {
            when (country.region) {
                "Europe" -> add("culture")
                "Asia" -> add("diverse")
            }

            if (country.subregion.contains("Caribbean") || country.subregion.contains("Pacific")) {
                add("beach")
            }

            if (minCost < budget * BUDGET_FRIENDLY_THRESHOLD) {
                add("budget friendly")
            }
        }
    }

    private fun estimateTripCost(
        isInternational: Boolean,
        currency: String,
        region: String
    ): Pair<Double, Double> {
        val currencyMultiplier = getCurrencyMultiplier(currency)
        val regionMultiplier = getRegionMultiplier(region)
        val baseCost = if (isInternational) BASE_COST_INTERNATIONAL else BASE_COST_DOMESTIC

        val minCost = baseCost * currencyMultiplier * regionMultiplier
        val maxCost = minCost * PRICE_RANGE_MULTIPLIER

        return Pair(minCost, maxCost)
    }

    private fun getCurrencyMultiplier(currency: String): Double = when (currency) {
        "USD" -> 1.0
        "EUR" -> 0.85
        "GBP" -> 0.73
        "INR" -> 83.0
        "AUD" -> 1.5
        "CAD" -> 1.35
        "SGD" -> 1.35
        else -> 1.0
    }

    private fun getRegionMultiplier(region: String): Double = when (region) {
        "Europe" -> 1.2
        "Americas" -> 1.0
        "Asia" -> 0.7
        "Africa" -> 0.8
        "Oceania" -> 1.3
        else -> 1.0
    }

    private fun getBestMonthsForRegion(region: String, subregion: String): List<Int> = when {
        subregion.contains("Caribbean") -> listOf(11, 12, 1, 2, 3, 4)
        region == "Europe" -> listOf(5, 6, 7, 8, 9)
        subregion.contains("South-Eastern Asia") -> listOf(11, 12, 1, 2, 3)
        subregion.contains("Southern Asia") -> listOf(10, 11, 12, 1, 2, 3)
        subregion.contains("Eastern Asia") -> listOf(3, 4, 5, 9, 10, 11)
        region == "Oceania" -> listOf(11, 12, 1, 2, 3)
        subregion.contains("Northern Africa") -> listOf(3, 4, 5, 9, 10, 11)
        else -> (1..12).toList()
    }

    private fun getFallbackSuggestions(homeCountry: String, budget: Double): List<TripSuggestion> {
        val settings = createDefaultSettings(homeCountry)
        return fallbackService.suggestTrips(settings, 1, budget)
    }

    private fun createDefaultSettings(homeCountry: String) =
        com.example.travelplanner.data.prefs.AppSettings(
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

    private fun handleError(error: Exception, homeCountry: String, budget: Double): List<TripSuggestion> {
        error.printStackTrace()
        return getFallbackSuggestions(homeCountry, budget)
    }

    @Serializable
    data class RestCountry(
        val name: CountryName,
        val capital: List<String> = emptyList(),
        val region: String = "",
        val subregion: String = ""
    )

    @Serializable
    data class CountryName(
        val common: String,
        val official: String = ""
    )
}
