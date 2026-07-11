package com.example.travelplanner.llm

import com.example.travelplanner.data.db.ActivityItemEntity
import com.example.travelplanner.data.db.ItineraryDayEntity
import com.example.travelplanner.data.db.TripPlanEntity
import com.example.travelplanner.data.prefs.AppSettings

object PromptTemplates {

    fun enrichPrompt(
        trip: TripPlanEntity,
        days: List<ItineraryDayEntity>,
        itemsByDay: Map<String, List<ActivityItemEntity>>,
        settings: AppSettings
    ): String {
        val sb = StringBuilder()
        sb.appendLine("You are a worldwide travel planner assistant with local expertise.")
        sb.appendLine("Return ONLY valid JSON array. No extra text or markdown.")
        sb.appendLine("Schema: [{\"dayDateEpochMs\":123,\"timeSlot\":\"Morning|Afternoon|Evening\",\"title\":\"...\",\"notes\":\"...\"}]")
        sb.appendLine()

        sb.appendLine("=== TRAVELER PROFILE ===")
        sb.appendLine("Home location: ${settings.homeCity}, ${settings.homeCountry}")
        sb.appendLine("Currency: ${settings.currency}")
        sb.appendLine("Adults: ${settings.adultsCount}, Kid age: ${settings.kidAgeYears} years")
        sb.appendLine("Kid nap required: ${settings.kidNapRequired}")
        sb.appendLine("Travel pace preference: ${settings.travelPace.name}")
        sb.appendLine()

        sb.appendLine("=== TRIP DETAILS ===")
        sb.appendLine("Destination: ${trip.destination}")
        sb.appendLine("Trip type: ${trip.type}")
        sb.appendLine("Duration: ${java.util.concurrent.TimeUnit.MILLISECONDS.toDays(trip.endDateEpochMs - trip.startDateEpochMs)} days")
        sb.appendLine("Budget: ${settings.currency} ${trip.budgetCap}")
        sb.appendLine()

        sb.appendLine("=== EXISTING ITINERARY ===")
        for (day in days.sortedBy { it.dateEpochMs }) {
            val dayNum = days.indexOf(day) + 1
            sb.appendLine("Day $dayNum (${day.cityArea}):")
            val items = itemsByDay[day.id].orEmpty().sortedBy { it.sortIndex }
            if (items.isEmpty()) {
                sb.appendLine("  - No activities planned yet")
            } else {
                for (it in items) {
                    sb.appendLine("  - ${it.timeSlot}: ${it.title} (${it.commuteMinutes}m commute) - ${it.notes}")
                }
            }
        }

        sb.appendLine()
        sb.appendLine("=== TASK ===")
        sb.appendLine("Suggest 2-3 LOCAL, culturally appropriate activities per day:")
        sb.appendLine("- Consider the traveler is from ${settings.homeCountry}")
        sb.appendLine("- Suggest authentic ${trip.destination} experiences")
        sb.appendLine("- Include local cuisine, customs, and hidden gems")
        sb.appendLine("- Consider time zones, local holidays, weather patterns")
        sb.appendLine("- Prioritize: kid-friendly (age ${settings.kidAgeYears}), low commute, rest breaks")
        sb.appendLine("- Respect local culture and appropriate visiting hours")
        sb.appendLine("- Avoid tourist traps, prefer authentic local experiences")
        sb.appendLine("- One Morning, one Afternoon or Evening activity per day")
        sb.appendLine("- NO repeated activities across days")
        sb.appendLine()
        sb.appendLine("Return pure JSON array only, no explanations.")

        return sb.toString()
    }
}
