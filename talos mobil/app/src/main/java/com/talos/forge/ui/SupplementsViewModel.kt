package com.talos.forge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talos.forge.data.Repository
import com.talos.forge.data.models.Supplement
import com.talos.forge.data.models.SupplementRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SupplementsViewModel(private val repository: Repository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _supplements = MutableStateFlow<List<Supplement>>(emptyList())
    val supplements: StateFlow<List<Supplement>> = _supplements

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadSupplements() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _supplements.value = repository.getSupplements()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createSupplement(request: SupplementRequest) {
        viewModelScope.launch {
            try {
                repository.createSupplement(request)
                loadSupplements()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteSupplement(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteSupplement(id)
                loadSupplements()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
