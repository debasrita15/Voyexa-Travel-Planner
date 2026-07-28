package com.example.travelplanner.data.repo

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.travelplanner.data.prefs.AuthSession
import com.example.travelplanner.data.prefs.AuthSessionData
import com.example.travelplanner.util.Supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.util.UUID

sealed class AuthResult {
    data class Success(val email: String, val displayName: String, val role: String = "") : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(context: Context) {

    private val appContext = context.applicationContext
    private val authSession = AuthSession(appContext)

    private val userPrefs by lazy {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            appContext,
            "auth_credentials",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun observeSession(): Flow<AuthSessionData> = authSession.observe()

    suspend fun login(email: String, password: String, role: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        if (!isValidEmail(normalizedEmail)) {
            return AuthResult.Error("Please enter a valid email address")
        }
        if (password.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters")
        }

        return try {
            // Attempt Supabase Authentication
            Supabase.client.auth.signInWith(Email) {
                this.email = normalizedEmail
                this.password = password
            }

            val user = Supabase.client.auth.currentUserOrNull()
            val userId = user?.id ?: UUID.randomUUID().toString()

            val metaRole = user?.userMetadata?.get("role")?.jsonPrimitive?.content
            val metaDisplayName = user?.userMetadata?.get("display_name")?.jsonPrimitive?.content

            val finalRole = metaRole ?: userPrefs.getString(roleKey(normalizedEmail), role) ?: role
            if (role.isNotEmpty() && finalRole != role) {
                return AuthResult.Error("This account is registered as a $finalRole. Please select the correct role.")
            }

            val displayName = metaDisplayName
                ?: userPrefs.getString(displayNameKey(normalizedEmail), normalizedEmail.substringBefore("@").replaceFirstChar { it.uppercase() })
                ?: normalizedEmail

            userPrefs.edit {
                putString(displayNameKey(normalizedEmail), displayName)
                putString(userIdKey(normalizedEmail), userId)
                putString(roleKey(normalizedEmail), finalRole)
            }

            authSession.saveSession(userId, normalizedEmail, displayName, finalRole)
            try {
                com.example.travelplanner.app.AppGraph.tripRepository.clearAllLocalData()
                com.example.travelplanner.app.AppGraph.guideRepository.resetGuideState()
                com.example.travelplanner.app.AppGraph.tripRepository.syncTripsWithCloud()
                com.example.travelplanner.app.AppGraph.settingsRepository.syncProfileWithCloud()
            } catch (_: Exception) {}

            AuthResult.Success(normalizedEmail, displayName, finalRole)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Authentication failed. Please check your credentials and network connection.")
        }
    }

    suspend fun register(
        email: String,
        password: String,
        confirmPassword: String,
        role: String,
        displayName: String = ""
    ): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        if (!isValidEmail(normalizedEmail)) {
            return AuthResult.Error("Please enter a valid email address")
        }
        if (password.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters")
        }
        if (password != confirmPassword) {
            return AuthResult.Error("Passwords do not match")
        }

        val finalDisplayName = displayName.ifBlank {
            normalizedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        }

        return try {
            // Register user with Supabase Auth
            Supabase.client.auth.signUpWith(Email) {
                this.email = normalizedEmail
                this.password = password
                this.data = buildJsonObject {
                    put("role", role)
                    put("display_name", finalDisplayName)
                }
            }

            val user = Supabase.client.auth.currentUserOrNull()
            val userId = user?.id ?: UUID.randomUUID().toString()

            userPrefs.edit {
                putString(displayNameKey(normalizedEmail), finalDisplayName)
                putString(userIdKey(normalizedEmail), userId)
                putString(roleKey(normalizedEmail), role)
            }

            authSession.saveSession(userId, normalizedEmail, finalDisplayName, role)
            try {
                com.example.travelplanner.app.AppGraph.tripRepository.clearAllLocalData()
                com.example.travelplanner.app.AppGraph.guideRepository.resetGuideState()
                com.example.travelplanner.app.AppGraph.tripRepository.syncTripsWithCloud()
                com.example.travelplanner.app.AppGraph.settingsRepository.syncProfileWithCloud()
            } catch (_: Exception) {}

            AuthResult.Success(normalizedEmail, finalDisplayName, role)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Registration failed. An account with this email may already exist.")
        }
    }

    suspend fun forgotPassword(email: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        if (!isValidEmail(normalizedEmail)) {
            return AuthResult.Error("Please enter a valid email address")
        }

        return try {
            Supabase.client.auth.resetPasswordForEmail(normalizedEmail)
            AuthResult.Success(normalizedEmail, "Reset link sent")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to send reset email")
        }
    }

    suspend fun signInWithGoogle(): AuthResult {
        return AuthResult.Error("Google OAuth can be enabled in Supabase Dashboard -> Authentication -> Providers.")
    }

    suspend fun logout() {
        try {
            Supabase.client.auth.signOut()
        } catch (_: Exception) {}
        authSession.clearSession()
        try {
            com.example.travelplanner.app.AppGraph.tripRepository.clearAllLocalData()
            com.example.travelplanner.app.AppGraph.guideRepository.resetGuideState()
        } catch (_: Exception) {}
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun displayNameKey(email: String) = "name_$email"
    private fun userIdKey(email: String) = "uid_$email"
    private fun roleKey(email: String) = "role_$email"
}