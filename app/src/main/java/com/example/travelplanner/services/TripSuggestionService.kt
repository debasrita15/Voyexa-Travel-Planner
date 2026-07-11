package com.example.travelplanner.services

import com.example.travelplanner.data.prefs.AppSettings
import com.example.travelplanner.domain.TripSuggestion
import com.example.travelplanner.domain.TripType
import kotlin.math.abs

class TripSuggestionService {

    private fun getSuggestionsForRegion(homeCountry: String): List<TripSuggestion> {
        return when {
            homeCountry.contains("India", ignoreCase = true) -> getIndianSuggestions()
            homeCountry.contains("United States", ignoreCase = true) ||
            homeCountry.contains("USA", ignoreCase = true) ||
            homeCountry.contains("America", ignoreCase = true) -> getUSSuggestions()
            homeCountry.contains("United Kingdom", ignoreCase = true) ||
            homeCountry.contains("UK", ignoreCase = true) ||
            homeCountry.contains("Britain", ignoreCase = true) -> getUKSuggestions()
            homeCountry.contains("Singapore", ignoreCase = true) -> getSingaporeSuggestions()
            homeCountry.contains("Australia", ignoreCase = true) -> getAustraliaSuggestions()
            homeCountry.contains("Canada", ignoreCase = true) -> getCanadaSuggestions()
            else -> getWorldwideSuggestions()
        }
    }

    private fun getIndianSuggestions() = listOf(
        TripSuggestion("Goa", TripType.DOMESTIC, listOf(11,12,1,2), 30000.0, 90000.0, listOf("beach","easy")),
        TripSuggestion("Pondicherry", TripType.DOMESTIC, listOf(10,11,12,1,2), 25000.0, 70000.0, listOf("beach","short")),
        TripSuggestion("Ooty", TripType.DOMESTIC, listOf(3,4,5,6,7), 25000.0, 80000.0, listOf("hill","cool weather")),
        TripSuggestion("Coorg", TripType.DOMESTIC, listOf(10,11,12,1,2), 25000.0, 80000.0, listOf("nature","relaxed")),
        TripSuggestion("Jaipur", TripType.DOMESTIC, listOf(10,11,12,1,2), 30000.0, 90000.0, listOf("culture")),
        TripSuggestion("Kerala", TripType.DOMESTIC, listOf(10,11,12,1,2), 40000.0, 120000.0, listOf("family","backwaters")),
        TripSuggestion("Ladakh", TripType.DOMESTIC, listOf(6,7,8,9), 50000.0, 120000.0, listOf("adventure","mountains")),
        TripSuggestion("Andaman", TripType.DOMESTIC, listOf(10,11,12,1,2,3), 60000.0, 150000.0, listOf("beach","island")),
        TripSuggestion("Singapore", TripType.INTERNATIONAL, listOf(2,3,4,7,8), 180000.0, 350000.0, listOf("kid friendly","easy")),
        TripSuggestion("Dubai", TripType.INTERNATIONAL, listOf(10,11,12,1,2,3), 200000.0, 400000.0, listOf("family","short flight")),
        TripSuggestion("Thailand", TripType.INTERNATIONAL, listOf(11,12,1,2,3), 170000.0, 350000.0, listOf("beach")),
        TripSuggestion("Bali", TripType.INTERNATIONAL, listOf(4,5,6,7,8,9), 200000.0, 420000.0, listOf("beach","relaxed")),
        TripSuggestion("Maldives", TripType.INTERNATIONAL, listOf(11,12,1,2,3,4), 250000.0, 600000.0, listOf("luxury","beach")),
        TripSuggestion("Sri Lanka", TripType.INTERNATIONAL, listOf(11,12,1,2,3), 100000.0, 250000.0, listOf("culture","beach"))
    )

    private fun getUSSuggestions() = listOf(
        TripSuggestion("Hawaii", TripType.DOMESTIC, listOf(4,5,6,7,8,9), 2000.0, 5000.0, listOf("beach","family")),
        TripSuggestion("Florida Keys", TripType.DOMESTIC, listOf(11,12,1,2,3), 1500.0, 4000.0, listOf("beach","relaxed")),
        TripSuggestion("Yellowstone", TripType.DOMESTIC, listOf(6,7,8), 1800.0, 4500.0, listOf("nature","adventure")),
        TripSuggestion("New York City", TripType.DOMESTIC, listOf(4,5,9,10), 2000.0, 5500.0, listOf("city","culture")),
        TripSuggestion("Grand Canyon", TripType.DOMESTIC, listOf(3,4,5,9,10), 1500.0, 3500.0, listOf("nature","adventure")),
        TripSuggestion("Disney World", TripType.DOMESTIC, listOf(1,2,3,10,11,12), 2500.0, 6000.0, listOf("kid friendly","family")),
        TripSuggestion("Mexico (Cancun)", TripType.INTERNATIONAL, listOf(11,12,1,2,3,4), 1500.0, 3500.0, listOf("beach","easy")),
        TripSuggestion("Costa Rica", TripType.INTERNATIONAL, listOf(11,12,1,2,3,4), 2000.0, 4500.0, listOf("nature","adventure")),
        TripSuggestion("Japan", TripType.INTERNATIONAL, listOf(3,4,5,9,10,11), 3500.0, 8000.0, listOf("culture","family")),
        TripSuggestion("Paris, France", TripType.INTERNATIONAL, listOf(4,5,6,9,10), 3000.0, 7000.0, listOf("culture","city")),
        TripSuggestion("London, UK", TripType.INTERNATIONAL, listOf(5,6,7,8,9), 2800.0, 6500.0, listOf("culture","city")),
        TripSuggestion("Caribbean Cruise", TripType.INTERNATIONAL, listOf(11,12,1,2,3), 2200.0, 5500.0, listOf("beach","relaxed"))
    )

    private fun getUKSuggestions() = listOf(
        TripSuggestion("Scottish Highlands", TripType.DOMESTIC, listOf(5,6,7,8,9), 800.0, 2000.0, listOf("nature","scenic")),
        TripSuggestion("Cornwall", TripType.DOMESTIC, listOf(6,7,8), 600.0, 1500.0, listOf("beach","family")),
        TripSuggestion("Lake District", TripType.DOMESTIC, listOf(5,6,7,8,9), 700.0, 1800.0, listOf("nature","relaxed")),
        TripSuggestion("Edinburgh", TripType.DOMESTIC, listOf(5,6,7,8,9), 600.0, 1600.0, listOf("culture","city")),
        TripSuggestion("Paris, France", TripType.INTERNATIONAL, listOf(4,5,6,9,10), 500.0, 1500.0, listOf("culture","city")),
        TripSuggestion("Barcelona, Spain", TripType.INTERNATIONAL, listOf(5,6,9,10), 600.0, 1800.0, listOf("beach","culture")),
        TripSuggestion("Amsterdam", TripType.INTERNATIONAL, listOf(4,5,6,9), 550.0, 1600.0, listOf("culture","family")),
        TripSuggestion("Greek Islands", TripType.INTERNATIONAL, listOf(5,6,7,8,9), 900.0, 2500.0, listOf("beach","relaxed")),
        TripSuggestion("Iceland", TripType.INTERNATIONAL, listOf(6,7,8), 1200.0, 3000.0, listOf("nature","adventure")),
        TripSuggestion("Dubai", TripType.INTERNATIONAL, listOf(11,12,1,2,3), 1200.0, 3500.0, listOf("luxury","family")),
        TripSuggestion("Thailand", TripType.INTERNATIONAL, listOf(11,12,1,2), 1500.0, 3500.0, listOf("beach","culture")),
        TripSuggestion("Morocco", TripType.INTERNATIONAL, listOf(3,4,5,10,11), 800.0, 2200.0, listOf("culture","adventure"))
    )

    private fun getSingaporeSuggestions() = listOf(
        TripSuggestion("Sentosa Island", TripType.DOMESTIC, listOf(1,2,3,4,5,6,7,8,9,10,11,12), 200.0, 600.0, listOf("beach","family")),
        TripSuggestion("Malaysia (JB/KL)", TripType.INTERNATIONAL, listOf(1,2,3,4,5,6,7,8,9,10,11,12), 300.0, 800.0, listOf("easy","shopping")),
        TripSuggestion("Bali, Indonesia", TripType.INTERNATIONAL, listOf(4,5,6,7,8,9), 800.0, 2000.0, listOf("beach","relaxed")),
        TripSuggestion("Phuket, Thailand", TripType.INTERNATIONAL, listOf(11,12,1,2,3), 700.0, 1800.0, listOf("beach","family")),
        TripSuggestion("Tokyo, Japan", TripType.INTERNATIONAL, listOf(3,4,5,9,10,11), 1500.0, 3500.0, listOf("culture","family")),
        TripSuggestion("Seoul, Korea", TripType.INTERNATIONAL, listOf(3,4,5,9,10,11), 1200.0, 3000.0, listOf("culture","shopping")),
        TripSuggestion("Maldives", TripType.INTERNATIONAL, listOf(11,12,1,2,3,4), 2000.0, 5000.0, listOf("luxury","beach")),
        TripSuggestion("Australia (Sydney)", TripType.INTERNATIONAL, listOf(3,4,5,9,10,11), 1800.0, 4000.0, listOf("city","nature")),
        TripSuggestion("Dubai", TripType.INTERNATIONAL, listOf(11,12,1,2,3), 1200.0, 3000.0, listOf("luxury","shopping")),
        TripSuggestion("Vietnam", TripType.INTERNATIONAL, listOf(11,12,1,2,3), 800.0, 2000.0, listOf("culture","food"))
    )

    private fun getAustraliaSuggestions() = listOf(
        TripSuggestion("Gold Coast", TripType.DOMESTIC, listOf(9,10,11,12,1,2,3,4), 800.0, 2000.0, listOf("beach","family")),
        TripSuggestion("Great Barrier Reef", TripType.DOMESTIC, listOf(6,7,8,9,10), 1200.0, 3000.0, listOf("nature","adventure")),
        TripSuggestion("Tasmania", TripType.DOMESTIC, listOf(11,12,1,2,3), 1000.0, 2500.0, listOf("nature","scenic")),
        TripSuggestion("Uluru", TripType.DOMESTIC, listOf(5,6,7,8,9), 1500.0, 3500.0, listOf("nature","cultural")),
        TripSuggestion("Bali, Indonesia", TripType.INTERNATIONAL, listOf(4,5,6,7,8,9), 1200.0, 2800.0, listOf("beach","relaxed")),
        TripSuggestion("New Zealand", TripType.INTERNATIONAL, listOf(11,12,1,2,3), 1500.0, 3500.0, listOf("nature","adventure")),
        TripSuggestion("Fiji", TripType.INTERNATIONAL, listOf(5,6,7,8,9,10), 1800.0, 4000.0, listOf("beach","relaxed")),
        TripSuggestion("Japan", TripType.INTERNATIONAL, listOf(3,4,5,9,10,11), 2500.0, 5500.0, listOf("culture","family")),
        TripSuggestion("Singapore", TripType.INTERNATIONAL, listOf(1,2,3,4,5,6,7,8,9,10,11,12), 1200.0, 2800.0, listOf("city","family")),
        TripSuggestion("Thailand", TripType.INTERNATIONAL, listOf(11,12,1,2,3), 1500.0, 3200.0, listOf("beach","culture"))
    )

    private fun getCanadaSuggestions() = listOf(
        TripSuggestion("Banff", TripType.DOMESTIC, listOf(6,7,8,9), 1200.0, 3000.0, listOf("nature","mountains")),
        TripSuggestion("Vancouver Island", TripType.DOMESTIC, listOf(6,7,8,9), 1000.0, 2500.0, listOf("nature","beach")),
        TripSuggestion("Quebec City", TripType.DOMESTIC, listOf(6,7,8,12,1,2), 800.0, 2000.0, listOf("culture","city")),
        TripSuggestion("Niagara Falls", TripType.DOMESTIC, listOf(5,6,7,8,9), 600.0, 1500.0, listOf("family","scenic")),
        TripSuggestion("USA (New York)", TripType.INTERNATIONAL, listOf(4,5,6,9,10), 800.0, 2200.0, listOf("city","culture")),
        TripSuggestion("Mexico (Cancun)", TripType.INTERNATIONAL, listOf(11,12,1,2,3,4), 1200.0, 2800.0, listOf("beach","relaxed")),
        TripSuggestion("Costa Rica", TripType.INTERNATIONAL, listOf(11,12,1,2,3,4), 1500.0, 3500.0, listOf("nature","adventure")),
        TripSuggestion("Iceland", TripType.INTERNATIONAL, listOf(6,7,8), 2000.0, 4500.0, listOf("nature","adventure")),
        TripSuggestion("France (Paris)", TripType.INTERNATIONAL, listOf(4,5,6,9,10), 2200.0, 5000.0, listOf("culture","city")),
        TripSuggestion("Japan", TripType.INTERNATIONAL, listOf(3,4,5,9,10,11), 2500.0, 5500.0, listOf("culture","family"))
    )

    private fun getWorldwideSuggestions() = listOf(
        TripSuggestion("Paris, France", TripType.INTERNATIONAL, listOf(4,5,6,9,10), 2000.0, 5000.0, listOf("culture","city")),
        TripSuggestion("Tokyo, Japan", TripType.INTERNATIONAL, listOf(3,4,5,9,10,11), 2500.0, 6000.0, listOf("culture","family")),
        TripSuggestion("Bali, Indonesia", TripType.INTERNATIONAL, listOf(4,5,6,7,8,9), 1500.0, 3500.0, listOf("beach","relaxed")),
        TripSuggestion("Dubai, UAE", TripType.INTERNATIONAL, listOf(11,12,1,2,3), 2000.0, 4500.0, listOf("luxury","family")),
        TripSuggestion("London, UK", TripType.INTERNATIONAL, listOf(5,6,7,8,9), 2200.0, 5200.0, listOf("culture","city")),
        TripSuggestion("New York, USA", TripType.INTERNATIONAL, listOf(4,5,9,10), 2500.0, 6000.0, listOf("city","culture")),
        TripSuggestion("Barcelona, Spain", TripType.INTERNATIONAL, listOf(5,6,9,10), 1800.0, 4200.0, listOf("beach","culture")),
        TripSuggestion("Singapore", TripType.INTERNATIONAL, listOf(2,3,4,7,8), 2000.0, 4500.0, listOf("kid friendly","city")),
        TripSuggestion("Thailand (Bangkok/Phuket)", TripType.INTERNATIONAL, listOf(11,12,1,2,3), 1500.0, 3500.0, listOf("beach","culture")),
        TripSuggestion("Maldives", TripType.INTERNATIONAL, listOf(11,12,1,2,3,4), 3000.0, 7000.0, listOf("luxury","beach"))
    )

    fun suggestTrips(settings: AppSettings, month: Int, remainingBudget: Double): List<TripSuggestion> {
        val suggestions = getSuggestionsForRegion(settings.homeCountry)
        val filtered = suggestions.filter { it.bestMonths.contains(month) }
        val within = filtered.filter { remainingBudget >= it.approxBudgetMin }

        return within.sortedWith(compareByDescending<TripSuggestion> { it.tags.contains("kid friendly") }
            .thenBy { s ->
                val mid = (s.approxBudgetMin + s.approxBudgetMax) / 2.0
                abs(remainingBudget - mid)
            })
            .take(10)
    }
}
