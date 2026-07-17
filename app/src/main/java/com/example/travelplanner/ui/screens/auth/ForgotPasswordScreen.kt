package com.example.travelplanner.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelplanner.ui.components.auth.AuthButton
import com.example.travelplanner.ui.components.auth.AuthScaffold
import com.example.travelplanner.ui.components.auth.AuthTextField
import com.example.travelplanner.ui.viewmodel.auth.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(formState.generalError) {
        formState.generalError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(formState.successMessage) {
        formState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    AuthScaffold(
        title = "Reset Password",
        subtitle = "Enter your email and we'll send you a reset link"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.Start),
                enabled = !formState.isLoading
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            AuthTextField(
                value = formState.email,
                onValueChange = viewModel::updateEmail,
                label = "Email",
                leadingIcon = Icons.Default.Email,
                errorMessage = formState.emailError,
                enabled = !formState.isLoading,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
                onImeAction = { viewModel.forgotPassword {} }
            )

            AuthButton(
                text = "Send Reset Link",
                onClick = { viewModel.forgotPassword {} },
                modifier = Modifier.fillMaxWidth(),
                isLoading = formState.isLoading
            )

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                enabled = !formState.isLoading
            ) {
                Text(
                    text = "Back to Sign In",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    SnackbarHost(hostState = snackbarHostState)
}
