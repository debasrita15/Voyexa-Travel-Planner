package com.example.travelplanner.data.repo

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.travelplanner.data.prefs.AuthSession
import com.example.travelplanner.data.prefs.AuthSessionData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import java.util.UUID

sealed class AuthResult {
    data class Success(val email: String, val displayName: String, val role: String = "") : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(context: Context) {

    private val appContext = context.applicationContext
    private val authSession = AuthSession(appContext)

    private val credentialsPrefs by lazy {
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
        delay(800)

        val normalizedEmail = email.trim().lowercase()
        if (!isValidEmail(normalizedEmail)) {
            return AuthResult.Error("Please enter a valid email address")
        }
        if (password.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters")
        }

        val storedHash = credentialsPrefs.getString(credentialKey(normalizedEmail), null)
            ?: return AuthResult.Error("No account found with this email. Please sign up.")

        if (storedHash != hashPassword(password)) {
            return AuthResult.Error("Incorrect password. Please try again.")
        }

        val storedRole = credentialsPrefs.getString(roleKey(normalizedEmail), "Traveller") ?: "Traveller"
        if (storedRole != role) {
            return AuthResult.Error("This account is registered as a $storedRole. Please select the correct role.")
        }

        val displayName = credentialsPrefs.getString(displayNameKey(normalizedEmail), normalizedEmail)
            ?: normalizedEmail

        val userId = credentialsPrefs.getString(userIdKey(normalizedEmail), UUID.randomUUID().toString())
            ?: UUID.randomUUID().toString()

        authSession.saveSession(userId, normalizedEmail, displayName, role)
        return AuthResult.Success(normalizedEmail, displayName, role)
    }

    suspend fun register(email: String, password: String, confirmPassword: String, role: String): AuthResult {
        delay(800)

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

        if (credentialsPrefs.contains(credentialKey(normalizedEmail))) {
            return AuthResult.Error("An account with this email already exists")
        }

        val userId = UUID.randomUUID().toString()
        val displayName = normalizedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }

        credentialsPrefs.edit {
            putString(credentialKey(normalizedEmail), hashPassword(password))
            putString(displayNameKey(normalizedEmail), displayName)
            putString(userIdKey(normalizedEmail), userId)
            putString(roleKey(normalizedEmail), role)
        }

        authSession.saveSession(userId, normalizedEmail, displayName, role)
        return AuthResult.Success(normalizedEmail, displayName, role)
    }

    suspend fun forgotPassword(email: String): AuthResult {
        delay(1000)

        val normalizedEmail = email.trim().lowercase()
        if (!isValidEmail(normalizedEmail)) {
            return AuthResult.Error("Please enter a valid email address")
        }

        if (!credentialsPrefs.contains(credentialKey(normalizedEmail))) {
            return AuthResult.Error("No account found with this email")
        }

        return AuthResult.Success(normalizedEmail, "Reset link sent")
    }

    suspend fun signInWithGoogle(): AuthResult {
        delay(600)
        return AuthResult.Error("Google Sign-In requires Firebase OAuth setup. Use email instead.")
    }

    suspend fun logout() {
        authSession.clearSession()
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun credentialKey(email: String) = "cred_$email"
    private fun displayNameKey(email: String) = "name_$email"
    private fun userIdKey(email: String) = "uid_$email"
    private fun roleKey(email: String) = "role_$email"
}
