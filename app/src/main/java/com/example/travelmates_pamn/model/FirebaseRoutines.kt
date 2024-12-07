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

suspend fun isFriend(userIdA: String, userIdB: String): Boolean {
    val db = FirebaseFirestore.getInstance()

    // Check if there's an accepted friendship document between the two users
    val friendshipQuery = db.collection("friendships")
        .whereArrayContainsAny("userIds", listOf(userIdA, userIdB))
        .whereEqualTo("status", "accepted")
        .get()
        .await()

    // Verify that the retrieved document actually contains BOTH user IDs
    return friendshipQuery.documents.any { document ->
        val userIds = document.get("userIds") as? List<String> ?: emptyList()
        userIds.containsAll(listOf(userIdA, userIdB))
    }
}

suspend fun isRequestSent(userIdA: String, userIdB: String): Boolean {
    val db = FirebaseFirestore.getInstance()

    // Check if there's an pending friendship document between the two users
    val friendshipQuery = db.collection("friendships")
        .whereArrayContainsAny("userIds", listOf(userIdA, userIdB))
        .whereEqualTo("status", "pending")
        .get()
        .await()

    // Verify that the retrieved document actually contains BOTH user IDs
    return friendshipQuery.documents.any { document ->
        val userIds = document.get("userIds") as? List<String> ?: emptyList()
        userIds.containsAll(listOf(userIdA, userIdB))
    }
}


suspend fun setFriendEntry(senderId: String, receiverId: String): Unit {
    val db = FirebaseFirestore.getInstance()

    // If no existing friendship exists, create a new friendship request
    if (! isFriend(senderId, receiverId)) {
        val friendshipDocument = hashMapOf(
            "userIds" to listOf(senderId, receiverId),
            "sender" to senderId,
            "status" to "pending"
        )

        db.collection("friendships")
            .add(friendshipDocument)
            .await()
    }
}