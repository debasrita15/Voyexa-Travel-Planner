package com.example.travelplanner.ui.screens.guide

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.travelplanner.ui.viewmodel.guide.GuideProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideProfileScreen(
    onBack: () -> Unit,
    viewModel: GuideProfileViewModel = viewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val scrollState = rememberScrollState()

    var isEditing by remember { mutableStateOf(false) }

    var editBio by remember { mutableStateOf("") }
    var editName by remember { mutableStateOf("") }
    var editLanguages by remember { mutableStateOf("") }
    var editContactNumber by remember { mutableStateOf("") }
    var editAvatarUrl by remember { mutableStateOf("") }
    var showPhotoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(profile) {
        if (!isEditing && profile.name.isNotBlank()) {
            editName = profile.name
            editBio = profile.bio
            editLanguages = profile.languages.joinToString(", ")
            editContactNumber = profile.contactNumber
            editAvatarUrl = profile.avatarUrl
        }
    }

    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            title = { Text("Update Profile Photo", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter image URL or photo path:", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = editAvatarUrl,
                        onValueChange = { editAvatarUrl = it },
                        label = { Text("Photo URL") },
                        placeholder = { Text("https://example.com/photo.jpg") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val langs = editLanguages.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        viewModel.updateProfile(editBio, editName.ifBlank { profile.name }, langs, editContactNumber, editAvatarUrl) {
                            showPhotoDialog = false
                        }
                    }
                ) {
                    Text("Save Photo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhotoDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guide Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(
                            onClick = {
                                val langs = editLanguages.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                viewModel.updateProfile(editBio, editName, langs, editContactNumber, editAvatarUrl) {
                                    isEditing = false
                                }
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                        }
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
            // Profile Header Panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(100.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isEditing) editName.take(2).uppercase() else profile.name.take(2).uppercase(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        FilledIconButton(
                            onClick = { showPhotoDialog = true },
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = "Upload Photo", modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    if (isEditing) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Display Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editContactNumber,
                            onValueChange = { editContactNumber = it },
                            label = { Text("Contact Number") },
                            placeholder = { Text("+1 (555) 019-2834") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Phone, null) }
                        )
                    } else {
                        Text(profile.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(profile.contactNumber, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Official Verified Guide", style = MaterialTheme.typography.bodySmall, color = Color(0xFF10B981))
                        }
                    }
                }
            }

            // About Bio Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("About Biography", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (isEditing) {
                        OutlinedTextField(
                            value = editBio,
                            onValueChange = { editBio = it },
                            label = { Text("Bio description") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 5
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editLanguages,
                            onValueChange = { editLanguages = it },
                            label = { Text("Languages spoken (comma separated)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editContactNumber,
                            onValueChange = { editContactNumber = it },
                            label = { Text("Contact Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Phone, null) }
                        )
                    } else {
                        Text(
                            text = profile.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Languages", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text(
                            text = profile.languages.joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Certifications Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Certifications & Experience", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Years active: ${profile.experienceYears} Years", style = MaterialTheme.typography.bodyMedium)

                    profile.certifications.forEach { cert ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(cert, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Documents Verification and Emergency Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Security & Verification Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Govt ID Uploaded")
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Background Check")
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
