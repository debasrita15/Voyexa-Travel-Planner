package com.example.travelplanner.data.repo

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.travelplanner.data.prefs.AppSettings
import com.example.travelplanner.data.prefs.SettingsStore
import kotlinx.coroutines.flow.Flow

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

    suspend fun updateSettings(block: (AppSettings) -> AppSettings) = store.update(block)

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

    fun loadGroqKey(): String? = prefs.getString("groq_key", null)

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
