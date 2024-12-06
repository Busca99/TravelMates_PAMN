package com.example.travelmates_pamn.model

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

suspend fun fetchUserById(userId: String): User {
    val firestore = FirebaseFirestore.getInstance()

    return try {
        val documentSnapshot = firestore.collection("users")
            .document(userId)
            .get()
            .await()

        documentSnapshot.toObject(User::class.java)
            ?: throw NoSuchElementException("User not found")
    } catch (e: Exception) {
        // You might want to log the error here
        throw IllegalStateException("Failed to retrieve user: ${e.message}")
    }
}