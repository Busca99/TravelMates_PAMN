package com.example.travelmates_pamn.ui

import com.example.travelmates_pamn.model.User
import com.google.firebase.firestore.GeoPoint

data class HomeUiState(
    val authUser: User? = null,
    val nearbyUsers: List<User> = emptyList(),
    val isLoading: Boolean = true,
)