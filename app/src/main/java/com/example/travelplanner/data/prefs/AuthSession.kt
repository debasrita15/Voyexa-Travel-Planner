package com.example.travelplanner.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore("auth_session")

data class AuthSessionData(
    val isLoggedIn: Boolean = false,
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: String = "Traveller"
)

class AuthSession(private val context: Context) {

    private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    private val KEY_USER_ID = stringPreferencesKey("user_id")
    private val KEY_EMAIL = stringPreferencesKey("email")
    private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
    private val KEY_ROLE = stringPreferencesKey("role")

    fun observe(): Flow<AuthSessionData> {
        return context.authDataStore.data.map { prefs ->
            AuthSessionData(
                isLoggedIn = prefs[KEY_IS_LOGGED_IN] ?: false,
                userId = prefs[KEY_USER_ID].orEmpty(),
                email = prefs[KEY_EMAIL].orEmpty(),
                displayName = prefs[KEY_DISPLAY_NAME].orEmpty(),
                role = prefs[KEY_ROLE] ?: "Traveller"
            )
        }
    }

    suspend fun saveSession(
        userId: String,
        email: String,
        displayName: String,
        role: String
    ) {
        context.authDataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_USER_ID] = userId
            prefs[KEY_EMAIL] = email
            prefs[KEY_DISPLAY_NAME] = displayName
            prefs[KEY_ROLE] = role
        }
    }

    suspend fun clearSession() {
        context.authDataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = false
            prefs[KEY_USER_ID] = ""
            prefs[KEY_EMAIL] = ""
            prefs[KEY_DISPLAY_NAME] = ""
            prefs[KEY_ROLE] = ""
        }
    }
}
