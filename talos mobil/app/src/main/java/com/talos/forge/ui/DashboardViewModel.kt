package com.talos.forge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talos.forge.data.Repository
import com.talos.forge.data.models.MacrosLog
import com.talos.forge.data.models.Meal
import com.talos.forge.data.models.Routine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: Repository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _macros = MutableStateFlow<MacrosLog?>(null)
    val macros: StateFlow<MacrosLog?> = _macros

    private val _meals = MutableStateFlow<List<Meal>>(emptyList())
    val meals: StateFlow<List<Meal>> = _meals

    private val _routines = MutableStateFlow<List<Routine>>(emptyList())
    val routines: StateFlow<List<Routine>> = _routines

    fun loadDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _macros.value = repository.getMacros()
                _meals.value = repository.getMeals()
                _routines.value = repository.getRoutines()
            } catch (e: Exception) {
                // ignore
            } finally {
                _isLoading.value = false
            }
        }
    }
}
