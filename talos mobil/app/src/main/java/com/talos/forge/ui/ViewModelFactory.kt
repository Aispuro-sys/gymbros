package com.talos.forge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.talos.forge.data.ApiClient
import com.talos.forge.data.ApiService
import com.talos.forge.data.Repository
import com.talos.forge.TalosApp
import com.talos.forge.data.SessionManager

class ViewModelFactory(
    private val repository: Repository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(repository, sessionManager) as T
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(repository) as T
            modelClass.isAssignableFrom(RoutinesViewModel::class.java) ->
                RoutinesViewModel(repository) as T
            modelClass.isAssignableFrom(NutritionViewModel::class.java) ->
                NutritionViewModel(repository) as T
            modelClass.isAssignableFrom(SupplementsViewModel::class.java) ->
                SupplementsViewModel(repository) as T
            modelClass.isAssignableFrom(RecipesViewModel::class.java) ->
                RecipesViewModel(repository) as T
            modelClass.isAssignableFrom(ShoppingViewModel::class.java) ->
                ShoppingViewModel(repository) as T
            modelClass.isAssignableFrom(CommunityViewModel::class.java) ->
                CommunityViewModel(repository) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                ProfileViewModel(repository, sessionManager) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        fun create(application: TalosApp): ViewModelFactory {
            val apiService = ApiClient.retrofit.create(ApiService::class.java)
            val repository = Repository(apiService)
            return ViewModelFactory(repository, application.sessionManager)
        }
    }
}
