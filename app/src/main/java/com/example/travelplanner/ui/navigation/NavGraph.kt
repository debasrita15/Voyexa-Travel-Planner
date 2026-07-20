package com.example.travelplanner.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.travelplanner.app.AppGraph
import com.example.travelplanner.data.prefs.AuthSessionData
import com.example.travelplanner.ui.screens.*
import com.example.travelplanner.ui.screens.auth.ForgotPasswordScreen
import com.example.travelplanner.ui.screens.auth.LoginScreen
import com.example.travelplanner.ui.screens.auth.RegisterScreen
import com.example.travelplanner.ui.screens.guide.*
import com.example.travelplanner.ui.viewmodel.auth.AuthViewModel
import kotlinx.coroutines.launch

private val authEnterTransition: EnterTransition =
    fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 3 }

private val authExitTransition: ExitTransition =
    fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { -it / 3 }

private val authPopEnterTransition: EnterTransition =
    fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it / 3 }

private val authPopExitTransition: ExitTransition =
    fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { it / 3 }

@Composable
fun NavGraph() {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()

    val session by AppGraph.authRepository.observeSession()
        .collectAsState(initial = AuthSessionData())

    // Reactively redirect based on session changes (handles logout from any role)
    LaunchedEffect(session.isLoggedIn, session.role) {
        val currentRoute = nav.currentBackStackEntry?.destination?.route
        if (!session.isLoggedIn) {
            // User logged out — always go to login
            if (currentRoute != Routes.LOGIN && currentRoute != Routes.REGISTER && currentRoute != Routes.FORGOT_PASSWORD) {
                nav.navigate(Routes.AUTH_GRAPH) {
                    popUpTo(0) { inclusive = true }
                }
            }
        } else {
            // User just logged in — navigate to their role's home if currently on auth screens
            val onAuthScreen = currentRoute == Routes.LOGIN ||
                currentRoute == Routes.REGISTER ||
                currentRoute == Routes.FORGOT_PASSWORD ||
                currentRoute == null
            if (onAuthScreen) {
                val target = if (session.role == "Guide") Routes.GUIDE_DASHBOARD else Routes.HOME
                nav.navigate(target) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    val startDestination = if (session.isLoggedIn) {
        if (session.role == "Guide") Routes.GUIDE_DASHBOARD else Routes.HOME
    } else {
        Routes.AUTH_GRAPH
    }

    NavHost(
        navController = nav,
        startDestination = startDestination
    ) {

        navigation(
            startDestination = Routes.LOGIN,
            route = Routes.AUTH_GRAPH,
            enterTransition = { authEnterTransition },
            exitTransition = { authExitTransition },
            popEnterTransition = { authPopEnterTransition },
            popExitTransition = { authPopExitTransition }
        ) {
            composable(Routes.LOGIN) { entry ->
                val parentEntry = remember(entry) {
                    nav.getBackStackEntry(Routes.AUTH_GRAPH)
                }
                val authViewModel: AuthViewModel = viewModel(parentEntry)

                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { role ->
                        val targetRoute = if (role == "Guide") Routes.GUIDE_DASHBOARD else Routes.HOME
                        nav.navigate(targetRoute) {
                            popUpTo(Routes.AUTH_GRAPH) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { nav.navigate(Routes.REGISTER) },
                    onNavigateToForgotPassword = { nav.navigate(Routes.FORGOT_PASSWORD) }
                )
            }

            composable(Routes.REGISTER) { entry ->
                val parentEntry = remember(entry) {
                    nav.getBackStackEntry(Routes.AUTH_GRAPH)
                }
                val authViewModel: AuthViewModel = viewModel(parentEntry)

                RegisterScreen(
                    viewModel = authViewModel,
                    onRegisterSuccess = { role ->
                        val targetRoute = if (role == "Guide") Routes.GUIDE_DASHBOARD else Routes.HOME
                        nav.navigate(targetRoute) {
                            popUpTo(Routes.AUTH_GRAPH) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { nav.popBackStack() }
                )
            }

            composable(Routes.FORGOT_PASSWORD) { entry ->
                val parentEntry = remember(entry) {
                    nav.getBackStackEntry(Routes.AUTH_GRAPH)
                }
                val authViewModel: AuthViewModel = viewModel(parentEntry)

                ForgotPasswordScreen(
                    viewModel = authViewModel,
                    onBack = { nav.popBackStack() }
                )
            }
        }

        composable(Routes.HOME) {
            HomeScreen(
                onCreateTrip = {
                    if (session.isLoggedIn) {
                        nav.navigate(Routes.CREATE_TRIP)
                    } else {
                        nav.navigate(Routes.AUTH_GRAPH)
                    }
                },
                onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                onOpenSuggestions = { nav.navigate(Routes.SUGGESTIONS) },
                onOpenYearPlanner = { nav.navigate(Routes.YEAR_PLANNER) },
                onOpenTrip = { tripId -> nav.navigate(Routes.tripDetail(tripId)) },
                onLogout = {
                    scope.launch {
                        AppGraph.authRepository.logout()
                    }
                },
                onLogin = {
                    nav.navigate(Routes.AUTH_GRAPH)
                }
            )
        }

        composable(Routes.GUIDE_DASHBOARD) {
            GuideDashboardScreen(
                onLogout = {
                    scope.launch {
                        AppGraph.authRepository.logout()
                        // LaunchedEffect above will handle navigation to AUTH_GRAPH
                    }
                },
                onNavigateToCreateTour = { nav.navigate(Routes.CREATE_TOUR) },
                onNavigateToManageBookings = { nav.navigate(Routes.MANAGE_BOOKINGS) },
                onNavigateToChat = { nav.navigate(Routes.CHAT) },
                onNavigateToEarnings = { nav.navigate(Routes.EARNINGS) },
                onNavigateToReviews = { nav.navigate(Routes.REVIEWS) },
                onNavigateToAvailability = { nav.navigate(Routes.AVAILABILITY) },
                onNavigateToProfile = { nav.navigate(Routes.GUIDE_PROFILE) },
                onNavigateToSupport = { nav.navigate(Routes.SUPPORT) },
                onNavigateToBookingDetails = { id -> nav.navigate(Routes.bookingDetails(id)) },
                onNavigateToScheduleEditor = { nav.navigate(Routes.SCHEDULE_EDITOR) }
            )
        }

        composable(Routes.CREATE_TOUR) {
            CreateTourScreen(onBack = { nav.popBackStack() })
        }

        composable(Routes.MANAGE_BOOKINGS) {
            ManageBookingsScreen(
                onBack = { nav.popBackStack() },
                onNavigateToBookingDetails = { id -> nav.navigate(Routes.bookingDetails(id)) }
            )
        }

        composable(Routes.CHAT) {
            ChatScreen(onBack = { nav.popBackStack() })
        }

        composable(Routes.EARNINGS) {
            EarningsScreen(onBack = { nav.popBackStack() })
        }

        composable(Routes.REVIEWS) {
            ReviewsScreen(onBack = { nav.popBackStack() })
        }

        composable(Routes.AVAILABILITY) {
            AvailabilityScreen(onBack = { nav.popBackStack() })
        }

        composable(Routes.GUIDE_PROFILE) {
            GuideProfileScreen(onBack = { nav.popBackStack() })
        }

        composable(Routes.SUPPORT) {
            SupportScreen(onBack = { nav.popBackStack() })
        }

        composable(
            route = Routes.BOOKING_DETAILS,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { entry ->
            val bookingId = entry.arguments?.getString("bookingId").orEmpty()
            BookingDetailsScreen(
                bookingId = bookingId,
                onBack = { nav.popBackStack() },
                onNavigateToChat = { nav.navigate(Routes.CHAT) },
                onNavigateToAddReview = { id -> nav.navigate(Routes.addReview(id)) }
            )
        }

        composable(Routes.SCHEDULE_EDITOR) {
            ScheduleEditorScreen(onBack = { nav.popBackStack() })
        }

        composable(
            route = Routes.ADD_REVIEW,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { entry ->
            val bookingId = entry.arguments?.getString("bookingId").orEmpty()
            AddReviewScreen(
                bookingId = bookingId,
                onBack = { nav.popBackStack() }
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
                            val tripId = AppGraph.tripRepository.createTrip(
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
        ) { entry ->
            val tripId = entry.arguments?.getString("tripId").orEmpty()
            TripDetailScreen(
                tripId = tripId,
                onBack = { nav.popBackStack() },
                onEditDay = { dayId -> nav.navigate(Routes.dayEditor(dayId)) }
            )
        }

        composable(
            route = Routes.DAY_EDITOR,
            arguments = listOf(navArgument("dayId") { type = NavType.StringType })
        ) { entry ->
            val dayId = entry.arguments?.getString("dayId").orEmpty()
            DayEditorScreen(
                dayId = dayId,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
