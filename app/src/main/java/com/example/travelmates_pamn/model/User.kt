package com.example.travelmates_pamn.model

import com.google.firebase.firestore.GeoPoint

data class User(
    val id: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val age: Int = 0,
    val birthday: String = "",
    val hometown: String = "",
    val photoUrl: String = "",
    val bio: String = "",
    val interests: List<String> = listOf<String>(),
    val location: GeoPoint = GeoPoint(0.0, 0.0)
)