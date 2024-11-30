package com.example.travelmates_pamn

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.travelmates_pamn.ui.theme.TravelMates_PAMNTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class AuthActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AuthActivity", "onCreate started")

        auth = FirebaseAuth.getInstance()

        // Forza il logout di qualsiasi utente -- DA CAMBIARE
        auth.signOut()
        Log.d("AuthActivity", "Forced logout")

        if (auth.currentUser != null) {
            Log.d("AuthActivity", "User logged in: ${auth.currentUser}")
            Log.d("AuthActivity", "User already logged in, starting MainActivity")
            startMainActivity()
            return
        }

        Log.d("AuthActivity", "No user logged in, showing auth screen")

        setContent {
            TravelMates_PAMNTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthScreen(
                        onLoginSuccess = {
                            Toast.makeText(this, "Authentication successful", Toast.LENGTH_SHORT).show()
                            startMainActivity()
                        }
                    )
                }
            }
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}

@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var hometown by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isLogin) "Login" else "Register",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trim() },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        if (!isLogin) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = birthDate,
                onValueChange = { birthDate = it },
                label = { Text("Birth Date (DD/MM/YYYY)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = hometown,
                onValueChange = { hometown = it },
                label = { Text("Hometown") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Write something about yourself") },
                singleLine = false,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (isLogin) {
                    if (email.isEmpty() || password.isEmpty()) {
                        errorMessage = "Please fill all fields"
                        return@Button
                    }
                } else {
                    if (email.isEmpty() || password.isEmpty() || phoneNumber.isEmpty() || name.isEmpty() ||
                        birthDate.isEmpty() || hometown.isEmpty()) {
                        errorMessage = "Please fill all fields"
                        return@Button
                    }
                }

                isLoading = true
                errorMessage = null

                val auth = FirebaseAuth.getInstance()
                if (isLogin) {
                    // Login code rimane invariato
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                onLoginSuccess()
                            } else {
                                errorMessage = task.exception?.message ?: "Authentication failed"
                            }
                        }
                } else {
                    // Registrazione con dati aggiuntivi
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid
                                val db = FirebaseFirestore.getInstance()

                                // Calcola l'età dalla data di nascita
                                val age = try {
                                    val parts = birthDate.split("/")
                                    val birthYear = parts[2].toInt()
                                    val currentYear = java.time.Year.now().value
                                    currentYear - birthYear
                                } catch (e: Exception) {
                                    0
                                }

                                val newUser = hashMapOf(
                                    "id" to userId,
                                    "name" to name,
                                    "phoneNumber" to phoneNumber,
                                    "age" to age,
                                    "hometown" to hometown,
                                    "bio" to "",
                                    "photoUrl" to "",
                                    "location" to GeoPoint(0.0, 0.0),
                                    "bio" to bio
                                )

                                db.collection("users")
                                    .add(newUser)
                                    .addOnSuccessListener {
                                        isLoading = false
                                        onLoginSuccess()
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        errorMessage = "Registration successful but failed to create profile"
                                    }
                            } else {
                                isLoading = false
                                errorMessage = when {
                                    task.exception?.message?.contains("badly formatted") == true ->
                                        "Invalid email format"
                                    task.exception?.message?.contains("password") == true ->
                                        "Password should be at least 6 characters"
                                    else -> task.exception?.message ?: "Registration failed"
                                }
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (isLogin) "Login" else "Register")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                isLogin = !isLogin
                errorMessage = null
            }
        ) {
            Text(if (isLogin) "Need an account? Register" else "Have an account? Login")
        }
    }
}