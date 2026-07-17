package com.talos.forge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talos.forge.data.ErrorUtils
import com.talos.forge.data.Repository
import com.talos.forge.data.models.CommunityPost
import com.talos.forge.data.models.CommunityUser
import com.talos.forge.data.models.CommunityUserProfileResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommunityViewModel(private val repository: Repository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _posts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val posts: StateFlow<List<CommunityPost>> = _posts

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _searchResults = MutableStateFlow<List<CommunityUser>>(emptyList())
    val searchResults: StateFlow<List<CommunityUser>> = _searchResults

    private val _userProfile = MutableStateFlow<CommunityUserProfileResponse?>(null)
    val userProfile: StateFlow<CommunityUserProfileResponse?> = _userProfile

    private val _isLoadingProfile = MutableStateFlow(false)
    val isLoadingProfile: StateFlow<Boolean> = _isLoadingProfile

    fun loadFeed() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _posts.value = repository.getCommunityFeed()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createPost(content: String, mediaUrl: String? = null, mediaType: String = "TEXT") {
        viewModelScope.launch {
            try {
                repository.createCommunityPost(content, mediaUrl, mediaType)
                loadFeed()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun replyToPost(postId: String, content: String, mediaUrl: String? = null, mediaType: String = "TEXT") {
        viewModelScope.launch {
            try {
                repository.replyToPost(postId, content, mediaUrl, mediaType)
                loadFeed()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun reactToPost(postId: String, emoji: String) {
        viewModelScope.launch {
            try {
                repository.reactToPost(postId, emoji)
                loadFeed()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                repository.deletePost(postId)
                loadFeed()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            try {
                _searchResults.value = if (query.length >= 2) repository.searchUsers(query) else emptyList()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _isLoadingProfile.value = true
            try {
                _userProfile.value = repository.getUserProfile(userId)
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            } finally {
                _isLoadingProfile.value = false
            }
        }
    }

    fun clearUserProfile() {
        _userProfile.value = null
    }

    fun clearError() { _error.value = null }
}
