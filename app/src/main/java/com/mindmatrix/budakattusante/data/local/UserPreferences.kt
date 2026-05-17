package com.mindmatrix.budakattusante.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mindmatrix.budakattusante.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val SHOULD_SHOW_ONBOARDING_KEY = booleanPreferencesKey("should_show_onboarding")
    }

    val userRole: Flow<UserRole> = context.dataStore.data.map { preferences ->
        val roleStr = preferences[USER_ROLE_KEY] ?: UserRole.NONE.name
        try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            UserRole.NONE
        }
    }

    val shouldShowOnboarding: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOULD_SHOW_ONBOARDING_KEY] ?: true
    }

    suspend fun saveUserRole(role: UserRole) {
        context.dataStore.edit { preferences ->
            preferences[USER_ROLE_KEY] = role.name
        }
    }

    suspend fun saveOnboardingCompleted() {
        context.dataStore.edit { preferences ->
            preferences[SHOULD_SHOW_ONBOARDING_KEY] = false
        }
    }
}
