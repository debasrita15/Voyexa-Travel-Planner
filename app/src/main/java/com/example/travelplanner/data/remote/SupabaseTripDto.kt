package com.example.travelplanner.data.remote

import com.example.travelplanner.data.db.TripPlanEntity
import com.example.travelplanner.domain.TripStatus
import com.example.travelplanner.domain.TripType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseTripDto(
    val id: String,
    @SerialName("user_id") val userId: String? = null,
    val title: String,
    val destination: String,
    @SerialName("start_date_epoch_ms") val startDateEpochMs: Long,
    @SerialName("end_date_epoch_ms") val endDateEpochMs: Long,
    val type: String,
    val status: String,
    @SerialName("budget_cap") val budgetCap: Double,
    @SerialName("created_at_epoch_ms") val createdAtEpochMs: Long,
    @SerialName("updated_at_epoch_ms") val updatedAtEpochMs: Long
) {
    fun toEntity(): TripPlanEntity = TripPlanEntity(
        id = id,
        userId = userId ?: "",
        title = title,
        destination = destination,
        startDateEpochMs = startDateEpochMs,
        endDateEpochMs = endDateEpochMs,
        type = try { TripType.valueOf(type) } catch (_: Exception) { TripType.DOMESTIC },
        status = try { TripStatus.valueOf(status) } catch (_: Exception) { TripStatus.DRAFT },
        budgetCap = budgetCap,
        createdAtEpochMs = createdAtEpochMs,
        updatedAtEpochMs = updatedAtEpochMs
    )
}

fun TripPlanEntity.toSupabaseDto(userId: String? = null): SupabaseTripDto = SupabaseTripDto(
    id = id,
    userId = userId ?: this.userId,
    title = title,
    destination = destination,
    startDateEpochMs = startDateEpochMs,
    endDateEpochMs = endDateEpochMs,
    type = type.name,
    status = status.name,
    budgetCap = budgetCap,
    createdAtEpochMs = createdAtEpochMs,
    updatedAtEpochMs = updatedAtEpochMs
)