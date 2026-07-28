package com.example.travelplanner.data.remote

import com.example.travelplanner.data.repo.BookingItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseGuideBookingDto(
    val id: String,
    @SerialName("traveller_name") val travellerName: String = "",
    @SerialName("traveller_contact") val travellerContact: String = "",
    @SerialName("destination") val destination: String = "",
    @SerialName("tour_package") val tourPackage: String = "Custom Guide Request",
    @SerialName("date") val date: String = "",
    @SerialName("meeting_time") val meetingTime: String = "09:30 AM",
    @SerialName("meeting_location") val meetingLocation: String = "To be arranged",
    @SerialName("group_size") val groupSize: Int = 1,
    @SerialName("special_requests") val specialRequests: String = "None",
    @SerialName("payment_status") val paymentStatus: String = "Pending",
    @SerialName("status") val status: String = "Pending",
    @SerialName("notes") val notes: String = "Looking forward to the tour!"
) {
    fun toBookingItem(): BookingItem = BookingItem(
        id = id,
        travellerName = travellerName.ifBlank { "Traveller" },
        travellerContact = travellerContact,
        destination = destination,
        tourPackage = tourPackage,
        date = date,
        meetingTime = meetingTime,
        meetingLocation = meetingLocation,
        groupSize = groupSize,
        specialRequests = specialRequests,
        paymentStatus = paymentStatus,
        status = status,
        notes = notes
    )
}

fun BookingItem.toSupabaseDto(): SupabaseGuideBookingDto = SupabaseGuideBookingDto(
    id = id,
    travellerName = travellerName,
    travellerContact = travellerContact,
    destination = destination,
    tourPackage = tourPackage,
    date = date,
    meetingTime = meetingTime,
    meetingLocation = meetingLocation,
    groupSize = groupSize,
    specialRequests = specialRequests,
    paymentStatus = paymentStatus,
    status = status,
    notes = notes
)
