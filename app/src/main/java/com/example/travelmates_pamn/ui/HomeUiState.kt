package com.example.travelmates_pamn.ui

import com.google.firebase.firestore.GeoPoint
import com.google.type.LatLng

data class HomeUiState(
    val username: String = "",
    val currentLocation: GeoPoint? = null,
    val friendsList: List<String> = emptyList()
)