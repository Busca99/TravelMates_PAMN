package com.example.travelmates_pamn.ui

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

/**
 * var name by remember { mutableStateOf("Lisa Meyer") }
 *     var age by remember { mutableStateOf("date") }
 *     var hometown by remember { mutableStateOf("Ohio") }
 *     var location by remember { mutableStateOf("Las Palmas, Spain") }
 *     var bio by remember { mutableStateOf("I like traveling!") }
 *     var selectedTags by remember { mutableStateOf(listOf<String>()) }
 *     var birthday by remember { mutableStateOf("2001-01-01") }
 */