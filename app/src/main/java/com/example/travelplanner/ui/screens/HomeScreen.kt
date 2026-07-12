package com.example.travelplanner.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelplanner.app.AppGraph
import com.example.travelplanner.data.db.TripPlanEntity
import com.example.travelplanner.domain.TripStatus
import com.example.travelplanner.util.Dates
import com.example.travelplanner.util.Money
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import androidx.compose.ui.graphics.vector.ImageVector

class HomeViewModel : ViewModel() {
    val trips = AppGraph.tripRepository.observeTrips()
        .stateIn(AppGraph.appScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCreateTrip: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSuggestions: () -> Unit,
    onOpenYearPlanner: () -> Unit,
    onOpenTrip: (String) -> Unit
) {

    val vm: HomeViewModel = viewModel()
    val trips by vm.trips.collectAsState()

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(

        containerColor = MaterialTheme.colorScheme.background,

        floatingActionButton = {

            ExtendedFloatingActionButton(

                onClick = onCreateTrip,

                icon = {
                    Icon(Icons.Default.Add, null)
                },

                text = {
                    Text("New Trip")
                },

                containerColor = MaterialTheme.colorScheme.tertiary,

                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        }

    ) { padding ->

        LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding),

            contentPadding = PaddingValues(20.dp),

            verticalArrangement = Arrangement.spacedBy(20.dp)

        ) {

            item {

                HeroHeader(

                    onOpenSettings = onOpenSettings,

                    onOpenSuggestions = onOpenSuggestions,

                    onOpenYearPlanner = onOpenYearPlanner,

                    onCreateTrip = onCreateTrip

                )

            }

            if (trips.isEmpty()) {

                item {

                    EmptyState(onCreateTrip)

                }

            }

            else {

                item {

                    Text(

                        "Your Trips",

                        style = MaterialTheme.typography.headlineSmall,

                        fontWeight = FontWeight.Bold

                    )

                }

                items(trips) {

                    TripCard(

                        trip = it,

                        onClick = {

                            onOpenTrip(it.id)

                        }

                    )

                }

                item {

                    Spacer(
                        Modifier.height(100.dp)
                    )

                }

            }

        }

    }

}
@Composable
private fun HeroHeader(
    onOpenSettings: () -> Unit,
    onOpenSuggestions: () -> Unit,
    onOpenYearPlanner: () -> Unit,
    onCreateTrip: () -> Unit
) {

    val dark = isSystemInDarkTheme()

    val heroColor =
        if (dark)
            MaterialTheme.colorScheme.surface
        else
            MaterialTheme.colorScheme.primary

    val heroText =
        if (dark)
            MaterialTheme.colorScheme.onSurface
        else
            MaterialTheme.colorScheme.onPrimary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),

        border = BorderStroke(
            width = if (dark) 1.dp else 0.dp,
            color =
                if (dark)
                    MaterialTheme.colorScheme.primary.copy(.35f)
                else
                    Color.Transparent
        ),

        colors = CardDefaults.cardColors(
            containerColor = heroColor
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        )
    ) {

        Column(
            Modifier.padding(28.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        "👋 Hello Traveller",
                        color = heroText,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        "Explore",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = heroText
                    )

                    Text(
                        "The World",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = heroText
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        "Plan your next adventure",
                        color = heroText.copy(.75f)
                    )

                }

                FilledIconButton(

                    onClick = onOpenSettings,

                    colors = IconButtonDefaults.filledIconButtonColors(

                        containerColor = MaterialTheme.colorScheme.tertiary,

                        contentColor = MaterialTheme.colorScheme.onTertiary

                    )

                ) {

                    Icon(Icons.Default.Settings, null)

                }

            }

            Spacer(Modifier.height(15.dp))

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceEvenly

            ) {

                HeaderButton(
                    Icons.Default.Add,
                    "Trip",
                    onCreateTrip
                )

                HeaderButton(
                    Icons.Default.Info,
                    "Ideas",
                    onOpenSuggestions
                )

                HeaderButton(
                    Icons.Default.DateRange,
                    "Planner",
                    onOpenYearPlanner
                )

            }

        }

    }

}
@Composable
private fun HeaderButton(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {

        Card(

            shape = RoundedCornerShape(18.dp),

            colors = CardDefaults.cardColors(

                containerColor = MaterialTheme.colorScheme.tertiary

            ),

            elevation = CardDefaults.cardElevation(6.dp)

        ) {

            Icon(

                icon,

                null,

                modifier = Modifier.padding(18.dp),

                tint = MaterialTheme.colorScheme.onTertiary

            )

        }

        Spacer(Modifier.height(8.dp))

        Text(

            title,

            color =
                if (isSystemInDarkTheme())
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onPrimary,

            style = MaterialTheme.typography.labelLarge

        )

    }

}
@Composable
private fun EmptyState(onCreateTrip: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Place,
            null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "No trips planned yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Start planning your next adventure!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onCreateTrip,
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Create Your First Trip", style = MaterialTheme.typography.titleMedium)
        }
    }
}
@Composable
private fun TripCard(
    trip: TripPlanEntity,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(22.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = trip.title.uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(10.dp))
                    val dark = isSystemInDarkTheme()
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            Icons.Default.LocationOn,
                            null,
                            tint = if (dark) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(Modifier.width(6.dp))

                        Text(
                            trip.destination,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (dark) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                StatusChip(trip.status)
            }

            Spacer(Modifier.height(10.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(Modifier.height(10.dp))

            TripInfoItem(
                icon = Icons.Default.DateRange,
                label = "Travel Dates",
                value = "${Dates.formatDate(trip.startDateEpochMs)} - ${
                    Dates.formatDate(trip.endDateEpochMs)
                }"
            )

            Spacer(Modifier.height(10.dp))

            TripInfoItem(
                icon = Icons.Default.Star,
                label = "Budget",
                value = Money.format(trip.budgetCap)
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {

                    Text(
                        text = trip.type.name,
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Icon(
                    Icons.Default.ArrowForward,
                    null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
@Composable
private fun StatusChip(
    status: TripStatus
) {
    val dark = isSystemInDarkTheme()
    val background = when (status) {
        TripStatus.DRAFT -> if (dark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
        TripStatus.PLANNED -> if (dark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
        TripStatus.COMPLETED -> if (dark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
    }

    val icon = when (status) {

        TripStatus.DRAFT ->
            Icons.Default.Edit

        TripStatus.PLANNED ->
            Icons.Default.CheckCircle

        TripStatus.COMPLETED ->
            Icons.Default.Done
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = background
    ) {

        Row(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                icon,
                null,
                modifier = Modifier.size(16.dp)
            )

            Spacer(Modifier.width(6.dp))

            Text(
                status.name,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
@Composable
private fun TripInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Surface(
            // Keep primaryContainer for Light, use surfaceContainer for Dark
            color = if (dark) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                icon,
                null,
                // Keep primary for Light, use onSurfaceVariant for Dark
                tint = if (dark) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(10.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column {

            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(2.dp))

            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}