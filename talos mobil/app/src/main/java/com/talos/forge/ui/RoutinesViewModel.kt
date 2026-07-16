package com.talos.forge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talos.forge.data.Repository
import com.talos.forge.data.models.ExerciseRequest
import com.talos.forge.data.models.Routine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoutinesViewModel(private val repository: Repository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _routines = MutableStateFlow<List<Routine>>(emptyList())
    val routines: StateFlow<List<Routine>> = _routines

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadRoutines() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _routines.value = repository.getRoutines()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createRoutine(name: String) {
        viewModelScope.launch {
            try {
                repository.createRoutine(name)
                loadRoutines()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteRoutine(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteRoutine(id)
                loadRoutines()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun addExercise(routineId: String, name: String, sets: Int, reps: String, restSeconds: Int) {
        viewModelScope.launch {
            try {
                repository.addExercise(routineId, ExerciseRequest(name, sets, reps, restSeconds))
                loadRoutines()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteExercise(routineId: String, exerciseId: String) {
        viewModelScope.launch {
            try {
                repository.deleteExercise(routineId, exerciseId)
                loadRoutines()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
