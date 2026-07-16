package com.talos.forge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talos.forge.data.Repository
import com.talos.forge.data.models.Meal
import com.talos.forge.data.models.MealRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NutritionViewModel(private val repository: Repository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _meals = MutableStateFlow<List<Meal>>(emptyList())
    val meals: StateFlow<List<Meal>> = _meals

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _totalCalories = MutableStateFlow(0)
    val totalCalories: StateFlow<Int> = _totalCalories

    private val _totalProtein = MutableStateFlow(0)
    val totalProtein: StateFlow<Int> = _totalProtein

    fun loadMeals() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val mealsList = repository.getMeals()
                _meals.value = mealsList
                _totalCalories.value = mealsList.sumOf { it.calories }
                _totalProtein.value = mealsList.sumOf { it.protein_g }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createMeal(request: MealRequest) {
        viewModelScope.launch {
            try {
                repository.createMeal(request)
                loadMeals()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteMeal(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteMeal(id)
                loadMeals()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun confirmMeal(id: String) {
        viewModelScope.launch {
            try {
                repository.confirmMeal(id)
                loadMeals()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
