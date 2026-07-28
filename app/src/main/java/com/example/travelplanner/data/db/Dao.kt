package com.example.travelplanner.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Query("SELECT * FROM trip_plan ORDER BY startDateEpochMs ASC")
    fun observeTrips(): Flow<List<TripPlanEntity>>

    @Query("SELECT * FROM trip_plan ORDER BY startDateEpochMs ASC")
    suspend fun getTripsSync(): List<TripPlanEntity>

    @Query("SELECT * FROM trip_plan WHERE userId = :userId OR userId = '' ORDER BY startDateEpochMs ASC")
    fun observeTripsForUser(userId: String): Flow<List<TripPlanEntity>>

    @Query("SELECT * FROM trip_plan WHERE id = :tripId LIMIT 1")
    fun observeTrip(tripId: String): Flow<TripPlanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTrip(trip: TripPlanEntity)

    @Query("DELETE FROM trip_plan WHERE id = :tripId")
    suspend fun deleteTrip(tripId: String)

    // Days
    @Query("SELECT * FROM itinerary_day WHERE tripId = :tripId ORDER BY dateEpochMs ASC")
    fun observeDays(tripId: String): Flow<List<ItineraryDayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDays(days: List<ItineraryDayEntity>)

    @Query("DELETE FROM itinerary_day WHERE tripId = :tripId")
    suspend fun deleteDaysByTrip(tripId: String)

    // Items
    @Query("SELECT * FROM activity_item WHERE itineraryDayId IN (:dayIds) ORDER BY sortIndex ASC")
    fun observeItemsByDays(dayIds: List<String>): Flow<List<ActivityItemEntity>>

    @Query("SELECT * FROM activity_item WHERE itineraryDayId = :dayId ORDER BY sortIndex ASC")
    fun observeItemsForDay(dayId: String): Flow<List<ActivityItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<ActivityItemEntity>)

    @Update
    suspend fun updateItem(item: ActivityItemEntity)

    @Query("DELETE FROM activity_item WHERE itineraryDayId IN (:dayIds)")
    suspend fun deleteItemsByDays(dayIds: List<String>)

    // Budget
    @Query("SELECT * FROM trip_budget_item WHERE tripId = :tripId")
    fun observeBudget(tripId: String): Flow<List<TripBudgetItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudgetItems(items: List<TripBudgetItemEntity>)

    @Update
    suspend fun updateBudgetItem(item: TripBudgetItemEntity)

    @Query("DELETE FROM trip_budget_item WHERE tripId = :tripId")
    suspend fun deleteBudgetByTrip(tripId: String)

    // Packing
    @Query("SELECT * FROM packing_item WHERE tripId = :tripId")
    fun observePacking(tripId: String): Flow<List<PackingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPacking(items: List<PackingItemEntity>)

    @Update
    suspend fun updatePackingItem(item: PackingItemEntity)

    @Query("DELETE FROM packing_item WHERE tripId = :tripId")
    suspend fun deletePackingByTrip(tripId: String)

    // Clear all tables for user isolation / logout
    @Query("DELETE FROM trip_plan")
    suspend fun clearAllTrips()

    @Query("DELETE FROM itinerary_day")
    suspend fun clearAllDays()

    @Query("DELETE FROM activity_item")
    suspend fun clearAllActivities()

    @Query("DELETE FROM trip_budget_item")
    suspend fun clearAllBudgets()

    @Query("DELETE FROM packing_item")
    suspend fun clearAllPacking()
}
