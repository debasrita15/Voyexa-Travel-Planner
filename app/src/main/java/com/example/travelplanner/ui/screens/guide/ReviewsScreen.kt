package com.example.travelplanner.ui.screens.guide

import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelplanner.data.repo.ReviewItem
import com.example.travelplanner.ui.viewmodel.guide.ReviewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    onBack: () -> Unit,
    viewModel: ReviewsViewModel = viewModel()
) {
    val reviewList by viewModel.reviews.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Reviews Received", "Reviews Written")

    var selectedFilter by remember { mutableStateOf("Newest") }
    var showReplyDialogForReview by remember { mutableStateOf<ReviewItem?>(null) }
    var replyText by remember { mutableStateOf("") }

    val filteredReviews = remember(reviewList, selectedTab, selectedFilter) {
        val isWrittenByMe = selectedTab == 1
        var list = reviewList.filter { it.isWrittenByMe == isWrittenByMe }

        list = when (selectedFilter) {
            "Highest Rated" -> list.sortedByDescending { it.rating }
            "Lowest Rated" -> list.sortedBy { it.rating }
            else -> list // Newest / Default sorted in repo
        }
        list
    }

    if (showReplyDialogForReview != null) {
        AlertDialog(
            onDismissRequest = { showReplyDialogForReview = null },
            title = { Text("Reply to Traveller", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Responding to ${showReplyDialogForReview!!.travellerName}'s review:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "\"${showReplyDialogForReview!!.reviewText}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Light
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text("Your Response") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.replyToReview(showReplyDialogForReview!!.id, replyText)
                        replyText = ""
                        showReplyDialogForReview = null
                    }
                ) {
                    Text("Submit Reply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReplyDialogForReview = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Traveller Reviews", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Header Selection
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Rating Overview Distribution (Only show for reviews received)
                if (selectedTab == 0) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("4.9", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                                    Spacer(Modifier.height(4.dp))
                                    Row {
                                        repeat(5) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB000), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text("26 reviews", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                VerticalDivider(modifier = Modifier.height(80.dp).padding(horizontal = 16.dp))

                                Column(
                                    modifier = Modifier.weight(1.5f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    RatingBarRow(5, 0.90f)
                                    RatingBarRow(4, 0.08f)
                                    RatingBarRow(3, 0.02f)
                                    RatingBarRow(2, 0.00f)
                                    RatingBarRow(1, 0.00f)
                                }
                            }
                        }
                    }
                }

                // 2. Filter row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedTab == 0) "What travellers say" else "Your feedback to travellers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Simple filter dropdown action
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            onClick = {
                                selectedFilter = if (selectedFilter == "Newest") "Highest Rated" else if (selectedFilter == "Highest Rated") "Lowest Rated" else "Newest"
                            }
                        ) {
                            Text(
                                text = selectedFilter,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // 3. Reviews list
                if (filteredBookingsEmpty(filteredReviews)) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No reviews in this section.")
                        }
                    }
                } else {
                    items(filteredReviews) { review ->
                        ReviewItemCard(
                            review = review,
                            showReplyButton = selectedTab == 0,
                            onReplyClick = {
                                showReplyDialogForReview = review
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun filteredBookingsEmpty(list: List<ReviewItem>): Boolean {
    return list.isEmpty()
}

@Composable
private fun RatingBarRow(stars: Int, progress: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$stars", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(12.dp))
        Spacer(Modifier.width(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            color = Color(0xFFFFB000),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}

@Composable
private fun ReviewItemCard(
    review: ReviewItem,
    showReplyButton: Boolean,
    onReplyClick: () -> Unit
) {
    Card(
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
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(review.travellerName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("${review.tourName} • ${review.date}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

            Spacer(Modifier.height(12.dp))
            Text(review.reviewText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Show reply if it exists
            if (review.replyText != null) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Your response:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        Text(review.replyText, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else if (showReplyButton) {
                Spacer(Modifier.height(12.dp))
                TextButton(
                    onClick = onReplyClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reply to Traveller")
                }
            }
        }
    }
}
