package com.talos.forge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talos.forge.data.Repository
import com.talos.forge.data.models.CommunityPost
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

    fun loadFeed() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _posts.value = repository.getCommunityFeed()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createPost(content: String) {
        viewModelScope.launch {
            try {
                repository.createCommunityPost(content)
                loadFeed()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun replyToPost(postId: String, content: String) {
        viewModelScope.launch {
            try {
                repository.replyToPost(postId, content)
                loadFeed()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
