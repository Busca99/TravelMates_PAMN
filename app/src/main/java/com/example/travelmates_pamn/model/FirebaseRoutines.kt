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

@Composable
fun getUserByIdxyz(userId: String, callback: (User?) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()

    // Fetch the document asynchronously
    firestore.collection("users")
        .document(userId)
        .get()
        .addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                // If the document exists, convert it to a User object and invoke the callback with the User
                val user = documentSnapshot.toObject(User::class.java)
                callback(user)
            } else {
                // If the user doesn't exist, invoke the callback with null
                Log.d("UserRetrieval", "User with ID $userId not found.")
                callback(null)
            }
        }
        .addOnFailureListener { exception ->
            // Handle any errors, such as network issues
            println("Error getting user: ${exception.message}")
            Log.d("UserRetrieval", "Error getting user: ${exception.message}")
            callback(null)
        }
}

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