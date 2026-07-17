package com.example.travelplanner.ui.viewmodel.guide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelplanner.app.AppGraph
import com.example.travelplanner.data.repo.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

// ===== 1. DASHBOARD VIEW MODEL =====
class GuideDashboardViewModel : ViewModel() {
    private val repo = AppGraph.guideRepository

    val bookings: StateFlow<List<BookingItem>> = repo.bookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val schedule: StateFlow<List<TourScheduleItem>> = repo.schedule
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val earnings: StateFlow<EarningsData> = repo.earnings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EarningsData(0.0, 0.0, 0.0, 0.0, 0.0, 0.10, 0))

    val profile: StateFlow<GuideProfileData> = repo.profile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GuideProfileData("", "", emptyList(), 0, emptyList(), emptyList(), 5.0))
}

// ===== 2. CREATE TOUR VIEW MODEL =====
class CreateTourViewModel : ViewModel() {
    private val repo = AppGraph.guideRepository

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun publishTour(
        title: String,
        destination: String,
        category: String,
        description: String,
        startDate: String,
        endDate: String,
        meetingPoint: String,
        maxTravellers: Int,
        price: Double,
        duration: String,
        difficulty: String,
        languages: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val tour = TourItem(
                id = UUID.randomUUID().toString(),
                title = title,
                destination = destination,
                category = category,
                description = description,
                startDate = startDate,
                endDate = endDate,
                meetingPoint = meetingPoint,
                maxTravellers = maxTravellers,
                price = price,
                duration = duration,
                difficulty = difficulty,
                languages = languages
            )
            repo.publishTour(tour)
            _isLoading.value = false
            onSuccess()
        }
    }
}

// ===== 3. MANAGE BOOKINGS VIEW MODEL =====
class ManageBookingsViewModel : ViewModel() {
    private val repo = AppGraph.guideRepository

    val bookings: StateFlow<List<BookingItem>> = repo.bookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun acceptBooking(id: String) {
        viewModelScope.launch { repo.acceptBooking(id) }
    }

    fun rejectBooking(id: String) {
        viewModelScope.launch { repo.rejectBooking(id) }
    }
}

// ===== 4. CHAT VIEW MODEL =====
class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(
        mapOf(
            "Sarah Jenkins" to listOf(
                ChatMessage("c1", "Hello! Is it okay if I bring my camera equipment?", "09:12 AM", isMe = false),
                ChatMessage("c2", "Yes, absolutely! There will be plenty of great photo opportunities.", "09:15 AM", isMe = true)
            ),
            "David Miller" to listOf(
                ChatMessage("c3", "Hi John, what is the difficulty level for the waterfalls tour?", "02:30 PM", isMe = false),
                ChatMessage("c4", "It involves a light 15-minute hike. Quite manageable!", "02:33 PM", isMe = true)
            )
        )
    )
    val messages = _messages.asStateFlow()

    fun sendMessage(recipient: String, text: String) {
        val currentList = _messages.value[recipient] ?: emptyList()
        val newMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            time = "Just now",
            isMe = true
        )
        val updatedMap = _messages.value.toMutableMap()
        updatedMap[recipient] = currentList + newMsg
        _messages.value = updatedMap
    }
}

data class ChatMessage(
    val id: String,
    val text: String,
    val time: String,
    val isMe: Boolean
)

// ===== 5. EARNINGS VIEW MODEL =====
class EarningsViewModel : ViewModel() {
    private val repo = AppGraph.guideRepository

    val earnings: StateFlow<EarningsData> = repo.earnings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EarningsData(0.0, 0.0, 0.0, 0.0, 0.0, 0.10, 0))

    fun withdrawBalance(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Logic to clear withdrawable balance
            onSuccess()
        }
    }
}

// ===== 6. REVIEWS VIEW MODEL =====
class ReviewsViewModel : ViewModel() {
    private val repo = AppGraph.guideRepository

    val reviews: StateFlow<List<ReviewItem>> = repo.reviews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun replyToReview(reviewId: String, reply: String) {
        viewModelScope.launch { repo.replyToReview(reviewId, reply) }
    }
}

// ===== 7. AVAILABILITY VIEW MODEL =====
class AvailabilityViewModel : ViewModel() {
    private val repo = AppGraph.guideRepository

    val availability: StateFlow<List<AvailabilityDay>> = repo.availability
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveAvailability(days: List<AvailabilityDay>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repo.saveAvailability(days)
            onSuccess()
        }
    }
}

// ===== 8. GUIDE PROFILE VIEW MODEL =====
class GuideProfileViewModel : ViewModel() {
    private val repo = AppGraph.guideRepository

    val profile: StateFlow<GuideProfileData> = repo.profile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GuideProfileData("", "", emptyList(), 0, emptyList(), emptyList(), 5.0))

    fun updateProfile(bio: String, name: String, languages: List<String>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repo.updateProfile(bio = bio, name = name, languages = languages)
            onSuccess()
        }
    }
}

// ===== 9. BOOKING DETAILS VIEW MODEL =====
class BookingDetailsViewModel : ViewModel() {
    private val repo = AppGraph.guideRepository

    val bookings: StateFlow<List<BookingItem>> = repo.bookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun acceptBooking(id: String) {
        viewModelScope.launch { repo.acceptBooking(id) }
    }

    fun rejectBooking(id: String) {
        viewModelScope.launch { repo.rejectBooking(id) }
    }

    fun completeBooking(id: String) {
        viewModelScope.launch { repo.completeBooking(id) }
    }

    fun rescheduleBooking(id: String, date: String, time: String) {
        viewModelScope.launch { repo.rescheduleBooking(id, date, time) }
    }

    fun cancelBooking(id: String) {
        viewModelScope.launch { repo.cancelBooking(id) }
    }
}

// ===== 10. SCHEDULE EDITOR VIEW MODEL =====
class ScheduleEditorViewModel : ViewModel() {
    private val repo = AppGraph.guideRepository

    val schedule: StateFlow<List<TourScheduleItem>> = repo.schedule
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveScheduleTimeline(tourId: String, activities: List<ActivityStop>, meetingPoint: String, time: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repo.saveScheduleTimeline(tourId, activities, meetingPoint, time)
            onSuccess()
        }
    }
}

// ===== 11. ADD REVIEW VIEW MODEL =====
class AddReviewViewModel : ViewModel() {
    private val repo = AppGraph.guideRepository

    fun addReviewWritten(travellerName: String, tourName: String, rating: Int, reviewText: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val review = ReviewItem(
                id = UUID.randomUUID().toString(),
                travellerName = travellerName,
                tourName = tourName,
                rating = rating,
                reviewText = reviewText,
                date = "Today",
                isWrittenByMe = true
            )
            repo.addReviewWritten(review)
            onSuccess()
        }
    }
}
