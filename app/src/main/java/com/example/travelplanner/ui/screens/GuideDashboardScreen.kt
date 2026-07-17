package com.example.travelplanner.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelplanner.app.AppGraph
import com.example.travelplanner.data.prefs.AuthSessionData
import com.example.travelplanner.data.repo.*
import com.example.travelplanner.ui.viewmodel.guide.GuideDashboardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideDashboardScreen(
    onLogout: () -> Unit,
    onNavigateToCreateTour: () -> Unit,
    onNavigateToManageBookings: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToEarnings: () -> Unit,
    onNavigateToReviews: () -> Unit,
    onNavigateToAvailability: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToBookingDetails: (String) -> Unit,
    onNavigateToScheduleEditor: () -> Unit,
    viewModel: GuideDashboardViewModel = viewModel()
) {
    val session by AppGraph.authRepository.observeSession()
        .collectAsState(initial = AuthSessionData())

    var isOnline by remember { mutableStateOf(true) }

    val bookings by viewModel.bookings.collectAsState()
    val schedule by viewModel.schedule.collectAsState()
    val earnings by viewModel.earnings.collectAsState()
    val profile by viewModel.profile.collectAsState()

    // Filter active upcoming bookings (pending or confirmed)
    val upcomingBookings = remember(bookings) {
        bookings.filter { it.status == "Pending" || it.status == "Confirmed" }
    }

    val reviews = remember {
        listOf(
            ReviewItem(
                id = "r1",
                travellerName = "Emily Watson",
                tourName = "Old Goa Churches Tour",
                rating = 5,
                reviewText = "Incredible guide! Explained the architecture and historical background of Old Goa so passionately. Highly recommended!",
                date = "July 12, 2026"
            ),
            ReviewItem(
                id = "r2",
                travellerName = "James Peterson",
                tourName = "Goa Heritage Walk",
                rating = 4,
                reviewText = "Very friendly, punctual, and knowledgeable. The heritage walk was very engaging.",
                date = "July 08, 2026"
            )
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Welcome Header
            item {
                GuideHeader(
                    session = session,
                    isOnline = isOnline,
                    onToggleOnline = { isOnline = !isOnline },
                    onLogout = onLogout
                )
            }

            // 2. Quick Stats Grid
            item {
                SectionTitle("Quick Overview")
                Spacer(Modifier.height(10.dp))
                StatsGrid(
                    earnings = earnings,
                    profile = profile,
                    onNavigateToEarnings = onNavigateToEarnings,
                    onNavigateToReviews = onNavigateToReviews,
                    onNavigateToManageBookings = onNavigateToManageBookings
                )
            }

            // 3. Today's Schedule
            item {
                SectionTitle("Today's Schedule")
            }
            if (schedule.isEmpty()) {
                item {
                    EmptyScheduleCard()
                }
            } else {
                items(schedule) { tour ->
                    ScheduleCard(
                        tour = tour,
                        onClick = onNavigateToScheduleEditor
                    )
                }
            }

            // 4. Earnings Goal Progress Card
            item {
                SectionTitle("Earnings & Goal Progress")
                Spacer(Modifier.height(10.dp))
                EarningsCard(
                    earnings = earnings,
                    onClick = onNavigateToEarnings
                )
            }

            // 5. Quick Actions
            item {
                SectionTitle("Quick Actions")
                Spacer(Modifier.height(10.dp))
                QuickActionsGrid(
                    onNavigateToCreateTour = onNavigateToCreateTour,
                    onNavigateToManageBookings = onNavigateToManageBookings,
                    onNavigateToChat = onNavigateToChat,
                    onNavigateToEarnings = onNavigateToEarnings,
                    onNavigateToReviews = onNavigateToReviews,
                    onNavigateToAvailability = onNavigateToAvailability,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToSupport = onNavigateToSupport
                )
            }

            // 6. Upcoming Bookings
            item {
                SectionTitle("Upcoming Bookings")
            }
            if (upcomingBookings.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No upcoming bookings.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(upcomingBookings) { booking ->
                    BookingCard(
                        booking = booking,
                        onClick = { onNavigateToBookingDetails(booking.id) },
                        onMessage = onNavigateToChat
                    )
                }
            }

            // 7. Recent Reviews
            item {
                SectionTitle("Recent Traveller Reviews")
            }
            items(reviews) { review ->
                ReviewCard(
                    review = review,
                    onClick = onNavigateToReviews
                )
            }

            // Bottom Spacer
            item {
                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun GuideHeader(
    session: AuthSessionData,
    isOnline: Boolean,
    onToggleOnline: () -> Unit,
    onLogout: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val heroColor = if (dark) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
    val heroText = if (dark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary

    val formattedDate = remember {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        sdf.format(Date())
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(
            width = if (dark) 1.dp else 0.dp,
            color = if (dark) MaterialTheme.colorScheme.primary.copy(.35f) else Color.Transparent
        ),
        colors = CardDefaults.cardColors(containerColor = heroColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val initials = if (session.displayName.isNotBlank()) {
                            session.displayName.take(2).uppercase()
                        } else {
                            "GD"
                        }
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Welcome back, Guide",
                        style = MaterialTheme.typography.labelLarge,
                        color = heroText.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (session.displayName.isNotBlank()) session.displayName else "Partner",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = heroText
                    )
                }

                FilledIconButton(
                    onClick = onLogout,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out")
                }
            }

            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = heroText.copy(alpha = 0.15f))
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TODAY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = heroText.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = heroText
                    )
                }

                Surface(
                    onClick = onToggleOnline,
                    shape = RoundedCornerShape(50),
                    color = if (isOnline) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (isOnline) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isOnline) Color(0xFF10B981) else Color(0xFF94A3B8),
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Text(
                            text = if (isOnline) "Online" else "Offline",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isOnline) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsGrid(
    earnings: EarningsData,
    profile: GuideProfileData,
    onNavigateToEarnings: () -> Unit,
    onNavigateToReviews: () -> Unit,
    onNavigateToManageBookings: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                icon = Icons.Default.Route,
                value = "${earnings.completedToursCount}",
                title = "Trips Guided",
                modifier = Modifier.weight(1f),
                tint = MaterialTheme.colorScheme.primary,
                onClick = onNavigateToManageBookings
            )
            StatCard(
                icon = Icons.Default.CalendarMonth,
                value = "8",
                title = "Upcoming Tours",
                modifier = Modifier.weight(1f),
                tint = MaterialTheme.colorScheme.secondary,
                onClick = onNavigateToManageBookings
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                icon = Icons.Default.Star,
                value = "${profile.rating}",
                title = "Traveller Rating",
                modifier = Modifier.weight(1f),
                tint = Color(0xFFFFB000),
                onClick = onNavigateToReviews
            )
            StatCard(
                icon = Icons.Default.Payments,
                value = "$${earnings.totalEarnings.toInt()}",
                title = "Total Earnings",
                modifier = Modifier.weight(1f),
                tint = MaterialTheme.colorScheme.tertiary,
                onClick = onNavigateToEarnings
            )
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    title: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = tint.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ScheduleCard(
    tour: TourScheduleItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = tour.destination,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = tour.status,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailItem(icon = Icons.Default.Schedule, label = "Time", value = tour.time)
                DetailItem(icon = Icons.Default.Place, label = "Meeting Point", value = tour.meetingPoint)
                DetailItem(icon = Icons.Default.People, label = "Travellers", value = "${tour.travellersCount} Confirmed")
            }
        }
    }
}

@Composable
private fun EmptyScheduleCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "No tours scheduled for today",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Take some rest or plan upcoming routes!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BookingCard(
    booking: BookingItem,
    onClick: () -> Unit,
    onMessage: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = booking.travellerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = booking.destination,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (booking.paymentStatus == "Paid") Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = booking.paymentStatus,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (booking.paymentStatus == "Paid") Color(0xFF065F46) else Color(0xFF92400E),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            DetailRow(label = "Date", value = booking.date)
            DetailRow(label = "Group Size", value = "${booking.groupSize} People")
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("View Details")
                }
                Button(
                    onClick = onMessage,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Message")
                }
            }
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onNavigateToCreateTour: () -> Unit,
    onNavigateToManageBookings: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToEarnings: () -> Unit,
    onNavigateToReviews: () -> Unit,
    onNavigateToAvailability: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSupport: () -> Unit
) {
    val items = listOf(
        QuickActionItem("Create Tour", Icons.Default.Add, MaterialTheme.colorScheme.primary, onNavigateToCreateTour),
        QuickActionItem("Bookings", Icons.Default.BookOnline, MaterialTheme.colorScheme.secondary, onNavigateToManageBookings),
        QuickActionItem("Chat Messages", Icons.Default.ChatBubble, MaterialTheme.colorScheme.tertiary, onNavigateToChat),
        QuickActionItem("Earnings Report", Icons.Default.MonetizationOn, Color(0xFF10B981), onNavigateToEarnings),
        QuickActionItem("Reviews List", Icons.Default.RateReview, Color(0xFFFFB000), onNavigateToReviews),
        QuickActionItem("Availability", Icons.Default.EventAvailable, Color(0xFF8B5CF6), onNavigateToAvailability),
        QuickActionItem("Guide Profile", Icons.Default.AccountCircle, Color(0xFFEC4899), onNavigateToProfile),
        QuickActionItem("Get Support", Icons.Default.ContactSupport, Color(0xFF6B7280), onNavigateToSupport)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        for (i in items.indices step 4) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                for (j in 0 until 4) {
                    if (i + j < items.size) {
                        val item = items[i + j]
                        QuickActionCard(
                            item = item,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    item: QuickActionItem,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = item.onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = item.color.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.color,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun EarningsCard(
    earnings: EarningsData,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Target Progress",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "$${earnings.monthlyEarnings.toInt()} earned",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Goal: $5,000",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(14.dp))
            val progress = remember(earnings.monthlyEarnings) {
                (earnings.monthlyEarnings / 5000.0).coerceIn(0.0, 1.0).toFloat()
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Today's Earnings: +$${earnings.todayEarnings.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF10B981)
                )
                Text(
                    text = "${(progress * 100).toInt()}% Completed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReviewCard(
    review: ReviewItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = review.travellerName.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = review.travellerName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = review.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < review.rating) Color(0xFFFFB000) else Color(0xFFE2E8F0),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = review.reviewText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

data class QuickActionItem(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)
