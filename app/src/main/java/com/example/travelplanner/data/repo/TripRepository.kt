package com.example.travelplanner.data.repo

import android.content.Context
import com.example.travelplanner.data.db.*
import com.example.travelplanner.data.remote.*
import com.example.travelplanner.domain.TripExportPayload
import com.example.travelplanner.domain.TripStatus
import com.example.travelplanner.domain.TripType
import com.example.travelplanner.util.Supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.UUID

/**
 * Repository for managing trip data.
 * Implements Clean Architecture data layer pattern.
 *
 * Responsibilities:
 * - CRUD operations for trips, itineraries, budgets, and packing lists
 * - Cloud syncing with Supabase
 * - Budget calculations
 * - Data transformations
 */
class TripRepository(context: Context) {

    private val dao = AppDatabase.get(context).tripDao()

    // ===== Query Operations =====

    fun observeTrips(): Flow<List<TripPlanEntity>> {
        val currentUserId = Supabase.client.auth.currentUserOrNull()?.id
        return if (!currentUserId.isNullOrBlank()) {
            dao.observeTripsForUser(currentUserId)
        } else {
            dao.observeTrips()
        }
    }

    fun observeTrip(tripId: String): Flow<TripPlanEntity?> = dao.observeTrip(tripId)

    fun observeDays(tripId: String): Flow<List<ItineraryDayEntity>> = dao.observeDays(tripId)

    fun observeItemsForDay(dayId: String): Flow<List<ActivityItemEntity>> = dao.observeItemsForDay(dayId)

    fun observeBudget(tripId: String): Flow<List<TripBudgetItemEntity>> = dao.observeBudget(tripId)

    fun observePacking(tripId: String): Flow<List<PackingItemEntity>> = dao.observePacking(tripId)

    // ===== Create Operations =====

    suspend fun createTrip(
        title: String,
        destination: String,
        startDateEpochMs: Long,
        endDateEpochMs: Long,
        type: TripType,
        budgetCap: Double
    ): String {
        val tripId = generateTripId()
        val currentTime = getCurrentTimeMillis()
        val currentUserId = Supabase.client.auth.currentUserOrNull()?.id ?: ""

        val tripEntity = TripPlanEntity(
            id = tripId,
            userId = currentUserId,
            title = title,
            destination = destination,
            startDateEpochMs = startDateEpochMs,
            endDateEpochMs = endDateEpochMs,
            type = type,
            status = TripStatus.DRAFT,
            budgetCap = budgetCap,
            createdAtEpochMs = currentTime,
            updatedAtEpochMs = currentTime
        )

        dao.upsertTrip(tripEntity)
        pushTripToSupabase(tripEntity)

        // Automatically create a pending guide request so local guides can view and accept it
        try {
            val formattedDate = try {
                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(startDateEpochMs))
            } catch (_: Exception) { "Upcoming" }
            val booking = BookingItem(
                id = "trip_$tripId",
                travellerName = "Traveller Request",
                destination = destination.ifBlank { "Local Tour" },
                date = formattedDate,
                meetingTime = "09:30 AM",
                groupSize = 2,
                specialRequests = "Trip Request: $title (${type.name.lowercase().replace('_', ' ')})",
                status = "Pending",
                paymentStatus = "Pending"
            )
            com.example.travelplanner.app.AppGraph.guideRepository.requestGuide(booking)
        } catch (_: Exception) {}

        return tripId
    }

    // ===== Sync Operations =====

    suspend fun clearAllLocalData() {
        try {
            dao.clearAllActivities()
            dao.clearAllDays()
            dao.clearAllBudgets()
            dao.clearAllPacking()
            dao.clearAllTrips()
        } catch (_: Exception) {}
    }

    suspend fun syncTripsWithCloud() {
        try {
            val user = Supabase.client.auth.currentUserOrNull() ?: return
            val remoteTrips = Supabase.client.from("trips")
                .select {
                    filter {
                        eq("user_id", user.id)
                    }
                }
                .decodeList<SupabaseTripDto>()

            clearAllLocalData()
            val localEntities = remoteTrips.map { it.toEntity() }
            localEntities.forEach { dao.upsertTrip(it) }
        } catch (_: Exception) {
            // Offline fallback
        }
    }

    suspend fun subscribeToRealtimeTripChanges() {
        try {
            val channel = Supabase.client.realtime.channel("realtime_trips")
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "trips"
            }
            channel.subscribe()
            changeFlow.collect {
                syncTripsWithCloud()
            }
        } catch (_: Exception) {}
    }

    suspend fun syncTripDetailsWithCloud(tripId: String) {
        try {
            val remoteDays = Supabase.client.from("itinerary_days")
                .select { filter { eq("trip_id", tripId) } }
                .decodeList<SupabaseItineraryDayDto>()
            if (remoteDays.isNotEmpty()) {
                dao.upsertDays(remoteDays.map { it.toEntity() })
            }

            val remoteBudget = Supabase.client.from("trip_budget_items")
                .select { filter { eq("trip_id", tripId) } }
                .decodeList<SupabaseTripBudgetItemDto>()
            if (remoteBudget.isNotEmpty()) {
                dao.upsertBudgetItems(remoteBudget.map { it.toEntity() })
            }

            val remotePacking = Supabase.client.from("packing_items")
                .select { filter { eq("trip_id", tripId) } }
                .decodeList<SupabasePackingItemDto>()
            if (remotePacking.isNotEmpty()) {
                dao.upsertPacking(remotePacking.map { it.toEntity() })
            }
        } catch (_: Exception) {
            // Offline fallback
        }
    }

    private suspend fun pushTripToSupabase(trip: TripPlanEntity) {
        try {
            val user = Supabase.client.auth.currentUserOrNull() ?: return
            val dto = trip.toSupabaseDto(user.id)
            Supabase.client.from("trips").upsert(dto)
        } catch (_: Exception) {}
    }

    private suspend fun pushItineraryToSupabase(days: List<ItineraryDayEntity>, items: List<ActivityItemEntity>) {
        try {
            val dayDtos = days.map { it.toSupabaseDto() }
            val itemDtos = items.map { it.toSupabaseDto() }
            if (dayDtos.isNotEmpty()) Supabase.client.from("itinerary_days").upsert(dayDtos)
            if (itemDtos.isNotEmpty()) Supabase.client.from("activity_items").upsert(itemDtos)
        } catch (_: Exception) {}
    }

    private suspend fun pushBudgetToSupabase(items: List<TripBudgetItemEntity>) {
        try {
            val dtos = items.map { it.toSupabaseDto() }
            if (dtos.isNotEmpty()) Supabase.client.from("trip_budget_items").upsert(dtos)
        } catch (_: Exception) {}
    }

    private suspend fun pushPackingToSupabase(items: List<PackingItemEntity>) {
        try {
            val dtos = items.map { it.toSupabaseDto() }
            if (dtos.isNotEmpty()) Supabase.client.from("packing_items").upsert(dtos)
        } catch (_: Exception) {}
    }

    // ===== Update Operations =====

    suspend fun upsertTrip(trip: TripPlanEntity) {
        dao.upsertTrip(trip)
        pushTripToSupabase(trip)
    }

    suspend fun replaceItinerary(
        tripId: String,
        days: List<ItineraryDayEntity>,
        items: List<ActivityItemEntity>
    ) {
        deleteExistingItinerary(tripId)
        insertNewItinerary(days, items)
        pushItineraryToSupabase(days, items)
    }

    suspend fun replaceBudget(tripId: String, items: List<TripBudgetItemEntity>) {
        dao.deleteBudgetByTrip(tripId)
        dao.upsertBudgetItems(items)
        pushBudgetToSupabase(items)
    }

    suspend fun replacePacking(tripId: String, items: List<PackingItemEntity>) {
        dao.deletePackingByTrip(tripId)
        dao.upsertPacking(items)
        pushPackingToSupabase(items)
    }

    suspend fun updatePacking(item: PackingItemEntity) {
        dao.updatePackingItem(item)
        try {
            Supabase.client.from("packing_items").upsert(item.toSupabaseDto())
        } catch (_: Exception) {}
    }

    suspend fun updateBudget(item: TripBudgetItemEntity) {
        dao.updateBudgetItem(item)
        try {
            Supabase.client.from("trip_budget_items").upsert(item.toSupabaseDto())
        } catch (_: Exception) {}
    }

    suspend fun updateActivity(item: ActivityItemEntity) {
        dao.updateItem(item)
        try {
            Supabase.client.from("activity_items").upsert(item.toSupabaseDto())
        } catch (_: Exception) {}
    }

    suspend fun upsertItems(items: List<ActivityItemEntity>) {
        dao.upsertItems(items)
        try {
            val dtos = items.map { it.toSupabaseDto() }
            if (dtos.isNotEmpty()) Supabase.client.from("activity_items").upsert(dtos)
        } catch (_: Exception) {}
    }

    // ===== Delete Operations =====

    suspend fun deleteTripCompletely(tripId: String) {
        deleteItineraryItems(tripId)
        deleteTripData(tripId)
        try {
            Supabase.client.from("trips").delete {
                filter { eq("id", tripId) }
            }
        } catch (_: Exception) {}
    }

    // ===== Budget Calculations =====

    suspend fun calculateSpentBudget(year: Int): Double {
        val allTrips = dao.observeTrips().first()
        val confirmedTrips = filterConfirmedTrips(allTrips)
        val tripsInYear = filterTripsByYear(confirmedTrips, year)
        return sumTripBudgets(tripsInYear)
    }

    suspend fun calculateRemainingBudget(year: Int, yearlyBudget: Double): Double {
        val spent = calculateSpentBudget(year)
        return yearlyBudget - spent
    }

    // ===== Export Operations =====

    fun exportTripPayload(trip: TripPlanEntity): TripExportPayload {
        return TripExportPayload(
            title = trip.title,
            destination = trip.destination,
            startDateEpochMs = trip.startDateEpochMs,
            endDateEpochMs = trip.endDateEpochMs,
            type = trip.type,
            budgetCap = trip.budgetCap
        )
    }

    // ===== Private Helper Methods =====

    private fun generateTripId(): String = UUID.randomUUID().toString()

    private fun getCurrentTimeMillis(): Long = System.currentTimeMillis()

    private suspend fun deleteExistingItinerary(tripId: String) {
        val existingDays = dao.observeDays(tripId).first()
        val dayIds = existingDays.map { it.id }

        if (dayIds.isNotEmpty()) {
            dao.deleteItemsByDays(dayIds)
        }
        dao.deleteDaysByTrip(tripId)
    }

    private suspend fun insertNewItinerary(
        days: List<ItineraryDayEntity>,
        items: List<ActivityItemEntity>
    ) {
        dao.upsertDays(days)
        dao.upsertItems(items)
    }

    private suspend fun deleteItineraryItems(tripId: String) {
        val days = dao.observeDays(tripId).first()
        val dayIds = days.map { it.id }

        if (dayIds.isNotEmpty()) {
            dao.deleteItemsByDays(dayIds)
        }
        dao.deleteDaysByTrip(tripId)
    }

    private suspend fun deleteTripData(tripId: String) {
        dao.deleteBudgetByTrip(tripId)
        dao.deletePackingByTrip(tripId)
        dao.deleteTrip(tripId)
    }

    private fun filterConfirmedTrips(trips: List<TripPlanEntity>): List<TripPlanEntity> {
        return trips.filter { trip ->
            trip.status == TripStatus.COMPLETED || trip.status == TripStatus.PLANNED
        }
    }

    private fun filterTripsByYear(trips: List<TripPlanEntity>, year: Int): List<TripPlanEntity> {
        return trips.filter { trip ->
            getTripYear(trip.startDateEpochMs) == year
        }
    }

    private fun getTripYear(timestampMs: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestampMs
        return calendar.get(Calendar.YEAR)
    }

    private fun sumTripBudgets(trips: List<TripPlanEntity>): Double {
        return trips.sumOf { it.budgetCap }
    }
}