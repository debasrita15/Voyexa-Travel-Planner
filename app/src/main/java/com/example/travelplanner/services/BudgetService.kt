package com.example.travelplanner.services

import com.example.travelplanner.data.prefs.AppSettings
import com.example.travelplanner.domain.BudgetCategory
import com.example.travelplanner.domain.TripType
import kotlin.math.max

class BudgetService {

    fun reservedBufferAmount(yearlyBudget: Double, bufferPercent: Double): Double {
        return max(0.0, yearlyBudget * bufferPercent / 100.0)
    }

    fun usableBudget(yearlyBudget: Double, bufferPercent: Double): Double {
        return max(0.0, yearlyBudget - reservedBufferAmount(yearlyBudget, bufferPercent))
    }

    fun defaultCategorySplit(tripType: TripType, kidAgeYears: Int): Map<BudgetCategory, Double> {
        val kidComfortBoost = kidAgeYears <= 4

        return if (tripType == TripType.INTERNATIONAL) {
            mapOf(
                BudgetCategory.TRANSPORT to 0.35,
                BudgetCategory.STAY to if (kidComfortBoost) 0.32 else 0.28,
                BudgetCategory.FOOD to 0.12,
                BudgetCategory.LOCAL_COMMUTE to 0.08,
                BudgetCategory.TICKETS to 0.07,
                BudgetCategory.BUFFER to 0.06
            )
        } else {
            mapOf(
                BudgetCategory.TRANSPORT to 0.25,
                BudgetCategory.STAY to if (kidComfortBoost) 0.34 else 0.30,
                BudgetCategory.FOOD to 0.16,
                BudgetCategory.LOCAL_COMMUTE to 0.10,
                BudgetCategory.TICKETS to 0.08,
                BudgetCategory.BUFFER to 0.07
            )
        }
    }

    fun allocateTripBudget(tripBudgetCap: Double, tripType: TripType, settings: AppSettings): List<Pair<BudgetCategory, Double>> {
        val split = defaultCategorySplit(tripType, settings.kidAgeYears)
        return BudgetCategory.entries.map { cat ->
            val pct = split[cat] ?: 0.0
            cat to (tripBudgetCap * pct)
        }
    }
}
