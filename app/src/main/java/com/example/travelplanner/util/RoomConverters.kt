package com.example.travelplanner.util

import androidx.room.TypeConverter
import com.example.travelplanner.domain.BudgetCategory
import com.example.travelplanner.domain.PackingCategory
import com.example.travelplanner.domain.TripStatus
import com.example.travelplanner.domain.TripType

class RoomConverters {

    @TypeConverter fun tripTypeToString(v: TripType): String = v.name
    @TypeConverter fun stringToTripType(v: String): TripType = TripType.valueOf(v)

    @TypeConverter fun tripStatusToString(v: TripStatus): String = v.name
    @TypeConverter fun stringToTripStatus(v: String): TripStatus = TripStatus.valueOf(v)

    @TypeConverter fun budgetCatToString(v: BudgetCategory): String = v.name
    @TypeConverter fun stringToBudgetCat(v: String): BudgetCategory = BudgetCategory.valueOf(v)

    @TypeConverter fun packingCatToString(v: PackingCategory): String = v.name
    @TypeConverter fun stringToPackingCat(v: String): PackingCategory = PackingCategory.valueOf(v)
}
