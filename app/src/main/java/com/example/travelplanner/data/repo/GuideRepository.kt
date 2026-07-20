package com.example.travelplanner.data.repo

import android.content.Context
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
    val tourPackage: String,
    val date: String,
    val meetingTime: String = "09:30 AM",
    val meetingLocation: String,
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
    val tourName: String,
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
    val verificationStatus: String = "Verified"
)

// ===== REPOSITORY =====

class GuideRepository(context: Context) {

    // Initial state flows populated with realistic mock data
    private val _tours = MutableStateFlow<List<TourItem>>(
        listOf(
            TourItem(
                id = "t1",
                title = "Old Goa Heritage Walk",
                destination = "Old Goa, India",
                category = "History & Culture",
                description = "Explore the ancient Portuguese churches and relics of Old Goa.",
                startDate = "2026-07-20",
                endDate = "2026-07-20",
                meetingPoint = "Fontainhas Post Office",
                maxTravellers = 15,
                price = 35.00,
                duration = "3 hours",
                difficulty = "Easy",
                languages = "English, Hindi"
            )
        )
    )
    val tours: Flow<List<TourItem>> = _tours.asStateFlow()

    private val _bookings = MutableStateFlow<List<BookingItem>>(
        listOf(
            BookingItem(
                id = "b1",
                travellerName = "Sarah Jenkins",
                travellerContact = "+1 (555) 012-3456",
                destination = "Goa Heritage Walk",
                tourPackage = "Heritage Standard Package",
                date = "July 16, 2026",
                meetingTime = "9:30 AM",
                meetingLocation = "Fontainhas Post Office",
                groupSize = 12,
                paymentStatus = "Paid",
                status = "Confirmed",
                notes = "Vegetarian option requested for food stops."
            ),
            BookingItem(
                id = "b2",
                travellerName = "David Miller",
                travellerContact = "+1 (555) 098-7654",
                destination = "Spice Plantation & Waterfalls",
                tourPackage = "Private Family Excursion",
                date = "July 18, 2026",
                meetingTime = "10:00 AM",
                meetingLocation = "Sahakari Spice Farm Entrance",
                groupSize = 6,
                paymentStatus = "Pending",
                status = "Pending",
                notes = "Will bring young children, need slow pace."
            ),
            BookingItem(
                id = "b3",
                travellerName = "Emma Watson",
                travellerContact = "+44 7911 123456",
                destination = "Old Goa Churches Tour",
                tourPackage = "Photography Walk",
                date = "July 12, 2026",
                meetingTime = "09:00 AM",
                meetingLocation = "Basilica of Bom Jesus Gate",
                groupSize = 2,
                paymentStatus = "Paid",
                status = "Completed"
            )
        )
    )
    val bookings: Flow<List<BookingItem>> = _bookings.asStateFlow()

    private val _schedule = MutableStateFlow<List<TourScheduleItem>>(
        listOf(
            TourScheduleItem(
                id = "s1",
                destination = "Goa Heritage Walk",
                time = "9:30 AM",
                meetingPoint = "Fontainhas Post Office",
                travellersCount = 12,
                status = "Confirmed",
                activities = listOf(
                    ActivityStop("a1", "09:30 AM", "Meet & Greet at Fontainhas Post Office", "15 mins", "Check registration forms."),
                    ActivityStop("a2", "09:45 AM", "Historical Briefing at Latin Quarter", "45 mins", "Discuss Portuguese architecture."),
                    ActivityStop("a3", "10:30 AM", "Visit St. Sebastian Chapel", "30 mins", "Photography stop inside."),
                    ActivityStop("a4", "11:00 AM", "Local Tea & Bakery Refreshment Break", "30 mins", "Provide traditional Goan sweets."),
                    ActivityStop("a5", "11:30 AM", "Walk towards Basilica of Bom Jesus", "60 mins", "Detailed historical guiding.")
                )
            )
        )
    )
    val schedule: Flow<List<TourScheduleItem>> = _schedule.asStateFlow()

    private val _reviews = MutableStateFlow<List<ReviewItem>>(
        listOf(
            ReviewItem(
                id = "r1",
                travellerName = "Emily Watson",
                tourName = "Old Goa Churches Tour",
                rating = 5,
                reviewText = "Incredible guide! Explained the architecture and historical background of Old Goa so passionately. Highly recommended!",
                date = "July 12, 2026"
            ),
            ReviewItem(
                id = "r2",
                travellerName = "James Peterson",
                tourName = "Goa Heritage Walk",
                rating = 4,
                reviewText = "Very friendly, punctual, and knowledgeable. The heritage walk was very engaging.",
                date = "July 08, 2026"
            )
        )
    )
    val reviews: Flow<List<ReviewItem>> = _reviews.asStateFlow()

    private val _availability = MutableStateFlow<List<AvailabilityDay>>(
        listOf(
            AvailabilityDay("2026-07-16", "Available", "09:00", "17:00", true),
            AvailabilityDay("2026-07-17", "Available", "09:00", "17:00", true),
            AvailabilityDay("2026-07-18", "Vacation", "00:00", "00:00", false),
            AvailabilityDay("2026-07-19", "Unavailable", "00:00", "00:00", true)
        )
    )
    val availability: Flow<List<AvailabilityDay>> = _availability.asStateFlow()

    private val _earnings = MutableStateFlow(
        EarningsData(
            todayEarnings = 180.0,
            weeklyEarnings = 920.0,
            monthlyEarnings = 4200.0,
            totalEarnings = 12450.0,
            withdrawableBalance = 1850.0,
            completedToursCount = 24
        )
    )
    val earnings: Flow<EarningsData> = _earnings.asStateFlow()

    private val _profile = MutableStateFlow(
        GuideProfileData(
            name = "John Doe",
            bio = "Professional Goan travel guide with over 8 years of experience showcasing Portugal architecture, historical sites, and nature treks.",
            languages = listOf("English", "Hindi", "Portuguese"),
            experienceYears = 8,
            certifications = listOf("Certified Govt Tourism Guide", "First Aid Responder", "Historical Arts Specialist"),
            specializations = listOf("Heritage Walks", "Spice Farm Excursions", "Photography Trails"),
            rating = 4.9
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
        _bookings.value = listOf(booking) + currentList
    }

    suspend fun updateProfile(bio: String, name: String, languages: List<String>) {
        val currentProfile = _profile.value
        _profile.value = currentProfile.copy(bio = bio, name = name, languages = languages)
    }

    private fun updateStatsAndSchedule() {
        // Sync schedule status based on bookings
        val list = _bookings.value
        val confirmedToday = list.filter { it.date.contains("July 16") && it.status == "Confirmed" }
        val size = confirmedToday.sumOf { it.groupSize }
        
        _schedule.value = _schedule.value.map {
            if (it.destination == "Goa Heritage Walk") {
                it.copy(
                    travellersCount = size,
                    status = if (size > 0) "Confirmed" else "No Bookings"
                )
            } else it
        }
    }
}
