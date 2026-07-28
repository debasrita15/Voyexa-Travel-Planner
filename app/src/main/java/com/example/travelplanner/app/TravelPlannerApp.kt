package com.example.travelplanner.app

import android.app.Application
import com.example.travelplanner.util.Notifications
import com.example.travelplanner.util.Money
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TravelPlannerApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        AppGraph.init(this)
        Notifications.createChannels(this)

        applicationScope.launch {
            try {
                val settings = AppGraph.settingsRepository.observeSettings().first()
                Money.setUserCurrency(settings.currency)
                AppGraph.tripRepository.syncTripsWithCloud()
                AppGraph.settingsRepository.syncProfileWithCloud()
                AppGraph.tripRepository.subscribeToRealtimeTripChanges()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
