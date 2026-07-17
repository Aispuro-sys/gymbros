package com.talos.forge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talos.forge.data.ErrorUtils
import com.talos.forge.data.Repository
import com.talos.forge.data.SessionManager
import com.talos.forge.data.models.ProfileUpdateRequest
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

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess

    fun loadProfile() {
        viewModelScope.launch {
            try {
                _user.value = repository.getMe()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun updateProfile(
        username: String? = null,
        age: Int? = null,
        heightCm: Float? = null,
        weightKg: Float? = null,
        goal: String? = null,
        bodyType: String? = null,
        gender: String? = null,
        bio: String? = null,
        phone: String? = null,
        profilePhoto: String? = null
    ) {
        viewModelScope.launch {
            _isUpdating.value = true
            _updateSuccess.value = false
            try {
                _user.value = repository.updateProfile(
                    ProfileUpdateRequest(
                        username = username,
                        age = age,
                        height_cm = heightCm,
                        weight_kg = weightKg,
                        goal = goal,
                        body_type = bodyType,
                        gender = gender,
                        bio = bio,
                        phone = phone,
                        profile_photo = profilePhoto
                    )
                )
                _updateSuccess.value = true
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearUpdateSuccess() {
        _updateSuccess.value = false
    }

    fun logout() {
        sessionManager.clearToken()
        _user.value = null
    }
}
