package com.example.travelmates_pamn.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.DrawerState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.travelmates_pamn.Screen
import com.example.travelmates_pamn.calculateDistance
import com.example.travelmates_pamn.model.User
import com.example.travelmates_pamn.model.fetchUserById
import com.example.travelmates_pamn.navigateToScreen
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
                        .take(6)

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

    @SuppressLint("MissingPermission")
    fun updateLocationMap(context: Context) {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(isLoading = true)
                }

                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnSuccessListener
                            val db = FirebaseFirestore.getInstance()
                            val geoPoint = GeoPoint(location.latitude, location.longitude)

                            db.collection("users")
                                .whereEqualTo("id", currentUserId)
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    if (!querySnapshot.isEmpty) {
                                        val document = querySnapshot.documents.first()
                                        db.collection("users")
                                            .document(document.id)
                                            .update("location", geoPoint)
                                            .addOnSuccessListener {
                                                // Update local UI state with new location
                                                _uiState.update {
                                                    it.copy(
                                                        authUser = it.authUser?.copy(location = geoPoint),
                                                        isLoading = false
                                                    )
                                                }
                                                fetchNearbyUsers()
                                                Toast.makeText(context, "Location updated successfully", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("HomeViewModel", "Error updating location: ${e.message}")
                                                _uiState.update {
                                                    it.copy(isLoading = false)
                                                }
                                                Toast.makeText(context, "Could not get location", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                        } else {
                            _uiState.update {
                                it.copy(isLoading = false)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("HomeViewModel", "Error getting location: ${e.message}")
                        _uiState.update {
                            it.copy(isLoading = false)
                        }
                    }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error updating location: ${e.message}")
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

}