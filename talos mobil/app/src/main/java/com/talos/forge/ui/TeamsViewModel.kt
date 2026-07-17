package com.talos.forge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talos.forge.data.ErrorUtils
import com.talos.forge.data.Repository
import com.talos.forge.data.models.Routine
import com.talos.forge.data.models.Team
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TeamsViewModel(private val repository: Repository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> = _teams

    private val _selectedTeam = MutableStateFlow<Team?>(null)
    val selectedTeam: StateFlow<Team?> = _selectedTeam

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _routines = MutableStateFlow<List<Routine>>(emptyList())
    val routines: StateFlow<List<Routine>> = _routines

    fun loadTeams() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _teams.value = repository.getTeams()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTeamDetail(teamId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _selectedTeam.value = repository.getTeamDetail(teamId)
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadRoutines() {
        viewModelScope.launch {
            try {
                _routines.value = repository.getRoutines()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun createTeam(name: String) {
        viewModelScope.launch {
            try {
                repository.createTeam(name)
                loadTeams()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun joinTeam(inviteCode: String) {
        viewModelScope.launch {
            try {
                repository.joinTeam(inviteCode)
                loadTeams()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun shareRoutine(teamId: String, routineId: String) {
        viewModelScope.launch {
            try {
                repository.shareRoutine(teamId, routineId)
                loadTeamDetail(teamId)
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun copyRoutine(teamId: String, routineId: String) {
        viewModelScope.launch {
            try {
                repository.copyRoutine(teamId, routineId)
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun createPost(teamId: String, content: String) {
        viewModelScope.launch {
            try {
                repository.createTeamPost(teamId, content)
                loadTeamDetail(teamId)
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun leaveTeam(teamId: String) {
        viewModelScope.launch {
            try {
                repository.leaveTeam(teamId)
                _selectedTeam.value = null
                loadTeams()
            } catch (e: Exception) {
                _error.value = ErrorUtils.getErrorMessage(e)
            }
        }
    }

    fun clearError() { _error.value = null }
    fun clearSelectedTeam() { _selectedTeam.value = null }
}
