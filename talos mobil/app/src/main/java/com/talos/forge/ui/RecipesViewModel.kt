package com.talos.forge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talos.forge.data.ErrorUtils
import com.talos.forge.data.Repository
import com.talos.forge.data.models.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecipesViewModel(private val repository: Repository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe

    private val _mealTypeFilter = MutableStateFlow("ANY")
    val mealTypeFilter: StateFlow<String> = _mealTypeFilter

    private val _shoppingListMessage = MutableStateFlow<String?>(null)
    val shoppingListMessage: StateFlow<String?> = _shoppingListMessage

    fun loadRecipes(search: String? = null, mealType: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _recipes.value = repository.getRecipes(search, mealType)
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearch(query: String) {
        _searchQuery.value = query
        loadRecipes(query.ifBlank { null }, _mealTypeFilter.value.takeIf { it != "ANY" })
    }

    fun updateMealTypeFilter(type: String) {
        _mealTypeFilter.value = type
        loadRecipes(_searchQuery.value.ifBlank { null }, type.takeIf { it != "ANY" })
    }

    fun selectRecipe(recipe: Recipe) {
        _selectedRecipe.value = recipe
    }

    fun clearSelectedRecipe() {
        _selectedRecipe.value = null
    }

    fun addToShoppingList(recipeId: String) {
        viewModelScope.launch {
            try {
                val aggregation = repository.generateShoppingList(listOf(recipeId))
                _shoppingListMessage.value = "Ingredientes agregados a tu lista de super"
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun clearShoppingMessage() { _shoppingListMessage.value = null }

    fun recommendWithAI() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _recipes.value = repository.recommendRecipes()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
