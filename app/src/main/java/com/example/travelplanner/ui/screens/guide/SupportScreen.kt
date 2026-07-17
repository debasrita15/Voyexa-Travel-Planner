package com.example.travelplanner.ui.screens.guide

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    var faqQuery by remember { mutableStateOf("") }
    var reportSubject by remember { mutableStateOf("") }
    var reportDescription by remember { mutableStateOf("") }
    var showReportSuccess by remember { mutableStateOf(false) }

    val faqs = remember {
        listOf(
            FAQItem("How do I receive payments?", "Payments are processed directly to your bank account 24 hours after a tour is successfully completed. Review your withdrawable balance in the Earnings tab."),
            FAQItem("What is the booking cancellation policy?", "If a traveller cancels within 24 hours of the tour start time, you receive a 50% payout. Otherwise, it is fully refunded."),
            FAQItem("How can I update my working hours?", "Navigate to the Availability tab on the dashboard to configure specific weekly hours, set vacation modes, or block off dates."),
            FAQItem("How do I contact support?", "You can contact our support team 24/7 via live chat, email support@voyexa.com, or call +1 (800) 555-0199.")
        )
    }

    val filteredFaqs = remember(faqQuery) {
        if (faqQuery.isBlank()) faqs else {
            faqs.filter { it.question.contains(faqQuery, ignoreCase = true) || it.answer.contains(faqQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Partner Support", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Quick Support Channels
            Text("Contact Channels", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SupportChannelCard("Live Chat", Icons.Default.ChatBubble, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                SupportChannelCard("Call Us", Icons.Default.Phone, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                SupportChannelCard("Email Us", Icons.Default.Email, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
            }

            // FAQs Search
            Text("Frequently Asked Questions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = faqQuery,
                onValueChange = { faqQuery = it },
                placeholder = { Text("Search FAQs...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                singleLine = true
            )

            // FAQs display
            filteredFaqs.forEach { faq ->
                FAQExpanderItem(faq = faq)
            }

            // Report Issue Form Card
            Text("Report an Issue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (showReportSuccess) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFD1FAE5),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF065F46))
                        Spacer(Modifier.height(8.dp))
                        Text("Issue Report Received", fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                        Text("Our support team will review your report and reply within 12 hours.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF065F46))
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = reportSubject,
                            onValueChange = { reportSubject = it },
                            label = { Text("Subject / Issue title") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = reportDescription,
                            onValueChange = { reportDescription = it },
                            label = { Text("Describe the problem...") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 4
                        )
                        Button(
                            onClick = {
                                if (reportSubject.isNotBlank()) {
                                    showReportSuccess = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Submit Report")
                        }
                    }
                }
            }

            // Footer App version
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Voyexa Partner App v2.4.1", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Terms & Privacy Policy • Guide Guidelines", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun SupportChannelCard(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = {},
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FAQExpanderItem(faq: FAQItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(faq.question, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                Text(faq.answer, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

data class FAQItem(
    val question: String,
    val answer: String
)
