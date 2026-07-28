package com.example.travelplanner.app

import android.content.Context
import com.example.travelplanner.data.repo.AuthRepository
import com.example.travelplanner.data.repo.SettingsRepository
import com.example.travelplanner.data.repo.TripRepository
import com.example.travelplanner.domain.usecase.*
import com.example.travelplanner.llm.*
import com.example.travelplanner.services.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Application-level dependency graph following Clean Architecture principles.
 * Uses Service Locator pattern for dependency management.
 *
 * Architecture layers:
 * - Domain: Use cases and business logic
 * - Data: Repositories and data sources
 * - Services: External integrations and utilities
 */
object AppGraph {

    private lateinit var appContext: Context

    val appScope: CoroutineScope by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // ===== Data Layer =====

    val tripRepository: TripRepository by lazy {
        TripRepository(appContext)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(appContext)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(appContext)
    }

    val guideRepository: com.example.travelplanner.data.repo.GuideRepository by lazy {
        com.example.travelplanner.data.repo.GuideRepository(appContext)
    }

    val storageRepository: com.example.travelplanner.data.repo.StorageRepository by lazy {
        com.example.travelplanner.data.repo.StorageRepository()
    }

    // ===== Domain Layer - Use Cases =====

    val calculateBudgetUseCase: CalculateBudgetUseCase by lazy {
        CalculateBudgetUseCaseImpl(tripRepository)
    }

    val getTripSuggestionsUseCase: GetTripSuggestionsUseCase by lazy {
        GetTripSuggestionsUseCaseImpl(
            onlineService = onlineSuggestionService,
            offlineService = suggestionService
        )
    }

    // ===== Services Layer =====

    // Business Services
    val budgetService: BudgetService by lazy {
        BudgetService()
    }

    val itineraryService: ItineraryService by lazy {
        ItineraryService()
    }

    val packingService: PackingService by lazy {
        PackingService()
    }

    val suggestionService: TripSuggestionService by lazy {
        TripSuggestionService()
    }

    val onlineSuggestionService: OnlineTripSuggestionService by lazy {
        OnlineTripSuggestionService(suggestionService)
    }

    // External Services
    val transportAdvisor: TransportAdvisorService by lazy {
        TransportAdvisorService(appContext)
    }

    val calendarService: CalendarService by lazy {
        CalendarService(appContext)
    }

    val pdfExportService: PdfExportService by lazy {
        PdfExportService(appContext)
    }

    val jsonShareService: JsonShareService by lazy {
        JsonShareService(appContext)
    }

    // ===== LLM Clients =====

    val openAIClient: OpenAIClient by lazy {
        OpenAIClient(apiKeyProvider = { settingsRepository.loadOpenAIKey() })
    }

    val groqClient: GroqClient by lazy {
        GroqClient(apiKeyProvider = { settingsRepository.loadGroqKey() })
    }

    val geminiClient: GeminiClient by lazy {
        GeminiClient(apiKeyProvider = { settingsRepository.loadGeminiKey() })
    }

    val anthropicClient: AnthropicClient by lazy {
        AnthropicClient(apiKeyProvider = { settingsRepository.loadAnthropicKey() })
    }

    val mcpAggregatorClient: MCPAggregatorClient by lazy {
        MCPAggregatorClient(
            groqClient = groqClient,
            geminiClient = geminiClient,
            anthropicClient = anthropicClient,
            openAIClient = openAIClient
        )
    }
}
