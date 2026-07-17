package com.example.travelplanner.ui.screens.guide

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.travelplanner.ui.viewmodel.guide.EarningsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarningsScreen(
    onBack: () -> Unit,
    viewModel: EarningsViewModel = viewModel()
) {
    val earnings by viewModel.earnings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSuccessWithdraw by remember { mutableStateOf(false) }

    // Mock Transaction List
    val transactions = remember {
        listOf(
            Transaction("TX1009", "Old Goa Heritage Walk", "July 16, 2026", 180.0, "Completed"),
            Transaction("TX1008", "Spice Plantation Private tour", "July 11, 2026", 210.0, "Completed"),
            Transaction("TX1007", "Historic Churches Excursion", "July 09, 2026", 140.0, "Completed"),
            Transaction("TX1006", "Goa Heritage Walk", "July 06, 2026", 180.0, "Completed")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Earnings Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Action to export PDF/Excel
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Export")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Main Withdrawable Balance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Withdrawable Balance",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "$${earnings.withdrawableBalance.toInt()}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = {
                                if (earnings.withdrawableBalance > 0) {
                                    viewModel.withdrawBalance {
                                        showSuccessWithdraw = true
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimary,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = earnings.withdrawableBalance > 0
                        ) {
                            Text("Withdraw to Bank", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Withdraw Successful alert
            if (showSuccessWithdraw) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFD1FAE5),
                        border = BorderStroke(1.dp, Color(0xFF10B981))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF065F46))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Withdrawal Request Sent", fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                                Text("Funds will arrive in your bank account in 2-3 business days.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF065F46))
                            }
                        }
                    }
                }
            }

            // Stat Cards Breakdown
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EarningsStatCard("Today", "$${earnings.todayEarnings.toInt()}", Modifier.weight(1f))
                    EarningsStatCard("This Week", "$${earnings.weeklyEarnings.toInt()}", Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EarningsStatCard("This Month", "$${earnings.monthlyEarnings.toInt()}", Modifier.weight(1f))
                    EarningsStatCard("Total Earned", "$${earnings.totalEarnings.toInt()}", Modifier.weight(1f))
                }
            }

            // Target Progress Tracker
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Commission & Goal Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Voyexa Commission (10%)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("-$${(earnings.totalEarnings * earnings.commissionRate).toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Completed Tours Count", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${earnings.completedToursCount} tours", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Transaction History header
            item {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // History entries
            items(transactions) { tx ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(tx.description, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(2.dp))
                            Text("${tx.date} • ${tx.id}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = "+$${tx.amount.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EarningsStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        }
    }
}

data class Transaction(
    val id: String,
    val description: String,
    val date: String,
    val amount: Double,
    val status: String
)
