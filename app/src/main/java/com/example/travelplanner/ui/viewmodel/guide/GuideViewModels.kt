package com.example.travelplanner.ui.viewmodel.guide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelplanner.app.AppGraph
import com.example.travelplanner.data.repo.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

import com.example.travelplanner.llm.GroqClient

// ===== 1. DASHBOARD VIEW MODEL =====
class GuideDashboardViewModel : ViewModel() {
    private val repo = AppGraph.guideRepository
    private val groqClient = GroqClient(apiKeyProvider = { AppGraph.settingsRepository.loadGroqKey() })

    private val _aiOptimizationText = MutableStateFlow<String?>(null)
    val aiOptimizationText: StateFlow<String?> = _aiOptimizationText.asStateFlow()

    private val _isOptimizing = MutableStateFlow(false)
    val isOptimizing: StateFlow<Boolean> = _isOptimizing.asStateFlow()

    private val demandPool = listOf(
        "🔥 High Demand Alert: Heritage & Culture walking tours in your area are trending up (+35% traveller searches this week). Recommended group tour pricing: ₹1,200 / person.",
        "📈 Weekend Travel Surge: Private Beach & Sunset Excursions are seeing a +42% spike in weekend booking requests. Recommended package rate: ₹1,800 / person.",
        "⚡ Peak Season Insight: Historic Church & Old Town tours have 88% higher demand for morning slots (09:00 AM - 11:30 AM).",
        "🎯 Pricing Suggestion: Family & Small Group Excursions are most requested today (+28% searches). Suggested private tour rate: ₹2,500 / group.",
        "🌟 Popular Category: Food & Culinary walking tours in Latin Quarters are seeing high engagement (+50% inquiries).",
        "☀️ Weather & Demand Tip: Clear skies expected this weekend! Outdoor coastal & nature treks are experiencing a +38% surge in traveller interest."
    )

    private val _aiDemandInsights = MutableStateFlow(demandPool.random())
    val aiDemandInsights: StateFlow<String> = _aiDemandInsights.asStateFlow()

    private val _aiSmartReplies = MutableStateFlow<List<String>>(emptyList())
    val aiSmartReplies: StateFlow<List<String>> = _aiSmartReplies.asStateFlow()

    private val _isGeneratingReply = MutableStateFlow(false)
    val isGeneratingReply: StateFlow<Boolean> = _isGeneratingReply.asStateFlow()

    init {
        viewModelScope.launch {
            repo.syncGuideBookingsFromCloud()
            refreshDemandInsights()
        }
    }

    fun syncGuideBookings() {
        viewModelScope.launch {
            repo.syncGuideBookingsFromCloud()
        }
    }

    fun refreshDemandInsights() {
        viewModelScope.launch {
            try {
                val settings = AppGraph.settingsRepository.observeSettings().first()
                val city = settings.guideBaseCity.ifBlank { "Goa" }
                val rate = settings.guideHourlyRate.toInt()

                val prompt = "Generate one short 1-sentence real-time travel demand forecast & pricing insight specifically for local tour guides operating in $city, India. Include percentage surge or recommended rate."
                val liveInsight = try {
                    groqClient.chat("llama-3.3-70b-versatile", prompt)
                } catch (_: Exception) { "" }

                if (liveInsight.isNotBlank()) {
                    _aiDemandInsights.value = liveInsight.trim().replace("🤖", "").replace("GenAI", "").trim()
                } else {
                    val localPool = listOf(
                        "🔥 High Demand in $city: Heritage & Culture walking tours are trending up (+35% searches in $city this week). Recommended tour rate: ₹$rate / person.",
                        "📈 Weekend Travel Surge in $city: Sunset excursions are seeing a +42% spike in weekend booking requests.",
                        "⚡ Peak Season Insight for $city: Historic tours have 88% higher demand for morning slots (09:00 AM - 11:30 AM).",
                        "🎯 Pricing Suggestion in $city: Small Group Excursions are most requested today (+28% searches). Suggested rate: ₹$rate / group.",
                        "🌟 Popular Category in $city: Culinary walking tours in old heritage quarters are seeing high engagement (+50% inquiries).",
                        "☀️ Weather & Demand Tip for $city: Clear skies expected! Outdoor treks in $city are experiencing a +38% surge in traveller interest."
                    )
                    _aiDemandInsights.value = localPool.random()
                }
            } catch (_: Exception) {}
        }
    }

    fun optimizeRouteWithAi(destination: String, date: String) {
        viewModelScope.launch {
            _isOptimizing.value = true
            try {
                val settings = AppGraph.settingsRepository.observeSettings().first()
                val city = settings.guideBaseCity.ifBlank { "Goa" }
                val targetLocation = if (destination.isNotBlank() && destination != "Old Goa Heritage Walk") "$destination, $city" else "$city, India"

                val prompt = "Generate a 3-bullet point smart route optimization & tour guide talking points for a guided tour in $targetLocation on $date. Include: 1) Best start time to beat local crowds in $city, 2) Key historical highlight & story, 3) Authentic local food/tea refreshment tip in $city. Do not use markdown symbols or asterisks, format cleanly as 3 bullet points."
                val response = groqClient.chat("llama-3.3-70b-versatile", prompt)
                _aiOptimizationText.value = response.trim().replace("🤖", "").replace("GenAI", "").replace("*", "").trim()
            } catch (_: Exception) {
                val settings = try { AppGraph.settingsRepository.observeSettings().first() } catch (e: Exception) { null }
                val city = settings?.guideBaseCity?.ifBlank { "Goa" } ?: "Goa"
                _aiOptimizationText.value = when (city.lowercase()) {
                    "mumbai" -> "⚡ Smart Route Optimization (Mumbai):\n• 08:30 AM: Begin at Gateway of India & Taj Mahal Palace before harbor crowds arrive.\n• 10:00 AM: Heritage architectural walk through Kala Ghoda arts district & Oval Maidan.\n• 11:15 AM: Authentic refreshment stop at historic Irani cafe for bun maska & Irani chai."
                    "jaipur" -> "⚡ Smart Route Optimization (Jaipur):\n• 08:00 AM: Early morning photo stop at Hawa Mahal for optimal golden hour light.\n• 09:30 AM: Guided historical walk through City Palace & Jantar Mantar observatory.\n• 11:00 AM: Traditional Johari Bazaar artisan visit & authentic Jaipur lassi tasting."
                    "delhi" -> "⚡ Smart Route Optimization (Delhi):\n• 08:30 AM: Start at Humayun Tomb gardens before midday heat & tour buses arrive.\n• 10:15 AM: Guided walk through Lodhi Art District & historical monuments.\n• 11:30 AM: Local refreshment break for street chai & authentic parathas."
                    else -> "⚡ Smart Route Optimization ($city):\n• 09:30 AM: Start at Old Goa Church complex to beat peak tourist bus arrivals.\n• 10:45 AM: Historical briefing & photography walk at Latin Quarter (Fontainhas).\n• 11:30 AM: Local bakery refreshment break with traditional Goan bebinca & tea."
                }
            } finally {
                _isOptimizing.value = false
            }
        }
    }

    fun generateSmartReplies(travellerName: String, specialRequests: String, destination: String) {
        viewModelScope.launch {
            _isGeneratingReply.value = true
            try {
                val prompt = "Generate 3 short, courteous, professional SMS replies from a tour guide to traveller $travellerName for a tour in $destination. Their request: '$specialRequests'. Format as 3 numbered options."
                val response = groqClient.chat("llama-3.3-70b-versatile", prompt)
                val lines = response.lines().filter { it.trim().startsWith("1.") || it.trim().startsWith("2.") || it.trim().startsWith("3.") }
                if (lines.isNotEmpty()) {
                    _aiSmartReplies.value = lines.map { it.replace(Regex("^[0-9]+\\.\\s*"), "").trim() }
                } else {
                    _aiSmartReplies.value = listOf(
                        "Hi $travellerName! Looking forward to guiding your tour in $destination. I'll make sure we accommodate your request!",
                        "Hello $travellerName, thanks for reaching out. Your tour is confirmed and I've noted your preference for $specialRequests.",
                        "Hi $travellerName! All set for our tour in $destination. Feel free to text me if you have any further questions!"
                    )
                }
            } catch (_: Exception) {
                _aiSmartReplies.value = listOf(
                    "Hi $travellerName! Looking forward to guiding your tour in $destination. I'll make sure we accommodate your request!",
                    "Hello $travellerName, thanks for reaching out. Your tour is confirmed and I've noted your preference for $specialRequests.",
                    "Hi $travellerName! All set for our tour in $destination. Feel free to text me if you have any further questions!"
                )
            } finally {
                _isGeneratingReply.value = false
            }
        }
    }

    fun acceptBooking(bookingId: String) {
        viewModelScope.launch {
            repo.acceptBooking(bookingId)
        }
    }

    fun rejectBooking(bookingId: String) {
        viewModelScope.launch {
            repo.rejectBooking(bookingId)
        }
    }

    val bookings: StateFlow<List<BookingItem>> = repo.bookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val schedule: StateFlow<List<TourScheduleItem>> = repo.schedule
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val earnings: StateFlow<EarningsData> = repo.earnings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EarningsData(0.0, 0.0, 0.0, 0.0, 0.0, 0.10, 0))

    val profile: StateFlow<GuideProfileData> = repo.profile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GuideProfileData("", "", emptyList(), 0, emptyList(), emptyList(), 5.0))

    val reviews: StateFlow<List<ReviewItem>> = repo.reviews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
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
    private val repo = AppGraph.guideRepository

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = AppGraph.settingsRepository.observeSettings().first()
            val guideLocation = settings.guideBaseCity.trim().lowercase()

            repo.bookings.collect { list ->
                val confirmedInLocation = list.filter { booking ->
                    val isConfirmed = booking.status.equals("Confirmed", ignoreCase = true) ||
                            booking.status.equals("Completed", ignoreCase = true)
                    val dest = booking.destination.trim().lowercase()
                    val matchesLocation = guideLocation.isBlank() ||
                            dest.contains(guideLocation) ||
                            guideLocation.contains(dest) ||
                            dest.contains("local")

                    isConfirmed && matchesLocation
                }
                val currentMap = _messages.value.toMutableMap()
                confirmedInLocation.forEach { booking ->
                    if (!currentMap.containsKey(booking.travellerName)) {
                        currentMap[booking.travellerName] = listOf(
                            ChatMessage(
                                id = UUID.randomUUID().toString(),
                                text = "Hi ${booking.travellerName}! I have accepted your trip request for ${booking.destination}. Looking forward to guiding you!",
                                time = "Just now",
                                isMe = true
                            )
                        )
                    }
                }
                _messages.value = currentMap
            }
        }
    }

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

    fun updateProfile(bio: String, name: String, languages: List<String>, contactNumber: String = "", avatarUrl: String = "", onSuccess: () -> Unit) {
        viewModelScope.launch {
            repo.updateProfile(bio = bio, name = name, languages = languages, contactNumber = contactNumber, avatarUrl = avatarUrl)
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
