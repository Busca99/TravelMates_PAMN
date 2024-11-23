package com.example.travelmates_pamn.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ShowProfileViewModel : ViewModel() {
    // Mutable state that can be modified
    private val _uiState = MutableStateFlow(ShowProfileUiState())
    // Immutable state that can only be read by the UI
    val uiState: StateFlow<ShowProfileUiState> = _uiState.asStateFlow()

    fun sendFriendRequest() {
        // todo: send request in firebase or something
        _uiState.update { currentState ->
            currentState.copy(friendRequestSent = true)
        }
    }

}