package com.example.travelplanner.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.travelplanner.util.RoomConverters

@Database(
    entities = [
        TripPlanEntity::class,
        ItineraryDayEntity::class,
        ActivityItemEntity::class,
        TripBudgetItemEntity::class,
        PackingItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "travel_planner.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = db
                db
            }
        }
    }
}
