package com.example.travelplanner.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.travelplanner.domain.BudgetCategory
import com.example.travelplanner.domain.PackingCategory
import com.example.travelplanner.domain.TripStatus
import com.example.travelplanner.domain.TripType

@Entity(tableName = "trip_plan")
data class TripPlanEntity(
    @PrimaryKey val id: String,
    val title: String,
    val destination: String,
    val startDateEpochMs: Long,
    val endDateEpochMs: Long,
    val type: TripType,
    val status: TripStatus,
    val budgetCap: Double,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

@Entity(tableName = "itinerary_day")
data class ItineraryDayEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val dateEpochMs: Long,
    val cityArea: String,
    val restBufferMinutes: Int,
    val kidFriendlinessScore: Int
)

@Entity(tableName = "activity_item")
data class ActivityItemEntity(
    @PrimaryKey val id: String,
    val itineraryDayId: String,
    val title: String,
    val category: String,
    val timeSlot: String,
    val estimatedCost: Double,
    val commuteMinutes: Int,
    val notes: String,
    val sortIndex: Int
)

@Entity(tableName = "trip_budget_item")
data class TripBudgetItemEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val category: BudgetCategory,
    val planned: Double,
    val spent: Double
)

@Entity(tableName = "packing_item")
data class PackingItemEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val category: PackingCategory,
    val title: String,
    val isChecked: Boolean
)