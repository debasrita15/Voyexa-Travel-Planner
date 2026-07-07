package com.example.travelplanner.services

import com.example.travelplanner.data.db.ActivityItemEntity
import com.example.travelplanner.data.db.ItineraryDayEntity
import com.example.travelplanner.data.prefs.AppSettings
import com.example.travelplanner.domain.TripType
import com.example.travelplanner.util.Dates
import java.util.UUID

class ItineraryService {

    fun restBufferMinutes(settings: AppSettings): Int {
        var buffer = 90
        if (settings.kidAgeYears <= 3) buffer += 45
        if (settings.kidNapRequired) buffer += 30

        buffer = when (settings.travelPace) {
            com.example.travelplanner.domain.TravelPace.LOW -> buffer + 30
            com.example.travelplanner.domain.TravelPace.MEDIUM -> buffer
            com.example.travelplanner.domain.TravelPace.HIGH -> maxOf(60, buffer - 30)
        }

        return buffer.coerceAtMost(240)
    }

    fun generate(
        tripId: String,
        destination: String,
        startDateEpochMs: Long,
        endDateEpochMs: Long,
        type: TripType,
        settings: AppSettings
    ): Pair<List<ItineraryDayEntity>, List<ActivityItemEntity>> {
        val daysCount = Dates.durationDays(startDateEpochMs, endDateEpochMs)
        val restBuffer = restBufferMinutes(settings)

        val days = mutableListOf<ItineraryDayEntity>()
        val items = mutableListOf<ActivityItemEntity>()

        for (i in 0 until daysCount) {
            val date = Dates.addDays(startDateEpochMs, i)
            val arrivalDay = i == 0
            val cityArea = if (arrivalDay) "$destination Arrival Area" else "$destination Core Area"

            val commuteMinutes = if (arrivalDay) 45 else 60
            val kidScore = kidFriendlinessScore(settings, commuteMinutes, hasRestBlock = true, arrivalDay = arrivalDay)

            val dayId = UUID.randomUUID().toString()
            val day = ItineraryDayEntity(
                id = dayId,
                tripId = tripId,
                dateEpochMs = date,
                cityArea = cityArea,
                restBufferMinutes = restBuffer,
                kidFriendlinessScore = kidScore
            )
            days.add(day)

            items.add(
                ActivityItemEntity(
                    id = UUID.randomUUID().toString(),
                    itineraryDayId = dayId,
                    title = if (arrivalDay) "Light walk and nearby cafe" else "Main sightseeing spot",
                    category = if (arrivalDay) "relaxed" else "sightseeing",
                    timeSlot = "Morning",
                    estimatedCost = if (arrivalDay) 500.0 else 1500.0,
                    commuteMinutes = if (arrivalDay) 20 else 40,
                    notes = if (arrivalDay) "Keep it easy on arrival day" else "Avoid peak rush, start early",
                    sortIndex = 0
                )
            )

            items.add(
                ActivityItemEntity(
                    id = UUID.randomUUID().toString(),
                    itineraryDayId = dayId,
                    title = "Lunch at family friendly place",
                    category = "food",
                    timeSlot = "Afternoon",
                    estimatedCost = 1000.0,
                    commuteMinutes = 15,
                    notes = "Prefer places with high chair or kid menu",
                    sortIndex = 1
                )
            )

            items.add(
                ActivityItemEntity(
                    id = UUID.randomUUID().toString(),
                    itineraryDayId = dayId,
                    title = if (settings.kidNapRequired) "Nap and rest break" else "Hotel rest break",
                    category = "rest",
                    timeSlot = "Afternoon",
                    estimatedCost = 0.0,
                    commuteMinutes = 10,
                    notes = "Protect rest window to reduce crankiness",
                    sortIndex = 2
                )
            )

            items.add(
                ActivityItemEntity(
                    id = UUID.randomUUID().toString(),
                    itineraryDayId = dayId,
                    title = if (arrivalDay) "Dinner nearby hotel" else "Easy evening activity and dinner",
                    category = "relaxed",
                    timeSlot = "Evening",
                    estimatedCost = 1200.0,
                    commuteMinutes = 20,
                    notes = "Keep commute short in evening",
                    sortIndex = 3
                )
            )
        }

        return days to items
    }

    private fun kidFriendlinessScore(
        settings: AppSettings,
        estimatedCommuteMinutes: Int,
        hasRestBlock: Boolean,
        arrivalDay: Boolean
    ): Int {
        var score = settings.kidFriendlyPreference

        if (estimatedCommuteMinutes > 90) score -= 20
        if (estimatedCommuteMinutes > 150) score -= 15

        if (hasRestBlock) score += 10
        if (arrivalDay) score += 5

        if (settings.kidAgeYears <= 3) score -= 5

        return score.coerceIn(0, 100)
    }
}
