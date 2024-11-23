package com.example.travelmates_pamn.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel : ViewModel() {
    // Mutable state that can be modified
    private val _uiState = MutableStateFlow(ProfileUiState())
    // Immutable state that can only be read by the UI
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun updateName(newName: String) {
        _uiState.update { currentState ->
            currentState.copy(name = newName)
        }
    }

    fun updateHometown(newTown: String) {
        _uiState.update { currentState ->
            currentState.copy(hometown = newTown)
        }
    }

    fun updateBio(newBio: String) {
        _uiState.update { currentState ->
            currentState.copy(bio = newBio)
        }
    }

    fun toggleEditMode() {
        _uiState.update { currentState ->
            currentState.copy(isEditing = !currentState.isEditing)
        }
    }

    // Other functions to update state
}