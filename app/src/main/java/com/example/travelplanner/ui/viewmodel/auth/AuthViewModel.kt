package com.example.travelplanner.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelplanner.app.AppGraph
import com.example.travelplanner.data.prefs.AuthSessionData
import com.example.travelplanner.data.repo.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthFormState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val role: String = "Traveller",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val generalError: String? = null,
    val successMessage: String? = null
)

class AuthViewModel : ViewModel() {

    private val authRepository = AppGraph.authRepository

    val session: StateFlow<AuthSessionData> = authRepository.observeSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthSessionData())

    private val _formState = MutableStateFlow(AuthFormState())
    val formState: StateFlow<AuthFormState> = _formState.asStateFlow()

    fun updateEmail(value: String) {
        _formState.value = _formState.value.copy(email = value, emailError = null, generalError = null)
    }

    fun updatePassword(value: String) {
        _formState.value = _formState.value.copy(password = value, passwordError = null, generalError = null)
    }

    fun updateConfirmPassword(value: String) {
        _formState.value = _formState.value.copy(
            confirmPassword = value,
            confirmPasswordError = null,
            generalError = null
        )
    }

    fun updateRole(value: String) {
        _formState.value = _formState.value.copy(role = value, generalError = null)
    }

    fun clearForm() {
        _formState.value = AuthFormState()
    }

    fun clearMessages() {
        _formState.value = _formState.value.copy(generalError = null, successMessage = null)
    }

    fun login(onSuccess: (String) -> Unit) {
        val state = _formState.value
        val emailError = validateEmail(state.email)
        val passwordError = validatePassword(state.password)

        if (emailError != null || passwordError != null) {
            _formState.value = state.copy(emailError = emailError, passwordError = passwordError)
            return
        }

        viewModelScope.launch {
            _formState.value = state.copy(isLoading = true, generalError = null)
            when (val result = authRepository.login(state.email, state.password, state.role)) {
                is AuthResult.Success -> {
                    _formState.value = AuthFormState()
                    onSuccess(result.role)
                }
                is AuthResult.Error -> {
                    _formState.value = _formState.value.copy(isLoading = false, generalError = result.message)
                }
            }
        }
    }

    fun register(onSuccess: (String) -> Unit) {
        val state = _formState.value
        val emailError = validateEmail(state.email)
        val passwordError = validatePassword(state.password)
        val confirmError = when {
            state.confirmPassword.isBlank() -> "Please confirm your password"
            state.confirmPassword != state.password -> "Passwords do not match"
            else -> null
        }

        if (emailError != null || passwordError != null || confirmError != null) {
            _formState.value = state.copy(
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmError
            )
            return
        }

        viewModelScope.launch {
            _formState.value = state.copy(isLoading = true, generalError = null)
            when (val result = authRepository.register(state.email, state.password, state.confirmPassword, state.role)) {
                is AuthResult.Success -> {
                    _formState.value = AuthFormState()
                    onSuccess(result.role)
                }
                is AuthResult.Error -> {
                    _formState.value = _formState.value.copy(isLoading = false, generalError = result.message)
                }
            }
        }
    }

    fun forgotPassword(onSuccess: () -> Unit) {
        val state = _formState.value
        val emailError = validateEmail(state.email)

        if (emailError != null) {
            _formState.value = state.copy(emailError = emailError)
            return
        }

        viewModelScope.launch {
            _formState.value = state.copy(isLoading = true, generalError = null, successMessage = null)
            when (val result = authRepository.forgotPassword(state.email)) {
                is AuthResult.Success -> {
                    _formState.value = _formState.value.copy(
                        isLoading = false,
                        successMessage = "Password reset link sent to ${result.email}"
                    )
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _formState.value = _formState.value.copy(isLoading = false, generalError = result.message)
                }
            }
        }
    }

    fun signInWithGoogle(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _formState.value = _formState.value.copy(isLoading = true, generalError = null)
            when (val result = authRepository.signInWithGoogle()) {
                is AuthResult.Success -> {
                    _formState.value = AuthFormState()
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _formState.value = _formState.value.copy(isLoading = false, generalError = result.message)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            clearForm()
        }
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Enter a valid email address"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }
}
