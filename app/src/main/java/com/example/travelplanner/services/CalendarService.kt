package com.example.travelplanner.services

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.example.travelplanner.data.db.ActivityItemEntity
import com.example.travelplanner.data.db.ItineraryDayEntity
import com.example.travelplanner.data.db.TripPlanEntity
import com.example.travelplanner.util.Dates

class CalendarService(private val context: Context) {

    fun buildAllDayInsertIntent(trip: TripPlanEntity, day: ItineraryDayEntity, items: List<ActivityItemEntity>): Intent {
        val start = Dates.startOfDay(day.dateEpochMs)
        val end = Dates.addDays(start, 1)

        val notes = buildString {
            append("Area: ${day.cityArea}\n")
            append("Kid score: ${day.kidFriendlinessScore}\n\n")
            for (it in items.sortedBy { it.sortIndex }) {
                append("${it.timeSlot}: ${it.title} commute ${it.commuteMinutes} min\n")
            }
        }

        return Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI).apply {
            putExtra(CalendarContract.Events.TITLE, "Trip: ${trip.destination}")
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end)
            putExtra(CalendarContract.Events.ALL_DAY, true)
            putExtra(CalendarContract.Events.DESCRIPTION, notes)
        }
    }
}
