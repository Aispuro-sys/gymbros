package com.talos.forge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talos.forge.data.Repository
import com.talos.forge.data.SessionManager
import com.talos.forge.data.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: Repository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoggedIn = MutableStateFlow(sessionManager.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        if (sessionManager.isLoggedIn()) {
            loadCurrentUser()
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.login(email, password)
                sessionManager.saveToken(response.token)
                _currentUser.value = response.user
                _isLoggedIn.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al iniciar sesión"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(
        username: String, email: String, password: String,
        phone: String? = null, age: Int? = null,
        heightCm: Float? = null, weightKg: Float? = null,
        goal: String = "MAINTENANCE", gender: String = "M"
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.register(
                    username, email, password, phone, age, heightCm, weightKg, goal, gender
                )
                sessionManager.saveToken(response.token)
                _currentUser.value = response.user
                _isLoggedIn.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al registrarse"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                _currentUser.value = repository.getMe()
                _isLoggedIn.value = true
            } catch (e: Exception) {
                logout()
            }
        }
    }

    fun logout() {
        sessionManager.clearToken()
        _currentUser.value = null
        _isLoggedIn.value = false
    }

    fun clearError() {
        _error.value = null
    }
}
