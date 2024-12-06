package com.example.travelmates_pamn.ui

import com.example.travelmates_pamn.model.User


data class ShowProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isFriend: Boolean = false,
    val friendRequestSent: Boolean = false
)