package com.talos.forge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talos.forge.data.ErrorUtils
import com.talos.forge.data.Repository
import com.talos.forge.data.models.Supplement
import com.talos.forge.data.models.SupplementRequest
import com.talos.forge.data.models.SupplementAnalysis
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

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing

    private val _analysisResult = MutableStateFlow<SupplementAnalysis?>(null)
    val analysisResult: StateFlow<SupplementAnalysis?> = _analysisResult

    fun loadSupplements() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _supplements.value = repository.getSupplements()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
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
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun deleteSupplement(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteSupplement(id)
                loadSupplements()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun analyzeSupplementPhoto(base64Image: String) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _error.value = null
            try {
                _analysisResult.value = repository.analyzeSupplementPhoto(base64Image)
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun clearAnalysisResult() {
        _analysisResult.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
