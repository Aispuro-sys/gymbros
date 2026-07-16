package com.talos.forge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talos.forge.data.Repository
import com.talos.forge.data.SessionManager
import com.talos.forge.data.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: Repository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadProfile() {
        viewModelScope.launch {
            try {
                _user.value = repository.getMe()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun logout() {
        sessionManager.clearToken()
        _user.value = null
    }
}
