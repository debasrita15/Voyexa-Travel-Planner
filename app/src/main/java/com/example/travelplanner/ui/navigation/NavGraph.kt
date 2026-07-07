package com.example.travelplanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.travelplanner.ui.screens.*
import kotlinx.coroutines.launch

@Composable
fun NavGraph() {
    val nav = rememberNavController()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    NavHost(navController = nav, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onCreateTrip = { nav.navigate(Routes.CREATE_TRIP) },
                onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                onOpenSuggestions = { nav.navigate(Routes.SUGGESTIONS) },
                onOpenYearPlanner = { nav.navigate(Routes.YEAR_PLANNER) },
                onOpenTrip = { tripId -> nav.navigate(Routes.tripDetail(tripId)) }
            )
        }

        composable(Routes.CREATE_TRIP) {
            CreateTripScreen(
                onBack = { nav.popBackStack() },
                onCreated = { tripId ->
                    nav.popBackStack()
                    nav.navigate(Routes.tripDetail(tripId))
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { nav.popBackStack() })
        }

        composable(Routes.SUGGESTIONS) {
            SuggestionsScreen(
                onBack = { nav.popBackStack() },
                onCreateFromSuggestion = { title, destination, startMs, endMs, type, budget ->
                    scope.launch {
                        try {
                            val tripId = com.example.travelplanner.app.AppGraph.tripRepository.createTrip(
                                title = title,
                                destination = destination,
                                startDateEpochMs = startMs,
                                endDateEpochMs = endMs,
                                type = type,
                                budgetCap = budget
                            )
                            nav.navigate(Routes.tripDetail(tripId))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            )
        }

        composable(Routes.YEAR_PLANNER) {
            YearPlannerScreen(onBack = { nav.popBackStack() })
        }

        composable(
            route = Routes.TRIP_DETAIL,
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId").orEmpty()
            TripDetailScreen(
                tripId = tripId,
                onBack = { nav.popBackStack() },
                onEditDay = { dayId -> nav.navigate(Routes.dayEditor(dayId)) }
            )
        }

        composable(
            route = Routes.DAY_EDITOR,
            arguments = listOf(navArgument("dayId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dayId = backStackEntry.arguments?.getString("dayId").orEmpty()
            DayEditorScreen(
                dayId = dayId,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
