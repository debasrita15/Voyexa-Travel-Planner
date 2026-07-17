package com.example.travelplanner.ui.navigation

object Routes {
    const val AUTH_GRAPH = "auth_graph"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"

    const val HOME = "home"
    const val GUIDE_DASHBOARD = "guide_dashboard"
    const val CREATE_TRIP = "create_trip"
    const val SETTINGS = "settings"
    const val SUGGESTIONS = "suggestions"
    const val YEAR_PLANNER = "year_planner"
    const val TRIP_DETAIL = "trip_detail/{tripId}"
    const val DAY_EDITOR = "day_editor/{dayId}"
    const val CREATE_TOUR = "create_tour"
    const val MANAGE_BOOKINGS = "manage_bookings"
    const val CHAT = "chat"
    const val EARNINGS = "earnings"
    const val REVIEWS = "reviews"
    const val AVAILABILITY = "availability"
    const val GUIDE_PROFILE = "guide_profile"
    const val SUPPORT = "support"
    const val BOOKING_DETAILS = "booking_details/{bookingId}"
    const val SCHEDULE_EDITOR = "schedule_editor"
    const val ADD_REVIEW = "add_review/{bookingId}"

    fun tripDetail(tripId: String) = "trip_detail/$tripId"
    fun dayEditor(dayId: String) = "day_editor/$dayId"
    fun bookingDetails(bookingId: String) = "booking_details/$bookingId"
    fun addReview(bookingId: String) = "add_review/$bookingId"
}
