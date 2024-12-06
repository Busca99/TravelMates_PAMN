package com.example.travelmates_pamn.ui

import android.net.Uri

data class ProfileUiState(
    val uid: String = "",
    val name: String = "No User",
    val age: String = "",
    val birthday: String = "",
    val hometown: String = "",
    val location: String = "",
    val bio: String = "About me!",
    val photoUrl: String = "",
    val selectedTags: List<String> = emptyList(),
    val isEditing: Boolean = false
)

// New data class for editing state
data class EditingProfileState(
    val name: String = "",
    val hometown: String = "",
    val location: String = "",
    val bio: String = "",
    val photoUri: Uri? = null,
    val selectedTags: List<String> = emptyList(),
    val birthday: String = ""
)