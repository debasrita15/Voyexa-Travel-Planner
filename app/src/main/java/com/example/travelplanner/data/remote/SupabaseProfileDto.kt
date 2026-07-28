package com.example.travelplanner.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseProfileDto(
    val id: String,
    @SerialName("home_city") val homeCity: String = "",
    @SerialName("home_country") val homeCountry: String = "",
    val currency: String = "USD",
    @SerialName("yearly_budget") val yearlyBudget: Double = 200000.0,
    @SerialName("avatar_url") val avatarUrl: String = "",
    @SerialName("updated_at_epoch_ms") val updatedAtEpochMs: Long = System.currentTimeMillis()
)
