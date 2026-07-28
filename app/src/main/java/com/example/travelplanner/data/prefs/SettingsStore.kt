package com.example.travelplanner.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.travelplanner.domain.LLMProviderType
import com.example.travelplanner.domain.TravelPace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

data class AppSettings(
    val year: Int,
    val yearlyBudget: Double,
    val reservedBufferPercent: Double,
    val homeCity: String,
    val homeCountry: String,
    val currency: String,
    val adultsCount: Int,
    val kidAgeYears: Int,
    val kidsAgesText: String = "2, 5",
    val kidNapRequired: Boolean,
    val kidFriendlyPreference: Int,
    val travelPace: TravelPace,
    val guideBaseCity: String = "Goa",
    val guideHourlyRate: Double = 1500.0,
    val guideLanguages: String = "English, Hindi",
    val guideSpecialization: String = "Heritage & Culture",
    val guideEmergencyContact: String = "+1 (555) 019-8877",
    val llmProvider: LLMProviderType,
    val openAIModel: String,
    val groqModel: String,
    val geminiModel: String,
    val anthropicModel: String,
    val mcpProviders: List<String>
)

class SettingsStore(private val context: Context) {

    private val KEY_YEAR = intPreferencesKey("year")
    private val KEY_YEARLY_BUDGET = doublePreferencesKey("yearly_budget")
    private val KEY_BUFFER_PCT = doublePreferencesKey("buffer_pct")
    private val KEY_HOME_CITY = stringPreferencesKey("home_city")
    private val KEY_HOME_COUNTRY = stringPreferencesKey("home_country")
    private val KEY_CURRENCY = stringPreferencesKey("currency")

    private val KEY_ADULTS = intPreferencesKey("adults")
    private val KEY_KID_AGE = intPreferencesKey("kid_age")
    private val KEY_KIDS_AGES = stringPreferencesKey("kids_ages")
    private val KEY_KID_NAP = booleanPreferencesKey("kid_nap")
    private val KEY_KID_PREF = intPreferencesKey("kid_pref")
    private val KEY_PACE = stringPreferencesKey("pace")

    private val KEY_GUIDE_BASE_CITY = stringPreferencesKey("guide_base_city")
    private val KEY_GUIDE_HOURLY_RATE = doublePreferencesKey("guide_hourly_rate")
    private val KEY_GUIDE_LANGUAGES = stringPreferencesKey("guide_languages")
    private val KEY_GUIDE_SPECIALIZATION = stringPreferencesKey("guide_specialization")
    private val KEY_GUIDE_EMERGENCY_CONTACT = stringPreferencesKey("guide_emergency_contact")

    private val KEY_LLM_PROVIDER = stringPreferencesKey("llm_provider")
    private val KEY_OPENAI_MODEL = stringPreferencesKey("openai_model")
    private val KEY_GROQ_MODEL = stringPreferencesKey("groq_model")
    private val KEY_GEMINI_MODEL = stringPreferencesKey("gemini_model")
    private val KEY_ANTHROPIC_MODEL = stringPreferencesKey("anthropic_model")
    private val KEY_MCP_PROVIDERS = stringPreferencesKey("mcp_providers")

    fun observe(): Flow<AppSettings> {
        return context.dataStore.data.map { p ->
            val defaultCurrency = try {
                java.util.Currency.getInstance(java.util.Locale.getDefault()).currencyCode
            } catch (e: Exception) {
                "USD"
            }

            AppSettings(
                year = p[KEY_YEAR] ?: java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
                yearlyBudget = p[KEY_YEARLY_BUDGET] ?: 200000.0,
                reservedBufferPercent = p[KEY_BUFFER_PCT] ?: 15.0,
                homeCity = p[KEY_HOME_CITY] ?: "",
                homeCountry = p[KEY_HOME_COUNTRY] ?: java.util.Locale.getDefault().displayCountry,
                currency = p[KEY_CURRENCY] ?: defaultCurrency,
                adultsCount = p[KEY_ADULTS] ?: 2,
                kidAgeYears = p[KEY_KID_AGE] ?: 2,
                kidsAgesText = p[KEY_KIDS_AGES] ?: "2, 5",
                kidNapRequired = p[KEY_KID_NAP] ?: true,
                kidFriendlyPreference = p[KEY_KID_PREF] ?: 80,
                travelPace = TravelPace.valueOf(p[KEY_PACE] ?: TravelPace.MEDIUM.name),
                guideBaseCity = p[KEY_GUIDE_BASE_CITY] ?: "Goa",
                guideHourlyRate = p[KEY_GUIDE_HOURLY_RATE] ?: 1500.0,
                guideLanguages = p[KEY_GUIDE_LANGUAGES] ?: "English, Hindi",
                guideSpecialization = p[KEY_GUIDE_SPECIALIZATION] ?: "Heritage & Culture",
                guideEmergencyContact = p[KEY_GUIDE_EMERGENCY_CONTACT] ?: "+1 (555) 019-8877",
                llmProvider = LLMProviderType.valueOf(p[KEY_LLM_PROVIDER] ?: LLMProviderType.GROQ.name),
                openAIModel = p[KEY_OPENAI_MODEL] ?: "gpt-4o-mini",
                groqModel = p[KEY_GROQ_MODEL] ?: "llama-3.3-70b-versatile",
                geminiModel = p[KEY_GEMINI_MODEL] ?: "gemini-2.0-flash-exp",
                anthropicModel = p[KEY_ANTHROPIC_MODEL] ?: "claude-3-5-haiku-20241022",
                mcpProviders = p[KEY_MCP_PROVIDERS]?.split(",")?.filter { it.isNotBlank() }
                    ?: listOf("GROQ", "GEMINI")
            )
        }
    }

    suspend fun update(block: (AppSettings) -> AppSettings) {
        val current = observeOnce()
        val updated = block(current)
        context.dataStore.edit { p ->
            p[KEY_YEAR] = updated.year
            p[KEY_YEARLY_BUDGET] = updated.yearlyBudget
            p[KEY_BUFFER_PCT] = updated.reservedBufferPercent
            p[KEY_HOME_CITY] = updated.homeCity
            p[KEY_HOME_COUNTRY] = updated.homeCountry
            p[KEY_CURRENCY] = updated.currency

            p[KEY_ADULTS] = updated.adultsCount
            p[KEY_KID_AGE] = updated.kidAgeYears
            p[KEY_KIDS_AGES] = updated.kidsAgesText
            p[KEY_KID_NAP] = updated.kidNapRequired
            p[KEY_KID_PREF] = updated.kidFriendlyPreference
            p[KEY_PACE] = updated.travelPace.name

            p[KEY_GUIDE_BASE_CITY] = updated.guideBaseCity
            p[KEY_GUIDE_HOURLY_RATE] = updated.guideHourlyRate
            p[KEY_GUIDE_LANGUAGES] = updated.guideLanguages
            p[KEY_GUIDE_SPECIALIZATION] = updated.guideSpecialization
            p[KEY_GUIDE_EMERGENCY_CONTACT] = updated.guideEmergencyContact

            p[KEY_LLM_PROVIDER] = updated.llmProvider.name
            p[KEY_OPENAI_MODEL] = updated.openAIModel
            p[KEY_GROQ_MODEL] = updated.groqModel
            p[KEY_GEMINI_MODEL] = updated.geminiModel
            p[KEY_ANTHROPIC_MODEL] = updated.anthropicModel
            p[KEY_MCP_PROVIDERS] = updated.mcpProviders.joinToString(",")
        }
    }

    private suspend fun observeOnce(): AppSettings {
        return observe().map { it }.first()
    }
}
