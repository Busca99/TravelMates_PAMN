package com.example.travelmates_pamn.model

import com.google.firebase.firestore.GeoPoint

data class User(
    val id: String = "",
    val name: String = "",
    val age: Int = 0,
    val hometown: String = "",
    val currentCity: String = "",
    val photoUrl: String = "",
    val bio: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0)
)