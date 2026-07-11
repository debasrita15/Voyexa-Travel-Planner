package com.example.travelplanner.domain

data class TripSuggestion(
    val destination: String,
    val type: TripType,
    val bestMonths: List<Int>,
    val approxBudgetMin: Double,
    val approxBudgetMax: Double,
    val tags: List<String>
)
