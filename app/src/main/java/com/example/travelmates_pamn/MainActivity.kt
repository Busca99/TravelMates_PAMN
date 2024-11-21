package com.example.travelmates_pamn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.foundation.Image
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.travelmates_pamn.ui.theme.TravelMates_PAMNTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.material3.CircularProgressIndicator
import com.example.travelmates_pamn.model.User
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import com.google.firebase.firestore.GeoPoint
import kotlin.math.*
import android.util.Log
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializza Firebase Auth
        val auth = FirebaseAuth.getInstance()

        // Esegui login anonimo se l'utente non è già loggato
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Login riuscito, avvia l'app
                        startApp()
                    } else {
                        // Gestisci errore
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            // Utente già loggato, avvia l'app
            startApp()
        }
    }

    private fun startApp() {
        enableEdgeToEdge()
        setContent {
            TravelMates_PAMNTheme {
                MainApp()
            }
        }
    }
}

// Screen route definitions
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object PeopleInTown : Screen("people_in_town")
    object Profile : Screen("profile")
    object Friends : Screen("friends")
    object IncomingRequests : Screen("incoming_requests")
}

// Empty screen composables
@Composable
fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Home Screen")
    }
}

private fun calculateDistance(loc1: GeoPoint, loc2: GeoPoint): Double {
    val R = 6371.0 // Raggio della Terra in km
    val lat1 = Math.toRadians(loc1.latitude)
    val lat2 = Math.toRadians(loc2.latitude)
    val dLat = Math.toRadians(loc2.latitude - loc1.latitude)
    val dLon = Math.toRadians(loc2.longitude - loc1.longitude)

    val a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(lat1) * Math.cos(lat2) *
            Math.sin(dLon/2) * Math.sin(dLon/2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))

    return R * c
}

@Composable
fun PeopleInTownScreen() {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Posizione di test (Valencia)
    val currentLocation = GeoPoint(39.4699, -0.3763)

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        try {
            val snapshot = db.collection("users").get().await()
            users = snapshot.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)
            }.filter { user ->
                calculateDistance(currentLocation, user.location) <= 30.0
            }.sortedBy { user ->
                calculateDistance(currentLocation, user.location)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(users) { user ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable {
                            // Azione quando la riga è cliccata
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(color = Color.Green, shape = CircleShape)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${user.age}, from ${user.hometown}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = "Distance: ${calculateDistance(currentLocation, user.location).toInt()} km",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ProfileScreen() {
    // ToDo: add exchange with login
    // get birthday
    var name by remember { mutableStateOf("Lisa Meyer") }
    var age by remember { mutableStateOf("date") }
    var hometown by remember { mutableStateOf("Ohio") }
    var location by remember { mutableStateOf("Las Palmas, Spain") }
    var bio by remember { mutableStateOf("I like traveling!") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Profile Picture
            Image(
                painter = painterResource(id = R.drawable.default_profile), // Replace with your drawable
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Name
            TextBoxForProfile(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Age
            TextBoxForProfile(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Home Town
            TextBoxForProfile(
                value = hometown,
                onValueChange = { hometown = it },
                label = { Text("Hometown") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextBoxForProfile(
                value = location,
                onValueChange = {location = it},
                label = {Text("Current Location")}
            )

            Spacer(modifier = Modifier.height(8.dp))

            MultiLineTextBoxForProfile(
                value = bio,
                onValueChange = {bio = it},
                label = {Text("About Me")}
            )
        }
    }
}

@Composable
fun TextBoxForProfile(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        singleLine = true
    )
}

@Composable
fun MultiLineTextBoxForProfile(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    maxLines: Int = 4,  // Default to 4 lines, you can adjust this
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        singleLine = false,  // This enables multi-line input
        minLines = 3,
        maxLines = maxLines,
        //textStyle = TextStyle(fontSize = 16.sp),
        shape = RoundedCornerShape(8.dp)
    )
}


@Composable
fun FriendsScreen() {
    var friends by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // ID dell'utente corrente (per ora hardcoded, poi lo prenderemo dall'auth)
    val currentUserId = "user1"

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        try {
            // Ottieni tutte le amicizie accettate dove l'utente corrente è uno dei due amici
            val friendships = db.collection("friendships")
                .whereArrayContains("userIds", currentUserId)
                .whereEqualTo("status", "accepted")
                .get()
                .await()

            // Estrai gli ID degli amici
            val friendIds = friendships.documents.flatMap { doc ->
                doc.get("userIds") as List<String>
            }.filter { it != currentUserId }

            // Ottieni i dettagli degli utenti amici
            if (friendIds.isNotEmpty()) {
                val friendsSnapshots = db.collection("users")
                    .whereIn("id", friendIds)
                    .get()
                    .await()

                friends = friendsSnapshots.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(friends) { friend ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable {
                            // Azione quando la riga è cliccata
                        }
                ) {
                    // Cerchio verde (placeholder per la foto profilo)
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(color = Color.Green, shape = CircleShape)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Dettagli dell'amico
                    Column {
                        Text(
                            text = friend.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${friend.age}, from ${friend.hometown}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IncomingRequestsScreen() {
    var incomingRequests by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val currentUserId = "user1"

    // Funzione per ricaricare le richieste
    val loadRequests = suspend {
        val db = FirebaseFirestore.getInstance()
        val friendships = db.collection("friendships")
            .whereArrayContains("userIds", currentUserId)
            .whereEqualTo("status", "pending")
            .whereNotEqualTo("sender", currentUserId)
            .get()
            .await()

        val senderIds = friendships.documents.map { doc ->
            doc.getString("sender")
        }.filterNotNull()

        if (senderIds.isNotEmpty()) {
            val sendersSnapshots = db.collection("users")
                .whereIn("id", senderIds)
                .get()
                .await()

            incomingRequests = sendersSnapshots.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)
            }
        } else {
            incomingRequests = emptyList()
        }
    }

    // Carica le richieste iniziali
    LaunchedEffect(Unit) {
        try {
            loadRequests()
        } catch (e: Exception) {
            Log.e("IncomingRequests", "Errore: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    fun acceptFriendRequest(senderId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("friendships")
            .whereArrayContains("userIds", currentUserId)
            .whereEqualTo("sender", senderId)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.update("status", "accepted")
                        .addOnSuccessListener {
                            // Rimuovi l'utente dalla lista locale
                            incomingRequests = incomingRequests.filter { it.id != senderId }
                        }
                }
            }
    }

    fun rejectFriendRequest(senderId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("friendships")
            .whereArrayContains("userIds", currentUserId)
            .whereEqualTo("sender", senderId)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            // Rimuovi l'utente dalla lista locale
                            incomingRequests = incomingRequests.filter { it.id != senderId }
                        }
                }
            }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (incomingRequests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No incoming friend requests")
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(incomingRequests) { sender ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(color = Color.Green, shape = CircleShape)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = sender.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${sender.age}, from ${sender.hometown}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = {
                            rejectFriendRequest(sender.id)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Reject",
                                tint = Color.Red
                            )
                        }

                        IconButton(onClick = {
                            acceptFriendRequest(sender.id)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Accept",
                                tint = Color.Green
                            )
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Menu", modifier = Modifier.padding(16.dp))
                HorizontalDivider()

                // Navigation drawer items
                NavigationDrawerItem(
                    label = { Text(text = "Home") },
                    selected = currentRoute == Screen.Home.route,
                    onClick = {
                        navigateToScreen(navController, Screen.Home.route, drawerState, scope)
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = "View People in Town") },
                    selected = currentRoute == Screen.PeopleInTown.route,
                    onClick = {
                        navigateToScreen(navController, Screen.PeopleInTown.route, drawerState, scope)
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = "My Profile") },
                    selected = currentRoute == Screen.Profile.route,
                    onClick = {
                        navigateToScreen(navController, Screen.Profile.route, drawerState, scope)
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Friends") },
                    selected = currentRoute == Screen.Friends.route,
                    onClick = {
                        navigateToScreen(navController, Screen.Friends.route, drawerState, scope)
                    }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Incoming Requests") },
                    selected = currentRoute == Screen.IncomingRequests.route,
                    onClick = {
                        navigateToScreen(navController, Screen.IncomingRequests.route, drawerState, scope)
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("TravelMates") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    }
                )
            }
        ) { contentPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(contentPadding)
            ) {
                composable(Screen.Home.route) { HomeScreen() }
                composable(Screen.PeopleInTown.route) { PeopleInTownScreen() }
                composable(Screen.Profile.route) { ProfileScreen() }
                composable(Screen.Friends.route) { FriendsScreen() }
                composable(Screen.IncomingRequests.route) { IncomingRequestsScreen() }
            }
        }
    }
}

// Helper function to navigate and close drawer
fun navigateToScreen(
    navController: NavController,
    route: String,
    drawerState: androidx.compose.material3.DrawerState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    navController.navigate(route) {
        // Pop up to the start destination to avoid building up a large stack of destinations
        popUpTo(navController.graph.startDestinationId)
        launchSingleTop = true
    }
    scope.launch { drawerState.close() }
}