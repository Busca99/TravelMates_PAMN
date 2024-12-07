package com.example.travelmates_pamn.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelmates_pamn.model.fetchUserById
import com.example.travelmates_pamn.model.isFriend
import com.example.travelmates_pamn.model.isRequestSent
import com.example.travelmates_pamn.model.setFriendEntry
import com.google.firebase.auth.FirebaseAuth
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
                val authUser = FirebaseAuth.getInstance().currentUser
                _uiState.update {
                    it.copy(
                        user = user,
                        isLoading = false,
                        error = null,
                        isFriend = isFriend(user.id, authUser!!.uid),
                        friendRequestSent = isRequestSent(user.id, authUser.uid),
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
        viewModelScope.launch {
            try {
                setFriendEntry(receiverId = _uiState.value.user?.id!!, senderId = FirebaseAuth.getInstance().currentUser!!.uid)
                _uiState.update { currentState ->
                    currentState.copy(friendRequestSent = true)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to set friend entry in firebase"
                    )
                }
            }
        }
    }
}