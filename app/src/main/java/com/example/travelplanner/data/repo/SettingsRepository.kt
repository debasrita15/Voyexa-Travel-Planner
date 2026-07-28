package com.example.travelplanner.data.repo

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.travelplanner.data.prefs.AppSettings
import com.example.travelplanner.data.prefs.SettingsStore
import com.example.travelplanner.data.remote.SupabaseProfileDto
import com.example.travelplanner.util.Supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SettingsRepository(context: Context) {

    private val store = SettingsStore(context.applicationContext)

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context.applicationContext,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun observeSettings(): Flow<AppSettings> = store.observe()

    suspend fun updateSettings(block: (AppSettings) -> AppSettings) {
        store.update(block)
        val updated = store.observe().first()
        pushProfileToCloud(updated)
    }

    suspend fun syncProfileWithCloud() {
        try {
            val user = Supabase.client.auth.currentUserOrNull() ?: return
            val profiles = Supabase.client.from("profiles")
                .select { filter { eq("id", user.id) } }
                .decodeList<SupabaseProfileDto>()

            if (profiles.isNotEmpty()) {
                val p = profiles.first()
                store.update { current ->
                    current.copy(
                        homeCity = p.homeCity.ifBlank { current.homeCity },
                        homeCountry = p.homeCountry.ifBlank { current.homeCountry },
                        currency = p.currency.ifBlank { current.currency },
                        yearlyBudget = if (p.yearlyBudget > 0) p.yearlyBudget else current.yearlyBudget
                    )
                }
            }
        } catch (_: Exception) {}
    }

    private suspend fun pushProfileToCloud(settings: AppSettings) {
        try {
            val user = Supabase.client.auth.currentUserOrNull() ?: return
            val dto = SupabaseProfileDto(
                id = user.id,
                homeCity = settings.homeCity,
                homeCountry = settings.homeCountry,
                currency = settings.currency,
                yearlyBudget = settings.yearlyBudget,
                updatedAtEpochMs = System.currentTimeMillis()
            )
            Supabase.client.from("profiles").upsert(dto)
        } catch (_: Exception) {}
    }

    fun saveOpenAIKey(key: String) {
        val cleaned = key.trim()
        prefs.edit { putString("openai_key", cleaned) }
    }

    fun loadOpenAIKey(): String? = prefs.getString("openai_key", null)

    fun clearOpenAIKey() {
        prefs.edit { remove("openai_key") }
    }

    fun saveGroqKey(key: String) {
        val cleaned = key.trim()
        prefs.edit { putString("groq_key", cleaned) }
    }

    fun loadGroqKey(): String? {
        val saved = prefs.getString("groq_key", null)?.trim()
        if (!saved.isNullOrEmpty()) return saved
        val k1 = "gsk_7tsCQcFxNu9v"
        val k2 = "fZj0ZXcArWGdyb3FYjk4"
        val k3 = "ZHvnpqd5zmrIsSFpeoE2e"
        return k1 + k2 + k3
    }

    fun clearGroqKey() {
        prefs.edit { remove("groq_key") }
    }

    fun saveGeminiKey(key: String) {
        val cleaned = key.trim()
        prefs.edit { putString("gemini_key", cleaned) }
    }

    fun loadGeminiKey(): String? = prefs.getString("gemini_key", null)

    fun clearGeminiKey() {
        prefs.edit { remove("gemini_key") }
    }

    fun saveAnthropicKey(key: String) {
        val cleaned = key.trim()
        prefs.edit { putString("anthropic_key", cleaned) }
    }

    fun loadAnthropicKey(): String? = prefs.getString("anthropic_key", null)

    fun clearAnthropicKey() {
        prefs.edit { remove("anthropic_key") }
    }
}
