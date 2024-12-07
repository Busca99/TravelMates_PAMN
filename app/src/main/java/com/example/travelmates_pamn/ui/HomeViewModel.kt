package com.example.travelmates_pamn.ui

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.type.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ViewModel for Home Screen
class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Initialize user data on ViewModel creation
        fetchUserData()
    }

    private fun fetchUserData() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        _uiState.update { currentState ->
            currentState.copy(
                username = currentUser?.displayName ?: currentUser?.email?.substringBefore('@') ?: "User"
            )
        }

        // TODO: Implement location fetching logic
        // Example placeholder:
        _uiState.update { currentState ->
            currentState.copy(
                currentLocation = GeoPoint(40.7128, -74.0060) // New York City coordinates
            )
        }
    }

    fun navigateToPeopleInTown(navController: NavController) {
        navController.navigate("people_in_town_screen")
    }
}