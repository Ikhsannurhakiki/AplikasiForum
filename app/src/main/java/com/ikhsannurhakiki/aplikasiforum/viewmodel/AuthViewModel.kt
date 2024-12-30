package com.ikhsannurhakiki.aplikasiforum.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Auth
import com.ikhsannurhakiki.aplikasiforum.ui.preference.UserPreferences
import kotlinx.coroutines.launch

class AuthViewModel(private val pref: UserPreferences) : ViewModel() {

    fun saveSession(user: Auth) {
        viewModelScope.launch {
            pref.saveSession(user)
        }
    }

    fun getSession(): LiveData<Auth> {
        return pref.getSession().asLiveData()
    }

    fun clearSession() {
        viewModelScope.launch {
            pref.clearSession()
        }
    }
}