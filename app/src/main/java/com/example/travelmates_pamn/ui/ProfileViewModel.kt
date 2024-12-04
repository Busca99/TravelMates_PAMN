package com.example.travelmates_pamn.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        Log.d("profileViewModel", "get auth user")
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val currentUser = auth.currentUser
        Log.d("ProfileViewModel", "Current user ID: ${currentUser?.uid}")
        if (currentUser == null) {
            Log.e("ProfileViewModel", "No authenticated user found")
            _uiState.update {
                it.copy(
                    uid = "",
                    name = "No User",
                    hometown = "",
                    location = "",
                    bio = "",
                    selectedTags = emptyList(),
                    photoUrl = "",
                    age = ""
                )
            }
            return
        }

        // Log the exact document path we're trying to access
        val userDocRef = firestore.collection("users").document(currentUser.uid)
        Log.d("ProfileViewModel", "Attempting to fetch document at: users/${currentUser.uid}")

        userDocRef.get()
            .addOnSuccessListener { document ->
                Log.d("ProfileViewModel", "Document exists: ${document.exists()}")
                Log.d("ProfileViewModel", "Full document data: ${document.data}")

                val userData = document.data ?: mapOf()

                // Add more detailed logging for each field
                Log.d("ProfileViewModel", "Birthday: ${userData["birthday"]}")
                Log.d("ProfileViewModel", "Location: ${userData["location"]}")
                Log.d("ProfileViewModel", "Tags: ${userData["interests"]}")

                _uiState.update {
                    it.copy(
                        uid = currentUser.uid,
                        name = currentUser.displayName ?: userData["name"] as? String ?: "No Name",
                        hometown = userData["hometown"] as? String ?: "",
                        location = userData["location"] as? String ?: "",
                        bio = userData["bio"] as? String ?: "",
                        selectedTags = when (val tags = userData["interests"]) {
                            is List<*> -> tags.filterIsInstance<String>()
                            else -> emptyList()
                        },
                        photoUrl = currentUser.photoUrl?.toString() ?: "",
                        age = userData["age"] as? String ?: ""
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileViewModel", "Error fetching user profile", exception)
            }
    }


    fun updateProfile(
        name: String? = null,
        hometown: String? = null,
        location: String? = null,
        bio: String? = null,
        tags: List<String>? = null,
        birthday: String? = null
    ) {
        val currentUser = auth.currentUser ?: return

        // Update Firebase Authentication display name
        name?.let { newName ->
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()

            currentUser.updateProfile(profileUpdates)
        }

        // Update Firestore document
        val updateData = mutableMapOf<String, Any>()
        name?.let { updateData["name"] = it }
        hometown?.let { updateData["hometown"] = it }
        location?.let { updateData["location"] = it }
        bio?.let { updateData["bio"] = it }
        tags?.let { updateData["interests"] = it }
        birthday?.let { updateData["birthday"] = it }

        if (updateData.isNotEmpty()) {
            firestore.collection("users").document(currentUser.uid)
                .set(updateData, SetOptions.merge())
                .addOnSuccessListener {
                    // Update local UI state
                    _uiState.update { currentState ->
                        currentState.copy(
                            name = name ?: currentState.name,
                            hometown = hometown ?: currentState.hometown,
                            location = location ?: currentState.location,
                            bio = bio ?: currentState.bio,
                            selectedTags = tags ?: currentState.selectedTags,
                            age = birthday ?: currentState.age
                        )
                    }
                }
        }
    }

    fun toggleEditMode() {
        _uiState.update { currentState ->
            currentState.copy(isEditing = !currentState.isEditing)
        }
    }

    fun saveProfile() {
        val currentState = _uiState.value
        updateProfile(
            name = currentState.name,
            hometown = currentState.hometown,
            location = currentState.location,
            bio = currentState.bio,
            tags = currentState.selectedTags,
            birthday = currentState.age
        )
        toggleEditMode()
    }
}