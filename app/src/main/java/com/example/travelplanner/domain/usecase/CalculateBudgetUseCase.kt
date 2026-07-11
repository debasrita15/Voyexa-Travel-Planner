package com.example.travelplanner.domain.usecase

import com.example.travelplanner.data.repo.TripRepository

interface CalculateBudgetUseCase {
    suspend fun calculateSpent(year: Int): Result<Double>
    suspend fun calculateRemaining(year: Int, yearlyBudget: Double): Result<BudgetInfo>
}

data class BudgetInfo(
    val yearly: Double,
    val spent: Double,
    val remaining: Double
) {
    val spentPercentage: Float
        get() = if (yearly > 0) (spent / yearly).toFloat().coerceIn(0f, 1f) else 0f

    val isOverBudget: Boolean
        get() = spent > yearly
}

class CalculateBudgetUseCaseImpl(
    private val tripRepository: TripRepository
) : CalculateBudgetUseCase {

    override suspend fun calculateSpent(year: Int): Result<Double> {
        return try {
            val spent = tripRepository.calculateSpentBudget(year)
            Result.success(spent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun calculateRemaining(year: Int, yearlyBudget: Double): Result<BudgetInfo> {
        return try {
            val spent = tripRepository.calculateSpentBudget(year)
            val remaining = yearlyBudget - spent

            val budgetInfo = BudgetInfo(
                yearly = yearlyBudget,
                spent = spent,
                remaining = remaining
            )

            Result.success(budgetInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
