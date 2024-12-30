package com.ikhsannurhakiki.aplikasiforum.ui.preference

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences private constructor(private val dataStore: DataStore<Preferences>) {

    private val USERID_KEY = stringPreferencesKey("USERID")
    private val USER_NAME_KEY = stringPreferencesKey("USER_NAME")
    private val ACCESS_RIGHTS_KEY = stringPreferencesKey("ACCESS_RIGHTS")
    private val ISLOGGEDIN_KEY = booleanPreferencesKey("ISLOGGEDIN")


    suspend fun saveSession(user: Auth) {
        dataStore.edit { preferences ->
            preferences[USERID_KEY] = user.userId.toString()
            preferences[USER_NAME_KEY] = user.name.toString()
            preferences[ACCESS_RIGHTS_KEY] = user.accessRights
            preferences[ISLOGGEDIN_KEY] = user.isLoggedIn
        }
    }

    fun getSession(): Flow<Auth> {
        return dataStore.data.map { preferences ->
            val userId = preferences[USERID_KEY] ?: ""
            val name = preferences[USER_NAME_KEY] ?: ""
            val accessRights = preferences[ACCESS_RIGHTS_KEY] ?: ""
            val isLoggedIn = preferences[ISLOGGEDIN_KEY] ?: false
            Auth(userId ,name,accessRights, isLoggedIn)
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences[USERID_KEY] = ""
            preferences[USER_NAME_KEY] = ""
            preferences[ISLOGGEDIN_KEY] = false
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreferences? = null

        fun getInstance(dataStore: DataStore<Preferences>): UserPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}