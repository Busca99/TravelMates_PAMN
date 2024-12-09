package com.example.travelmates_pamn.ui

import com.example.travelmates_pamn.model.User
import org.osmdroid.views.MapView

data class HomeUiState(
    val authUser: User? = null,
    val nearbyUsers: List<User> = emptyList(),
    val mapView: MapView? = null,
    val isLoading: Boolean = true,
)