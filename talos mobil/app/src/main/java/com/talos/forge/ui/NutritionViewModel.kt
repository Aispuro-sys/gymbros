package com.talos.forge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talos.forge.data.ErrorUtils
import com.talos.forge.data.Repository
import com.talos.forge.data.models.FoodAnalysis
import com.talos.forge.data.models.Meal
import com.talos.forge.data.models.MealRequest
import com.talos.forge.data.models.NutritionPlan
import com.talos.forge.data.models.WeeklyNutritionSummary
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

    private val _totalCarbs = MutableStateFlow(0)
    val totalCarbs: StateFlow<Int> = _totalCarbs

    private val _totalFats = MutableStateFlow(0)
    val totalFats: StateFlow<Int> = _totalFats

    private val _foodAnalysis = MutableStateFlow<FoodAnalysis?>(null)
    val foodAnalysis: StateFlow<FoodAnalysis?> = _foodAnalysis

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing

    private val _weeklySummary = MutableStateFlow<WeeklyNutritionSummary?>(null)
    val weeklySummary: StateFlow<WeeklyNutritionSummary?> = _weeklySummary

    private val _nutritionPlan = MutableStateFlow<NutritionPlan?>(null)
    val nutritionPlan: StateFlow<NutritionPlan?> = _nutritionPlan

    private val _isLoadingPlan = MutableStateFlow(false)
    val isLoadingPlan: StateFlow<Boolean> = _isLoadingPlan

    fun loadMeals() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val mealsList = repository.getMeals()
                _meals.value = mealsList
                _totalCalories.value = mealsList.sumOf { it.calories }
                _totalProtein.value = mealsList.sumOf { it.protein_g }
                _totalCarbs.value = mealsList.sumOf { it.carbs_g }
                _totalFats.value = mealsList.sumOf { it.fats_g }
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadWeeklySummary() {
        viewModelScope.launch {
            try {
                _weeklySummary.value = repository.getWeeklyNutritionSummary()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun analyzeFoodPhoto(base64Image: String) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _foodAnalysis.value = null
            try {
                _foodAnalysis.value = repository.analyzeFoodPhoto(base64Image)
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun clearFoodAnalysis() {
        _foodAnalysis.value = null
    }

    fun createMealFromAnalysis(analysis: FoodAnalysis, mealType: String) {
        viewModelScope.launch {
            try {
                repository.createMeal(MealRequest(
                    name = analysis.name,
                    meal_type = mealType,
                    calories = analysis.calories,
                    protein_g = analysis.protein_g,
                    carbs_g = analysis.carbs_g,
                    fats_g = analysis.fats_g
                ))
                loadMeals()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun loadNutritionPlan() {
        viewModelScope.launch {
            _isLoadingPlan.value = true
            try {
                _nutritionPlan.value = repository.generateNutritionPlan()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            } finally {
                _isLoadingPlan.value = false
            }
        }
    }

    fun addMealFromPlan(mealName: String, mealType: String, calories: Int, proteinG: Int, carbsG: Int, fatsG: Int) {
        viewModelScope.launch {
            try {
                repository.createMeal(MealRequest(
                    name = mealName,
                    meal_type = mealType,
                    calories = calories,
                    protein_g = proteinG,
                    carbs_g = carbsG,
                    fats_g = fatsG
                ))
                loadMeals()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun createMeal(request: MealRequest) {
        viewModelScope.launch {
            try {
                repository.createMeal(request)
                loadMeals()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun deleteMeal(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteMeal(id)
                loadMeals()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun confirmMeal(id: String) {
        viewModelScope.launch {
            try {
                repository.confirmMeal(id)
                loadMeals()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun clearError() { _error.value = null }
}
