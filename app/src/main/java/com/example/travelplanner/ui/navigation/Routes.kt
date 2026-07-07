package com.example.travelplanner.ui.navigation

object Routes {
    const val HOME = "home"
    const val CREATE_TRIP = "create_trip"
    const val SETTINGS = "settings"
    const val SUGGESTIONS = "suggestions"
    const val YEAR_PLANNER = "year_planner"
    const val TRIP_DETAIL = "trip_detail/{tripId}"
    const val DAY_EDITOR = "day_editor/{dayId}"

    fun tripDetail(tripId: String) = "trip_detail/$tripId"
    fun dayEditor(dayId: String) = "day_editor/$dayId"
}
