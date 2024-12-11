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
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.google.firebase.storage.FirebaseStorage
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext


class AuthActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AuthActivity", "onCreate started")

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            Log.d("AuthActivity", "User logged in: ${auth.currentUser}")

            // Verifica se l'utente esiste in Firestore
            val db = FirebaseFirestore.getInstance()
            db.collection("users")
                .whereEqualTo("id", auth.currentUser?.uid)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        // L'utente non esiste in Firestore, fai il logout
                        Log.d("AuthActivity", "User not found in Firestore, logging out")
                        auth.signOut()
                        setAuthScreen()
                    } else {
                        // L'utente esiste, procedi con MainActivity
                        Log.d("AuthActivity", "User found in Firestore, starting MainActivity")
                        startMainActivity()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("AuthActivity", "Error checking user in Firestore: ${e.message}")
                    // In caso di errore, per sicurezza fai il logout
                    auth.signOut()
                    setAuthScreen()
                }
            return
        }

        Log.d("AuthActivity", "No user logged in, showing auth screen")
        setAuthScreen()
    }

    private fun setAuthScreen() {
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
fun ImagePicker(
    imageUri: Uri?,
    onImageSelected: (Uri) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add photo"
                )
            }
        }

        TextButton(onClick = { launcher.launch("image/*") }) {
            Text(if (imageUri == null) "Select profile picture" else "Change picture")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestSelectionSection(
    selectedInterests: List<String>,
    onInterestsChange: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val availableInterests = context.resources.getStringArray(R.array.available_interests).toList()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Select your interests (max 5)",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (selectedInterests.size >= 5) {
            Text(
                text = "Maximum interests selected",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            maxItemsInEachRow = 3
        ) {
            availableInterests.forEach { interest ->
                val isSelected = selectedInterests.contains(interest)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) {
                            onInterestsChange(selectedInterests - interest)
                        } else if (selectedInterests.size < 5) {
                            onInterestsChange(selectedInterests + interest)
                        }
                    },
                    label = { Text(interest) },
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                )
            }
        }
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
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedInterests by remember { mutableStateOf(listOf<String>()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Funzione per caricare l'immagine
    fun uploadImage(uri: Uri, userId: String, onComplete: (String?) -> Unit) {
        Log.d("ImageUpload", "Starting image upload for user: $userId")
        Log.d("ImageUpload", "Image URI: $uri")

        try {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val imageRef = storageRef.child("profile_pictures/$userId.jpg")

            imageRef.putFile(uri)
                .addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                    Log.d("ImageUpload", "Upload is $progress% done")
                }
                .addOnFailureListener { exception ->
                    Log.e("ImageUpload", "Upload failed: ${exception.message}")
                    onComplete(null)
                }
                .addOnSuccessListener {
                    Log.d("ImageUpload", "Upload successful")
                    imageRef.downloadUrl
                        .addOnSuccessListener { downloadUrl ->
                            Log.d("ImageUpload", "Download URL: $downloadUrl")
                            onComplete(downloadUrl.toString())
                        }
                        .addOnFailureListener { urlException ->
                            Log.e("ImageUpload", "Failed to get URL: ${urlException.message}")
                            onComplete(null)
                        }
                }
        } catch (e: Exception) {
            Log.e("ImageUpload", "Error in uploadImage: ${e.message}")
            onComplete(null)
        }
    }

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
                onValueChange = {
                    if (it.length <= 13) phoneNumber = it
                },
                label = { Text("Phone number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    if (it.length <= 50) name = it
                },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            ImagePicker(
                imageUri = selectedImageUri,
                onImageSelected = { selectedImageUri = it }
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
                onValueChange = {
                    if (it.length <= 50) hometown = it
                },
                label = { Text("Hometown") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            InterestSelectionSection(
                selectedInterests = selectedInterests,
                onInterestsChange = { selectedInterests = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = {
                    if (it.length <= 700) bio = it
                },
                label = { Text("Write something about yourself") },
                supportingText = { Text("${bio.length}/700") },
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
                    // Controlli sui campi durante la registrazione
                    when {
                        email.isEmpty() || password.isEmpty() || phoneNumber.isEmpty() ||
                                name.isEmpty() || birthDate.isEmpty() || hometown.isEmpty() -> {
                            errorMessage = "Please fill all fields"
                            return@Button
                        }
                        phoneNumber.length > 13 -> {
                            errorMessage = "Phone number must be max 13 characters"
                            return@Button
                        }
                        name.length > 50 -> {
                            errorMessage = "Name must be max 50 characters"
                            return@Button
                        }
                        hometown.length > 50 -> {
                            errorMessage = "Hometown must be max 50 characters"
                            return@Button
                        }
                        bio.length > 700 -> {
                            errorMessage = "Bio must be max 700 characters"
                            return@Button
                        }
                    }
                }

                isLoading = true
                errorMessage = null

                val auth = FirebaseAuth.getInstance()
                if (isLogin) {
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

                                // Funzione per creare l'utente nel database
                                fun createUser(photoUrl: String = "") {
                                    val newUser = hashMapOf(
                                        "id" to userId,
                                        "name" to name,
                                        "phoneNumber" to phoneNumber,
                                        "birthday" to birthDate,
                                        "age" to age,
                                        "hometown" to hometown,
                                        "bio" to bio,
                                        "photoUrl" to photoUrl,
                                        "location" to GeoPoint(0.0, 0.0),
                                        "interests" to selectedInterests
                                    )

                                    db.collection("users")
                                        .document(userId!!).set(newUser)
                                        .addOnSuccessListener {
                                            Log.d("Registration", "User created successfully")
                                            isLoading = false
                                            onLoginSuccess()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("Registration", "Failed to create user: ${e.message}")
                                            isLoading = false
                                            errorMessage = "Registration successful but failed to create profile"
                                        }
                                }

                                // Se c'è un'immagine, caricala prima di creare l'utente
                                if (selectedImageUri != null && userId != null) {
                                    uploadImage(selectedImageUri!!, userId) { photoUrl ->
                                        if (photoUrl != null) {
                                            createUser(photoUrl)
                                        } else {
                                            Log.e("Registration", "Failed to upload image, creating user without photo")
                                            createUser()
                                        }
                                    }
                                } else {
                                    createUser()
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