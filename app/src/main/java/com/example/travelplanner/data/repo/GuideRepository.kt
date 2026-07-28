package com.example.travelplanner.data.repo

import android.content.Context
import com.example.travelplanner.data.remote.toSupabaseDto
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

// ===== DATA MODELS =====

data class TourItem(
    val id: String,
    val title: String,
    val destination: String,
    val category: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val meetingPoint: String,
    val maxTravellers: Int,
    val price: Double,
    val duration: String,
    val difficulty: String,
    val languages: String,
    val status: String = "Published"
)

data class BookingItem(
    val id: String,
    val travellerName: String,
    val travellerContact: String = "+1 (555) 019-2834",
    val travellerAvatar: String = "",
    val destination: String,
    val tourPackage: String = "Private Custom Tour",
    val date: String,
    val meetingTime: String = "09:30 AM",
    val meetingLocation: String = "City Center / Hotel Pickup",
    val groupSize: Int,
    val specialRequests: String = "None",
    val paymentStatus: String,
    val status: String, // Pending, Confirmed, Completed, Cancelled
    val emergencyContact: String = "John Jenkins: +1 (555) 019-8877",
    val notes: String = "Looking forward to this historic walk!"
)

data class TourScheduleItem(
    val id: String,
    val destination: String,
    val time: String,
    val meetingPoint: String,
    val travellersCount: Int,
    val status: String,
    val activities: List<ActivityStop> = emptyList()
)

data class ActivityStop(
    val id: String,
    val time: String,
    val title: String,
    val duration: String = "30 mins",
    val notes: String = ""
)

data class ReviewItem(
    val id: String,
    val travellerName: String,
    val tourName: String = "Guided Tour",
    val rating: Int,
    val reviewText: String,
    val date: String,
    val isWrittenByMe: Boolean = false,
    val replyText: String? = null
)

data class AvailabilityDay(
    val date: String, // "YYYY-MM-DD"
    val status: String, // Available, Unavailable, Vacation
    val startHour: String = "09:00",
    val endHour: String = "17:00",
    val repeatWeekly: Boolean = true
)

data class EarningsData(
    val todayEarnings: Double,
    val weeklyEarnings: Double,
    val monthlyEarnings: Double,
    val totalEarnings: Double,
    val withdrawableBalance: Double,
    val commissionRate: Double = 0.10,
    val completedToursCount: Int
)

data class GuideProfileData(
    val name: String,
    val bio: String,
    val languages: List<String>,
    val experienceYears: Int,
    val certifications: List<String>,
    val specializations: List<String>,
    val rating: Double,
    val contactNumber: String = "+1 (555) 019-2834",
    val avatarUrl: String = "",
    val verificationStatus: String = "Verified"
)

// ===== REPOSITORY =====

class GuideRepository(private val context: Context) {

    // Dynamic initial state flows (start empty for newly signed up guides)
    private val _tours = MutableStateFlow<List<TourItem>>(emptyList())
    val tours: Flow<List<TourItem>> = _tours.asStateFlow()

    private val _bookings = MutableStateFlow<List<BookingItem>>(emptyList())
    val bookings: Flow<List<BookingItem>> = _bookings.asStateFlow()

    private val _schedule = MutableStateFlow<List<TourScheduleItem>>(emptyList())
    val schedule: Flow<List<TourScheduleItem>> = _schedule.asStateFlow()

    private val _reviews = MutableStateFlow<List<ReviewItem>>(emptyList())
    val reviews: Flow<List<ReviewItem>> = _reviews.asStateFlow()

    private val _availability = MutableStateFlow<List<AvailabilityDay>>(emptyList())
    val availability: Flow<List<AvailabilityDay>> = _availability.asStateFlow()

    private val _earnings = MutableStateFlow(
        EarningsData(
            todayEarnings = 0.0,
            weeklyEarnings = 0.0,
            monthlyEarnings = 0.0,
            totalEarnings = 0.0,
            withdrawableBalance = 0.0,
            completedToursCount = 0
        )
    )
    val earnings: Flow<EarningsData> = _earnings.asStateFlow()

    private val prefs = context.getSharedPreferences("guide_profile_prefs", Context.MODE_PRIVATE)

    private val _profile = MutableStateFlow(
        GuideProfileData(
            name = prefs.getString("guide_name", "")?.ifBlank { null } ?: "Official Guide",
            bio = prefs.getString("guide_bio", "")?.ifBlank { null } ?: "Certified local travel guide showcasing historical destinations and custom tours.",
            languages = prefs.getString("guide_languages", "")?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.ifEmpty { null } ?: listOf("English", "Hindi"),
            experienceYears = 3,
            certifications = listOf("Certified Govt Tourism Guide"),
            specializations = listOf("City Heritage", "Cultural Tours"),
            rating = 5.0,
            contactNumber = prefs.getString("guide_contact", "")?.ifBlank { null } ?: "+1 (555) 019-2834",
            avatarUrl = prefs.getString("guide_avatar", "") ?: ""
        )
    )
    val profile: Flow<GuideProfileData> = _profile.asStateFlow()

    // ===== MUTATIONS & ACTIONS =====

    suspend fun publishTour(tour: TourItem) {
        val currentList = _tours.value
        _tours.value = currentList + tour
    }

    suspend fun acceptBooking(bookingId: String) {
        val currentList = _bookings.value
        val updatedList = currentList.map {
            if (it.id == bookingId) {
                it.copy(status = "Confirmed")
            } else it
        }
        _bookings.value = updatedList
        updateStatsAndSchedule()
        syncCloudBookingState(bookingId)
    }

    suspend fun rejectBooking(bookingId: String) {
        val currentList = _bookings.value
        val updatedList = currentList.map {
            if (it.id == bookingId) {
                it.copy(status = "Cancelled")
            } else it
        }
        _bookings.value = updatedList
        updateStatsAndSchedule()
        syncCloudBookingState(bookingId)
    }

    suspend fun completeBooking(bookingId: String) {
        val currentList = _bookings.value
        var tourPrice = 0.0
        val updatedList = currentList.map {
            if (it.id == bookingId) {
                tourPrice = if (it.groupSize > 0) it.groupSize * 35.0 else 180.0
                it.copy(status = "Completed")
            } else it
        }
        _bookings.value = updatedList

        // Update Earnings on completion
        val currentEarnings = _earnings.value
        _earnings.value = currentEarnings.copy(
            todayEarnings = currentEarnings.todayEarnings + tourPrice,
            weeklyEarnings = currentEarnings.weeklyEarnings + tourPrice,
            monthlyEarnings = currentEarnings.monthlyEarnings + tourPrice,
            totalEarnings = currentEarnings.totalEarnings + tourPrice,
            withdrawableBalance = currentEarnings.withdrawableBalance + tourPrice,
            completedToursCount = currentEarnings.completedToursCount + 1
        )

        updateStatsAndSchedule()
        syncCloudBookingState(bookingId)
    }

    suspend fun rescheduleBooking(bookingId: String, newDate: String, newTime: String) {
        val currentList = _bookings.value
        val updatedList = currentList.map {
            if (it.id == bookingId) {
                it.copy(date = newDate, meetingTime = newTime, status = "Confirmed")
            } else it
        }
        _bookings.value = updatedList
        updateStatsAndSchedule()
        syncCloudBookingState(bookingId)
    }

    suspend fun cancelBooking(bookingId: String) {
        val currentList = _bookings.value
        val updatedList = currentList.map {
            if (it.id == bookingId) {
                it.copy(status = "Cancelled")
            } else it
        }
        _bookings.value = updatedList
        updateStatsAndSchedule()
        syncCloudBookingState(bookingId)
    }

    private suspend fun syncCloudBookingState(bookingId: String) {
        try {
            val booking = _bookings.value.firstOrNull { it.id == bookingId } ?: return
            val dto = booking.toSupabaseDto()
            com.example.travelplanner.util.Supabase.client.from("guide_bookings").upsert(dto)
        } catch (_: Exception) {}
    }

    suspend fun saveAvailability(days: List<AvailabilityDay>) {
        _availability.value = days
    }

    suspend fun saveScheduleTimeline(tourId: String, activities: List<ActivityStop>, meetingPoint: String, time: String) {
        val currentSchedule = _schedule.value
        _schedule.value = currentSchedule.map {
            if (it.id == tourId || it.destination == "Goa Heritage Walk") {
                it.copy(activities = activities, meetingPoint = meetingPoint, time = time)
            } else it
        }
    }

    suspend fun addReviewWritten(review: ReviewItem) {
        val currentReviews = _reviews.value
        _reviews.value = currentReviews + review
    }

    suspend fun replyToReview(reviewId: String, reply: String) {
        val currentReviews = _reviews.value
        _reviews.value = currentReviews.map {
            if (it.id == reviewId) {
                it.copy(replyText = reply)
            } else it
        }
    }

    suspend fun requestGuide(booking: BookingItem) {
        val currentList = _bookings.value
        val updated = listOf(booking) + currentList.filter { it.id != booking.id }
        _bookings.value = updated
        updateStatsAndSchedule()

        // Sync with Supabase cloud database
        try {
            val dto = booking.toSupabaseDto()
            com.example.travelplanner.util.Supabase.client.from("guide_bookings").upsert(dto)
        } catch (_: Exception) {}
    }

    suspend fun syncGuideBookingsFromCloud() {
        val mergedBookings = mutableListOf<BookingItem>()
        try {
            val dtos = com.example.travelplanner.util.Supabase.client.from("guide_bookings")
                .select()
                .decodeList<com.example.travelplanner.data.remote.SupabaseGuideBookingDto>()

            if (dtos.isNotEmpty()) {
                mergedBookings.addAll(dtos.map { it.toBookingItem() })
            }
        } catch (_: Exception) {}

        // Always sync with local Room database trips created by travellers
        try {
            val trips = com.example.travelplanner.data.db.AppDatabase.get(context).tripDao().getTripsSync()
            val existingIds = mergedBookings.map { it.id }.toSet()

            trips.forEach { trip ->
                val bookingId = "trip_${trip.id}"
                if (bookingId !in existingIds) {
                    val formattedDate = try {
                        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(trip.startDateEpochMs))
                    } catch (_: Exception) { "Upcoming" }
                    mergedBookings.add(
                        BookingItem(
                            id = bookingId,
                            travellerName = if (trip.userId.isNotBlank()) "Traveller" else "Traveller Request",
                            destination = trip.destination.ifBlank { "Local Tour" },
                            date = formattedDate,
                            meetingTime = "09:30 AM",
                            groupSize = 2,
                            specialRequests = "Trip: ${trip.title}",
                            status = "Pending",
                            paymentStatus = "Pending"
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        val currentBookings = _bookings.value
        val allMerged = (mergedBookings + currentBookings).distinctBy { it.id }
        if (allMerged.isNotEmpty()) {
            _bookings.value = allMerged
            updateStatsAndSchedule()
        }
    }

    suspend fun updateProfile(
        bio: String,
        name: String,
        languages: List<String>,
        contactNumber: String = "",
        avatarUrl: String = ""
    ) {
        val currentProfile = _profile.value
        val updated = currentProfile.copy(
            bio = bio.ifBlank { currentProfile.bio },
            name = name.ifBlank { currentProfile.name },
            languages = if (languages.isNotEmpty()) languages else currentProfile.languages,
            contactNumber = if (contactNumber.isNotBlank()) contactNumber else currentProfile.contactNumber,
            avatarUrl = if (avatarUrl.isNotBlank()) avatarUrl else currentProfile.avatarUrl
        )
        _profile.value = updated

        try {
            prefs.edit().apply {
                putString("guide_name", updated.name)
                putString("guide_bio", updated.bio)
                putString("guide_languages", updated.languages.joinToString(","))
                putString("guide_contact", updated.contactNumber)
                putString("guide_avatar", updated.avatarUrl)
                apply()
            }
        } catch (_: Exception) {}
    }

    private fun updateStatsAndSchedule() {
        val list = _bookings.value
        val confirmedBookings = list.filter { it.status == "Confirmed" }
        
        val newSchedules = confirmedBookings.mapIndexed { idx, booking ->
            TourScheduleItem(
                id = booking.id,
                destination = booking.destination,
                time = booking.meetingTime.ifBlank { "09:30 AM" },
                meetingPoint = booking.meetingLocation.ifBlank { "Main Entrance" },
                travellersCount = booking.groupSize,
                status = "Confirmed",
                activities = listOf(
                    ActivityStop("act_${idx}_1", booking.meetingTime.ifBlank { "09:30 AM" }, "Meet & Greet with ${booking.travellerName}", "15 mins", "Check-in and introductions"),
                    ActivityStop("act_${idx}_2", "10:00 AM", "Guided Tour of ${booking.destination}", "90 mins", "Explore top spots and hidden gems"),
                    ActivityStop("act_${idx}_3", "11:30 AM", "Local Refreshments & Q&A", "30 mins", "Taste local delicacies and wrap up")
                )
            )
        }
        _schedule.value = newSchedules
    }

    fun resetGuideState() {
        _tours.value = emptyList()
        _bookings.value = emptyList()
        _schedule.value = emptyList()
        _reviews.value = emptyList()
        _availability.value = emptyList()
        _earnings.value = EarningsData(
            todayEarnings = 0.0,
            weeklyEarnings = 0.0,
            monthlyEarnings = 0.0,
            totalEarnings = 0.0,
            withdrawableBalance = 0.0,
            completedToursCount = 0
        )
    }
}
