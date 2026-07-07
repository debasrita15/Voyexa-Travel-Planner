package com.example.travelplanner.data.repo

import android.content.Context
import com.example.travelplanner.data.db.*
import com.example.travelplanner.domain.TripExportPayload
import com.example.travelplanner.domain.TripStatus
import com.example.travelplanner.domain.TripType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.UUID

/**
 * Repository for managing trip data.
 * Implements Clean Architecture data layer pattern.
 *
 * Responsibilities:
 * - CRUD operations for trips
 * - Budget calculations
 * - Data transformations
 */
class TripRepository(context: Context) {

    private val dao = AppDatabase.get(context).tripDao()

    // ===== Query Operations =====

    fun observeTrips(): Flow<List<TripPlanEntity>> = dao.observeTrips()

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

        val tripEntity = TripPlanEntity(
            id = tripId,
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
        return tripId
    }

    // ===== Update Operations =====

    suspend fun upsertTrip(trip: TripPlanEntity) = dao.upsertTrip(trip)

    suspend fun replaceItinerary(
        tripId: String,
        days: List<ItineraryDayEntity>,
        items: List<ActivityItemEntity>
    ) {
        deleteExistingItinerary(tripId)
        insertNewItinerary(days, items)
    }

    suspend fun replaceBudget(tripId: String, items: List<TripBudgetItemEntity>) {
        dao.deleteBudgetByTrip(tripId)
        dao.upsertBudgetItems(items)
    }

    suspend fun replacePacking(tripId: String, items: List<PackingItemEntity>) {
        dao.deletePackingByTrip(tripId)
        dao.upsertPacking(items)
    }

    suspend fun updatePacking(item: PackingItemEntity) = dao.updatePackingItem(item)

    suspend fun updateBudget(item: TripBudgetItemEntity) = dao.updateBudgetItem(item)

    suspend fun updateActivity(item: ActivityItemEntity) = dao.updateItem(item)

    suspend fun upsertItems(items: List<ActivityItemEntity>) = dao.upsertItems(items)

    // ===== Delete Operations =====

    suspend fun deleteTripCompletely(tripId: String) {
        deleteItineraryItems(tripId)
        deleteTripData(tripId)
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
