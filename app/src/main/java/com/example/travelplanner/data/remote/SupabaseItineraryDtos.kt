package com.example.travelplanner.data.remote

import com.example.travelplanner.data.db.ActivityItemEntity
import com.example.travelplanner.data.db.ItineraryDayEntity
import com.example.travelplanner.data.db.PackingItemEntity
import com.example.travelplanner.data.db.TripBudgetItemEntity
import com.example.travelplanner.domain.BudgetCategory
import com.example.travelplanner.domain.PackingCategory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ===== Itinerary Day DTO =====
@Serializable
data class SupabaseItineraryDayDto(
    val id: String,
    @SerialName("trip_id") val tripId: String,
    @SerialName("date_epoch_ms") val dateEpochMs: Long,
    @SerialName("city_area") val cityArea: String,
    @SerialName("rest_buffer_minutes") val restBufferMinutes: Int,
    @SerialName("kid_friendliness_score") val kidFriendlinessScore: Int
) {
    fun toEntity(): ItineraryDayEntity = ItineraryDayEntity(
        id = id,
        tripId = tripId,
        dateEpochMs = dateEpochMs,
        cityArea = cityArea,
        restBufferMinutes = restBufferMinutes,
        kidFriendlinessScore = kidFriendlinessScore
    )
}

fun ItineraryDayEntity.toSupabaseDto(): SupabaseItineraryDayDto = SupabaseItineraryDayDto(
    id = id,
    tripId = tripId,
    dateEpochMs = dateEpochMs,
    cityArea = cityArea,
    restBufferMinutes = restBufferMinutes,
    kidFriendlinessScore = kidFriendlinessScore
)

// ===== Activity Item DTO =====

@Serializable
data class SupabaseActivityItemDto(
    val id: String,
    @SerialName("itinerary_day_id") val itineraryDayId: String,
    val title: String,
    val category: String,
    @SerialName("time_slot") val timeSlot: String,
    @SerialName("estimated_cost") val estimatedCost: Double,
    @SerialName("commute_minutes") val commuteMinutes: Int,
    val notes: String,
    @SerialName("sort_index") val sortIndex: Int
) {
    fun toEntity(): ActivityItemEntity = ActivityItemEntity(
        id = id,
        itineraryDayId = itineraryDayId,
        title = title,
        category = category,
        timeSlot = timeSlot,
        estimatedCost = estimatedCost,
        commuteMinutes = commuteMinutes,
        notes = notes,
        sortIndex = sortIndex
    )
}

fun ActivityItemEntity.toSupabaseDto(): SupabaseActivityItemDto = SupabaseActivityItemDto(
    id = id,
    itineraryDayId = itineraryDayId,
    title = title,
    category = category,
    timeSlot = timeSlot,
    estimatedCost = estimatedCost,
    commuteMinutes = commuteMinutes,
    notes = notes,
    sortIndex = sortIndex
)

// ===== Trip Budget Item DTO =====

@Serializable
data class SupabaseTripBudgetItemDto(
    val id: String,
    @SerialName("trip_id") val tripId: String,
    val category: String,
    val planned: Double,
    val spent: Double
) {
    fun toEntity(): TripBudgetItemEntity = TripBudgetItemEntity(
        id = id,
        tripId = tripId,
        category = try { BudgetCategory.valueOf(category) } catch (_: Exception) { BudgetCategory.BUFFER },
        planned = planned,
        spent = spent
    )
}

fun TripBudgetItemEntity.toSupabaseDto(): SupabaseTripBudgetItemDto = SupabaseTripBudgetItemDto(
    id = id,
    tripId = tripId,
    category = category.name,
    planned = planned,
    spent = spent
)

// ===== Packing Item DTO =====

@Serializable
data class SupabasePackingItemDto(
    val id: String,
    @SerialName("trip_id") val tripId: String,
    val category: String,
    val title: String,
    @SerialName("is_checked") val isChecked: Boolean
) {
    fun toEntity(): PackingItemEntity = PackingItemEntity(
        id = id,
        tripId = tripId,
        category = try { PackingCategory.valueOf(category) } catch (_: Exception) { PackingCategory.ESSENTIALS },
        title = title,
        isChecked = isChecked
    )
}

fun PackingItemEntity.toSupabaseDto(): SupabasePackingItemDto = SupabasePackingItemDto(
    id = id,
    tripId = tripId,
    category = category.name,
    title = title,
    isChecked = isChecked
)
