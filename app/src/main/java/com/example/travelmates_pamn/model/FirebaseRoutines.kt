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

@Composable
fun getUserById(userId: String): User {
    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(userId) {
        val db = FirebaseFirestore.getInstance()
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            println(userDoc)
            user = userDoc?.toObject(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val userReturn: User = when (user) {
        null -> User()
        else -> user!!
    }

    return userReturn

}