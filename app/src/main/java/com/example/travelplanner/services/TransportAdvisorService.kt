package com.example.travelplanner.services

import android.content.Context
import android.location.Geocoder
import com.example.travelplanner.domain.TransportRecommendation
import com.example.travelplanner.util.Geo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class TransportAdvisorService(private val context: Context) {

    suspend fun recommend(fromCity: String, toCity: String): TransportRecommendation = withContext(Dispatchers.IO) {
        val geocoder = Geocoder(context, Locale.getDefault())

        val from = geocoder.getFromLocationName(fromCity, 1)?.firstOrNull()
        val to = geocoder.getFromLocationName(toCity, 1)?.firstOrNull()

        if (from == null || to == null) {
            return@withContext TransportRecommendation(
                from = fromCity,
                to = toCity,
                distanceKm = 0,
                recommendedMode = "Unknown",
                reason = "Could not geocode cities. Please try a more specific name."
            )
        }

        val km = Geo.distanceKm(from.latitude, from.longitude, to.latitude, to.longitude)

        val (mode, reason) = when {
            km <= 250 -> "Train or Bus" to "Short distance, ground travel is cheaper and simpler for family travel."
            km <= 800 -> "Train preferred, Flight optional" to "Medium distance, train is comfortable and cost effective. Flight can be used if time is tight."
            else -> "Flight" to "Long distance, flight reduces travel time and fatigue, especially with a kid."
        }

        TransportRecommendation(
            from = fromCity,
            to = toCity,
            distanceKm = km,
            recommendedMode = mode,
            reason = reason
        )
    }
}
