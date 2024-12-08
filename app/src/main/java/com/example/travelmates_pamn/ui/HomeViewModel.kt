package com.example.travelmates_pamn.ui

import android.util.Log
import androidx.compose.material3.DrawerState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.travelmates_pamn.Screen
import com.example.travelmates_pamn.calculateDistance
import com.example.travelmates_pamn.model.User
import com.example.travelmates_pamn.model.fetchUserById
import com.example.travelmates_pamn.navigateToScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ViewModel for Home Screen
class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Initialize user data on ViewModel creation
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        authUser = fetchUserById(FirebaseAuth.getInstance().currentUser?.uid!!)
                    )

                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", e.message!!)
            }
        }
        //fetchUserData()
    }

//        // TODO: Implement location fetching logic
//        // Example placeholder:
////        _uiState.update { currentState ->
////            currentState.copy(
////                authUser.location = GeoPoint(40.7128, -74.0060) // New York City coordinates
////            )
////        }
//    }

    fun stopLoading() {
        _uiState.update {
            it.copy(
                isLoading = false,
            )
        }
    }

    fun fetchNearbyUsers() {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                if (_uiState.value.authUser?.location != GeoPoint(0.0, 0.0)) {
                    val allUsersQuery = db.collectionGroup("users")
                        .whereNotEqualTo("id", _uiState.value.authUser?.id)
                        .get()
                        .await()

                    val nearbyUsers = allUsersQuery.documents
                        .mapNotNull { it.toObject(User::class.java) }
                        .filter { user ->
                            calculateDistance(_uiState.value.authUser?.location!!, user.location) <= 30.0
                        }
                        .sortedBy { user ->
                            calculateDistance(_uiState.value.authUser?.location!!, user.location)
                        }
                        .take(4)

                    _uiState.update {
                        it.copy(
                            nearbyUsers = nearbyUsers,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching nearby users: ${e.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        //error = e.message
                    )
                }
            }
        }
    }

    fun navigateToPeopleInTown(
        navController: NavController,
        drawerState: DrawerState,
        scope: CoroutineScope
    ) {
        navigateToScreen(
            navController = navController,
            route = Screen.PeopleInTown.route,
            drawerState = drawerState,
            scope = scope
        )
    }
}