package com.example.travelmates_pamn.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelmates_pamn.model.fetchUserById
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShowProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ShowProfileUiState())
    val uiState: StateFlow<ShowProfileUiState> = _uiState.asStateFlow()

    fun fetchUser(userId: String) {
        viewModelScope.launch {
            try {
                val user = fetchUserById(userId)
                _uiState.update {
                    it.copy(
                        user = user,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        user = null,
                        isLoading = false,
                        error = e.message ?: "Failed to fetch user"
                    )
                }
            }
        }
    }

    fun sendFriendRequest() {
        // todo: send request in firebase or something
        _uiState.update { currentState ->
            currentState.copy(friendRequestSent = true)
        }
    }

}