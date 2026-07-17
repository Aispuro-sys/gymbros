package com.talos.forge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talos.forge.data.ErrorUtils
import com.talos.forge.data.Repository
import com.talos.forge.data.models.ExerciseDataset
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

    private val _searchResults = MutableStateFlow<List<ExerciseDataset>>(emptyList())
    val searchResults: StateFlow<List<ExerciseDataset>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val _aiMessage = MutableStateFlow<String?>(null)
    val aiMessage: StateFlow<String?> = _aiMessage

    enum class ViewMode { LIST, WEEKLY, MONTHLY }
    private val _viewMode = MutableStateFlow(ViewMode.LIST)
    val viewMode: StateFlow<ViewMode> = _viewMode

    private val _completedExercises = MutableStateFlow<Set<String>>(emptySet())
    val completedExercises: StateFlow<Set<String>> = _completedExercises

    fun loadRoutines() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _routines.value = repository.getRoutinesWithGifs()
            } catch (e: Exception) {
                try {
                    _routines.value = repository.getRoutines()
                } catch (e2: Exception) {
                    _error.value = ErrorUtils.getErrorMessage(e2)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun createRoutine(name: String) {
        viewModelScope.launch {
            try {
                repository.createRoutine(name)
                loadRoutines()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun deleteRoutine(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteRoutine(id)
                loadRoutines()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun addExercise(routineId: String, name: String, sets: Int, reps: String, restSeconds: Int, datasetId: String? = null) {
        viewModelScope.launch {
            try {
                repository.addExercise(routineId, ExerciseRequest(name, sets, reps, restSeconds, datasetId))
                loadRoutines()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun deleteExercise(routineId: String, exerciseId: String) {
        viewModelScope.launch {
            try {
                repository.deleteExercise(routineId, exerciseId)
                loadRoutines()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun searchExercises(query: String? = null, equipment: String? = null, target: String? = null) {
        viewModelScope.launch {
            _isSearching.value = true
            try {
                _searchResults.value = repository.searchExercises(query, null, equipment, target, 30)
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun findSubstitutes(target: String?, equipment: String? = null) {
        viewModelScope.launch {
            _isSearching.value = true
            try {
                _searchResults.value = repository.searchExercises(null, null, equipment, target, 20)
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun generateWeeklyPlan(daysPerWeek: Int = 4, equipment: String = "all", muscleGroups: List<String> = emptyList()) {
        viewModelScope.launch {
            _isGenerating.value = true
            _aiMessage.value = null
            try {
                val plan = repository.generateWeeklyPlan(daysPerWeek, equipment, null, muscleGroups)
                _aiMessage.value = plan.ai_notes ?: "Plan semanal generado"
                loadRoutines()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearAiMessage() {
        _aiMessage.value = null
    }

    fun toggleExerciseComplete(exerciseId: String) {
        _completedExercises.value = _completedExercises.value.toMutableSet().apply {
            if (contains(exerciseId)) remove(exerciseId) else add(exerciseId)
        }
    }

    fun isExerciseCompleted(exerciseId: String): Boolean = exerciseId in _completedExercises.value

    fun getRoutineProgress(routine: Routine): Float {
        if (routine.exercises.isEmpty()) return 0f
        val completed = routine.exercises.count { it.id in _completedExercises.value }
        return completed.toFloat() / routine.exercises.size
    }

    fun resetCompleted() {
        _completedExercises.value = emptySet()
    }
}
