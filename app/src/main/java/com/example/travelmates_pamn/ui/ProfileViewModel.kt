package com.example.travelmates_pamn.ui

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Separate state for editing to allow more flexible modifications
    private val _editingState = MutableStateFlow(EditingProfileState())
    val editingState: StateFlow<EditingProfileState> = _editingState.asStateFlow()

    // state for showing warnings
    private val _warningState = MutableStateFlow<String?>(null)
    val warningState: StateFlow<String?> = _warningState.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            resetToDefaultState()
            return
        }

        val userDocRef = firestore.collection("users").document(currentUser.uid)
        userDocRef.get()
            .addOnSuccessListener { document ->
                val userData = document.data ?: mapOf()

                val birthday = userData["birthday"] as? String ?: ""
                val calculatedAge = calculateAge(birthday)

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
                        photoUrl = userData["photoUrl"] as? String ?: "",
                        // currentUser.photoUrl?.toString() ?: "",
                        birthday = birthday,
                        age = calculatedAge
                    )
                }

                // Initialize editing state with current UI state
                _editingState.update {
                    EditingProfileState(
                        name = _uiState.value.name,
                        hometown = _uiState.value.hometown,
                        location = _uiState.value.location,
                        bio = _uiState.value.bio,
                        selectedTags = _uiState.value.selectedTags,
                        birthday = _uiState.value.birthday,
                        photoUri = Uri.parse(_uiState.value.photoUrl)
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileViewModel", "Error fetching user profile", exception)
                resetToDefaultState()
            }
    }

    private fun resetToDefaultState() {
        _uiState.update {
            it.copy(
                uid = "",
                name = "No User",
                hometown = "",
                location = "",
                bio = "",
                selectedTags = emptyList(),
                photoUrl = "",
                age = "",
                birthday = ""
            )
        }
    }

    fun updateEditingField(
        name: String? = null,
        hometown: String? = null,
        location: String? = null,
        bio: String? = null,
        tags: List<String>? = null,
        birthday: String? = null,
        photoUri: Uri? = null
    ) {
        _warningState.value = null  // Clear any previous warnings

        // Validate tags
        tags?.let { newTags ->
            if (newTags.size > 5) {
                _warningState.value = "You can select a maximum of 5 interests"
                return
            }
        }

        _editingState.update { currentState ->
            currentState.copy(
                name = name ?: currentState.name,
                hometown = hometown ?: currentState.hometown,
                location = location ?: currentState.location,
                bio = bio ?: currentState.bio,
                selectedTags = tags ?: currentState.selectedTags,
                birthday = birthday ?: currentState.birthday,
                photoUri = photoUri ?: currentState.photoUri
            )
        }
    }

    fun uploadProfilePhoto(photoUri: Uri) {
        val currentUser = auth.currentUser ?: return
        val photoRef = storage.reference.child("profile_photos/${currentUser.uid}")

        photoRef.putFile(photoUri)
            .continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                photoRef.downloadUrl
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result

                    // Update Firebase Authentication
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setPhotoUri(downloadUri)
                        .build()
                    currentUser.updateProfile(profileUpdates)

                    // Update Firestore
                    firestore.collection("users").document(currentUser.uid)
                        .update("photoUrl", downloadUri.toString())

                    // Update UI states
                    _uiState.update { it.copy(photoUrl = downloadUri.toString()) }
                    _editingState.update { it.copy(photoUri = null) }
                } else {
                    // Handle error
                    _warningState.value = "Failed to upload photo"
                }
            }
    }

    fun saveProfile() {
        val currentUser = auth.currentUser ?: return
        val editState = _editingState.value

        // Validate tags before saving
        if (editState.selectedTags.size > 5) {
            _warningState.value = "You can select a maximum of 5 interests"
            return
        }

        // Prepare update data
        val updateData = mutableMapOf<String, Any>()
        updateData["name"] = editState.name
        updateData["hometown"] = editState.hometown
        updateData["location"] = editState.location
        updateData["bio"] = editState.bio
        updateData["interests"] = editState.selectedTags
        updateData["birthday"] = editState.birthday

        // Update Firebase Authentication display name
        currentUser.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(editState.name)
                .build()
        )

        // Upload photo if a new one is selected
        if (editState.photoUri.toString() != _uiState.value.photoUrl) {
            editState.photoUri?.let {
                uploadProfilePhoto(it)
            }
        }


        // Update Firestore
        firestore.collection("users").document(currentUser.uid)
            .set(updateData, SetOptions.merge())
            .addOnSuccessListener {
                // Update UI state after successful save
                _uiState.update { currentState ->
                    currentState.copy(
                        name = editState.name,
                        hometown = editState.hometown,
                        location = editState.location,
                        bio = editState.bio,
                        selectedTags = editState.selectedTags,
                        birthday = editState.birthday,
                        age = calculateAge(editState.birthday),
                        isEditing = false
                    )
                }

                // Reset editing state
                _editingState.value = EditingProfileState()
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileViewModel", "Failed to save profile", exception)
                _warningState.value = "Failed to save profile"
            }
    }


    fun toggleEditMode() {
        _uiState.update { it.copy(isEditing = !it.isEditing) }

        // Reset or initialize editing state when toggling edit mode
        if (_uiState.value.isEditing) {
            _editingState.value = EditingProfileState(
                name = _uiState.value.name,
                hometown = _uiState.value.hometown,
                location = _uiState.value.location,
                bio = _uiState.value.bio,
                selectedTags = _uiState.value.selectedTags,
                birthday = _uiState.value.birthday,
                photoUri = Uri.parse(_uiState.value.photoUrl)
            )
        }
    }

    private fun calculateAge(birthday: String): String {
        if (birthday.isEmpty()) return ""
        return try {
            val birthdayDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(birthday)
            val today = Calendar.getInstance()
            val birthCalendar = Calendar.getInstance()
            birthdayDate?.let { birthCalendar.time = it }

            var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--
            }

            age.toString()
        } catch (e: Exception) {
            ""
        }
    }

    fun clearWarning() {
        _warningState.value = null // Update the state here
    }
}

