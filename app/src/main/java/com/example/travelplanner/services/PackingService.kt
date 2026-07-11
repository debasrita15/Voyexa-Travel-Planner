package com.example.travelplanner.services

import com.example.travelplanner.data.db.PackingItemEntity
import com.example.travelplanner.data.prefs.AppSettings
import com.example.travelplanner.domain.PackingCategory
import java.util.UUID

class PackingService {

    fun generateKidChecklist(tripId: String, settings: AppSettings): List<PackingItemEntity> {
        val age = settings.kidAgeYears

        val essentials = listOf(
            "Diapers or pull ups",
            "Wipes",
            "Extra underwear",
            "Light blanket",
            "Stroller or carrier"
        )

        val clothing = listOf(
            "Extra t shirts",
            "Comfortable pants or shorts",
            "Jacket or sweater",
            "Socks"
        )

        val food = listOf(
            "Snacks",
            "Water bottle",
            "Formula or milk if needed"
        )

        val health = listOf(
            "Thermometer",
            "Fever medicine",
            "Band aids",
            "Mosquito repellent"
        )

        val entertainment = listOf(
            "Small toy",
            "Story book",
            "Coloring kit"
        )

        val documents = listOf(
            "Kid ID proof if needed",
            "Insurance or medical info"
        )

        val ageExtras = if (age <= 3) listOf("Pacifier", "Teething gel if needed") else listOf("Simple board game")

        val map = listOf(
            PackingCategory.ESSENTIALS to (essentials + ageExtras),
            PackingCategory.CLOTHING to clothing,
            PackingCategory.FOOD to food,
            PackingCategory.HEALTH to health,
            PackingCategory.ENTERTAINMENT to entertainment,
            PackingCategory.DOCUMENTS to documents
        )

        val out = mutableListOf<PackingItemEntity>()
        for ((cat, titles) in map) {
            for (t in titles) {
                out.add(
                    PackingItemEntity(
                        id = UUID.randomUUID().toString(),
                        tripId = tripId,
                        category = cat,
                        title = t,
                        isChecked = false
                    )
                )
            }
        }
        return out
    }
}
