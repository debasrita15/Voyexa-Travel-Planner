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

        val destLower = destination.trim().lowercase()

        // Destination-aware activity pools
        val morningActivities = when {
            destLower.contains("pondi") -> listOf("Promenade Beach Morning Walk", "Auroville Matrimandir Visit", "French Quarter Architecture Tour", "Botanical Garden Exploration")
            destLower.contains("goa") -> listOf("Fort Aguada Sightseeing", "Dudhsagar Waterfalls Tour", "Anjuna Flea Market Walk", "Old Goa Churches Tour")
            destLower.contains("paris") -> listOf("Eiffel Tower Summit & Gardens", "Louvre Museum Highlights", "Montmartre Art Walk", "Musée d'Orsay Tour")
            destLower.contains("tokyo") -> listOf("Senso-ji Temple & Asakusa", "Shibuya Crossing & Hachiko", "Meiji Shrine Morning Walk", "Tsukiji Outer Market Food Tour")
            destLower.contains("jaipur") -> listOf("Amber Fort Elephant Ride & Palace", "Hawa Mahal & City Palace Tour", "Nahargarh Fort Viewpoint", "Jantar Mantar Observatory")
            destLower.contains("bali") -> listOf("Ubud Monkey Forest Walk", "Tegallalang Rice Terraces Tour", "Ulun Danu Beratan Temple", "Kanto Lampo Waterfall")
            destLower.contains("london") -> listOf("Big Ben & Westminster Abbey Walk", "Tower of London & Tower Bridge", "British Museum Treasures", "Hyde Park Stroll")
            destLower.contains("york") -> listOf("Central Park Stroll & Conservatory", "Statue of Liberty & Ellis Island", "Metropolitan Museum of Art", "Top of the Rock Observation Deck")
            else -> listOf("$destination Heritage & Historic Center Tour", "$destination Scenic Viewpoint Walk", "$destination Famous Local Landmark Tour", "$destination Botanical & Cultural Park Visit")
        }.shuffled()

        val lunchActivities = when {
            destLower.contains("pondi") -> listOf("French Bistro Lunch at Cafe Des Arts", "Creole & Tamil Fusion Lunch", "Seaside Seafood Lunch")
            destLower.contains("goa") -> listOf("Goan Fish Curry & Rice at Beach Shack", "Portuguese Heritage Lunch at Panaji", "Spice Plantation Buffet Lunch")
            destLower.contains("paris") -> listOf("Classic Parisian Brasserie Lunch", "Baguette & Cheese Picnic at Tuileries", "Le Marais Cafe Lunch")
            destLower.contains("tokyo") -> listOf("Ramen Street Tasting at Tokyo Station", "Sushi Conveyor Lunch at Ginza", "Tempura & Udon Lunch")
            destLower.contains("jaipur") -> listOf("Traditional Rajasthani Thali Lunch", "Dal Baati Churma Special", "Rooftop Heritage Lunch")
            destLower.contains("bali") -> listOf("Balinese Nasi Campur at Local Warung", "Organic Farm-to-Table Lunch in Ubud", "Beachfront Coconut & Seafood Lunch")
            else -> listOf("Authentic $destination Local Cuisine Lunch", "Family-Friendly Central Restaurant", "Top-Rated Local Food Tasting")
        }.shuffled()

        val eveningActivities = when {
            destLower.contains("pondi") -> listOf("Chunnambar Boat Ride to Paradise Beach", "Rock Beach Sunset & Craft Bazaar", "Serenity Beach Surfing & Relaxing")
            destLower.contains("goa") -> listOf("Baga Beach Sunset & Watersports", "Mandovi River Cruise with Live Folk Music", "Panaji Fontainhas Night Walk")
            destLower.contains("paris") -> listOf("Seine River Sunset Cruise", "Champs-Élysées Evening Stroll", "Latin Quarter Bistro Dinner")
            destLower.contains("tokyo") -> listOf("Shinjuku Omoide Yokocho Food Alley", "Akihabara Tech & Anime Exploration", "Roppongi Hills Sunset Skyline View")
            destLower.contains("jaipur") -> listOf("Chokhi Dhani Cultural Evening & Village", "Johari Bazaar Handicraft & Textile Shopping", "Jal Mahal Sunset View")
            destLower.contains("bali") -> listOf("Tanah Lot Temple Sunset View", "Jimbaran Beach Seafood Dinner at Sunset", "Kuta Beach Sunset Stroll")
            else -> listOf("$destination Evening Market & Handicraft Shopping", "$destination Riverfront & Sunset Walk", "$destination Cultural Night Performance")
        }.shuffled()

        for (i in 0 until daysCount) {
            val date = Dates.addDays(startDateEpochMs, i)
            val arrivalDay = i == 0
            val cityArea = if (arrivalDay) "$destination Arrival & Central Area" else "$destination Day ${i + 1} Circuit"

            val commuteMinutes = if (arrivalDay) 35 else 50
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

            val morningTitle = if (arrivalDay) "Hotel Check-in & Light Neighborhood Walk" else morningActivities[i % morningActivities.size]
            val lunchTitle = lunchActivities[i % lunchActivities.size]
            val eveningTitle = if (arrivalDay) "Relaxed Welcome Dinner near Hotel" else eveningActivities[i % eveningActivities.size]

            // Morning
            items.add(
                ActivityItemEntity(
                    id = UUID.randomUUID().toString(),
                    itineraryDayId = dayId,
                    title = morningTitle,
                    category = if (arrivalDay) "relaxed" else "sightseeing",
                    timeSlot = "Morning (9:00 AM - 12:30 PM)",
                    estimatedCost = if (arrivalDay) 300.0 else 1200.0,
                    commuteMinutes = if (arrivalDay) 20 else 35,
                    notes = if (arrivalDay) "Smooth arrival, check in and freshen up" else "Start early to beat the crowd and capture best light",
                    sortIndex = 0
                )
            )

            // Afternoon Lunch
            items.add(
                ActivityItemEntity(
                    id = UUID.randomUUID().toString(),
                    itineraryDayId = dayId,
                    title = lunchTitle,
                    category = "food",
                    timeSlot = "Afternoon (1:00 PM - 2:30 PM)",
                    estimatedCost = 900.0,
                    commuteMinutes = 15,
                    notes = "Enjoy authentic local culinary specialties",
                    sortIndex = 1
                )
            )

            // Rest Block
            items.add(
                ActivityItemEntity(
                    id = UUID.randomUUID().toString(),
                    itineraryDayId = dayId,
                    title = if (settings.kidNapRequired) "Rest & Afternoon Recharge Window" else "Leisure & Hotel Relax Block",
                    category = "rest",
                    timeSlot = "Afternoon (2:30 PM - 4:30 PM)",
                    estimatedCost = 0.0,
                    commuteMinutes = 10,
                    notes = "Avoid peak heat and recharge energy for evening activities",
                    sortIndex = 2
                )
            )

            // Evening
            items.add(
                ActivityItemEntity(
                    id = UUID.randomUUID().toString(),
                    itineraryDayId = dayId,
                    title = eveningTitle,
                    category = "relaxed",
                    timeSlot = "Evening (5:00 PM - 8:30 PM)",
                    estimatedCost = 1400.0,
                    commuteMinutes = 20,
                    notes = "Soak in the sunset vibes and local evening culture",
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
