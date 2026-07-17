package com.talos.forge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talos.forge.data.ErrorUtils
import com.talos.forge.data.Repository
import com.talos.forge.data.models.ShoppingList
import com.talos.forge.data.models.ShoppingListSaveItem
import com.talos.forge.data.models.ShoppingIngredient
import com.talos.forge.data.models.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShoppingViewModel(private val repository: Repository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _shoppingList = MutableStateFlow<ShoppingList?>(null)
    val shoppingList: StateFlow<ShoppingList?> = _shoppingList

    private val _ingredients = MutableStateFlow<List<ShoppingIngredient>>(emptyList())
    val ingredients: StateFlow<List<ShoppingIngredient>> = _ingredients

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _shareUrl = MutableStateFlow<String?>(null)
    val shareUrl: StateFlow<String?> = _shareUrl

    fun loadShoppingList() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = repository.getShoppingList()
                _shoppingList.value = list
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateFromAI() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val recommendedRecipes = repository.recommendRecipes()
                _recipes.value = recommendedRecipes
                if (recommendedRecipes.isNotEmpty()) {
                    val aggregation = repository.generateShoppingList(recommendedRecipes.map { it.id })
                    _ingredients.value = aggregation.ingredients
                    saveList(aggregation.ingredients)
                }
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateFromRecipes(recipeIds: List<String>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val aggregation = repository.generateShoppingList(recipeIds)
                _ingredients.value = aggregation.ingredients
                _recipes.value = aggregation.recipes
                saveList(aggregation.ingredients)
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun saveList(ingredients: List<ShoppingIngredient>) {
        viewModelScope.launch {
            try {
                val items = ingredients.map { ing ->
                    ShoppingListSaveItem(
                        name = ing.name,
                        quantity = ing.quantity.ifBlank { null },
                        checked = false,
                        recipe_names = ing.recipes
                    )
                }
                val saved = repository.saveShoppingList(items)
                _shoppingList.value = saved
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun toggleItem(itemId: String, checked: Boolean) {
        val list = _shoppingList.value ?: return
        viewModelScope.launch {
            try {
                repository.toggleShoppingItem(list.id, itemId, checked)
                loadShoppingList()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun shareList() {
        val list = _shoppingList.value ?: return
        viewModelScope.launch {
            try {
                val response = repository.shareShoppingList(list.id)
                _shareUrl.value = response.shareUrl
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun clearList() {
        val list = _shoppingList.value ?: return
        viewModelScope.launch {
            try {
                repository.deleteShoppingList(list.id)
                _shoppingList.value = null
                _ingredients.value = emptyList()
                _recipes.value = emptyList()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun addItem(name: String, quantity: String?) {
        val list = _shoppingList.value
        viewModelScope.launch {
            try {
                if (list != null) {
                    val updated = repository.addShoppingItem(list.id, name, quantity)
                    _shoppingList.value = updated
                } else {
                    val saved = repository.saveShoppingList(listOf(
                        ShoppingListSaveItem(name = name, quantity = quantity, checked = false)
                    ))
                    _shoppingList.value = saved
                }
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun deleteItem(itemId: String) {
        val list = _shoppingList.value ?: return
        viewModelScope.launch {
            try {
                val updated = repository.deleteShoppingItem(list.id, itemId)
                _shoppingList.value = updated
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun clearShareUrl() {
        _shareUrl.value = null
    }
}
