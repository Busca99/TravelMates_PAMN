package com.example.travelmates_pamn

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.travelmates_pamn.model.User
import com.example.travelmates_pamn.ui.screen.*
import com.example.travelmates_pamn.ui.theme.TravelMates_PAMNTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.*
import android.app.Activity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage


class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Permessi ottenuti, aggiorna la posizione
                updateUserLocation()
            }
            else -> {
                Toast.makeText(this, "Location permissions are required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializza il client della posizione
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Verifica se l'utente è autenticato
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            // Se non è autenticato, vai alla schermata di login
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        // Se l'utente è autenticato, richiedi i permessi di posizione
        requestLocationPermissions()
        startApp()
    }

    private fun requestLocationPermissions() {
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    @SuppressLint("MissingPermission")
    private fun updateUserLocation() {
        Log.d("Location", "Iniziando aggiornamento posizione...")

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    Log.d("Location", "Posizione ottenuta: ${location.latitude}, ${location.longitude}")

                    val db = FirebaseFirestore.getInstance()
                    val geoPoint = GeoPoint(location.latitude, location.longitude)

                    Log.d("Location", "Aggiornamento database per utente: $currentUserId")

                    // Cerca il documento basato sul campo "id" all'interno dei documenti
                    db.collection("users")
                        .whereEqualTo("id", currentUserId)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                // Documento trovato
                                val document = querySnapshot.documents.first()
                                db.collection("users")
                                    .document(document.id)
                                    .update("location", geoPoint)
                                    .addOnSuccessListener {
                                        Log.d("Location", "Posizione aggiornata con successo")
                                        Toast.makeText(this, "Location updated successfully", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Location", "Errore nell'aggiornamento: ${e.message}")
                                        Toast.makeText(this, "Error updating location", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                // Documento non trovato, crealo
                                Log.d("Location", "Utente non presente.")
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("Location", "Errore nella query: ${e.message}")
                            Toast.makeText(this, "Error querying user", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.e("Location", "Posizione null")
                    Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Location", "Errore nell'ottenere la posizione: ${e.message}")
                Toast.makeText(this, "Error getting location", Toast.LENGTH_SHORT).show()
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
    object MyProfile : Screen("profile")
    object Friends : Screen("friends")
    object IncomingRequests : Screen("incoming_requests")
    object OtherProfile : Screen("otherProfile/{userId}") {
        fun createRoute(userId: String) = "OtherProfile/$userId"
    }
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
fun PeopleInTownScreen(navController: NavController) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        try {
            Log.d("PeopleInTown", "Iniziando recupero posizione utente corrente...")

            // Cerca l'utente corrente
            val userQuery = db.collectionGroup("users")
                .whereEqualTo("id", currentUserId)
                .get()
                .await()

            Log.d("PeopleInTown", "Query utente corrente eseguita")

            if (!userQuery.isEmpty) {
                val currentUser = userQuery.documents[0].toObject(User::class.java)
                currentLocation = currentUser?.location
                Log.d("PeopleInTown", "Location trovata: ${currentLocation?.latitude}, ${currentLocation?.longitude}")

                if (currentLocation != null) {
                    // Cerca gli altri utenti
                    val allUsersQuery = db.collectionGroup("users")
                        .whereNotEqualTo("id", currentUserId)
                        .get()
                        .await()

                    users = allUsersQuery.documents
                        .mapNotNull { it.toObject(User::class.java) }
                        .filter { user ->
                            calculateDistance(currentLocation!!, user.location) <= 30.0
                        }
                        .sortedBy { user ->
                            calculateDistance(currentLocation!!, user.location)
                        }

                    Log.d("PeopleInTown", "Trovati ${users.size} utenti nelle vicinanze")
                }
            } else {
                Log.e("PeopleInTown", "Utente corrente non trovato")
            }
        } catch (e: Exception) {
            Log.e("PeopleInTown", "Errore generale: ${e.message}")
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (currentLocation == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Could not get your location")
            }
        }
    } else if (users.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("There's nobody in your area ):")
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
                            navController.navigate(Screen.OtherProfile.createRoute(user.id))
                        }
                ) {
                    if (user.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = "Profile picture of ${user.name}",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.name.firstOrNull()?.toString() ?: "",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }

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
                            text = "Distance: ${calculateDistance(currentLocation!!, user.location).toInt()} km",
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
fun FriendsScreen() {
    var friends by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

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
    } else if (friends.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("You still don't have friends!")
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
                    if (friend.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = friend.photoUrl,
                            contentDescription = "Profile picture of ${friend.name}",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Fallback se non c'è foto profilo
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = friend.name.firstOrNull()?.toString() ?: "",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }

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
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

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
                        if (sender.photoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = sender.photoUrl,
                                contentDescription = "Profile picture of ${sender.name}",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sender.name.firstOrNull()?.toString() ?: "",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        }

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
    val context = LocalContext.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    Column {
                        Text("Menu", modifier = Modifier.padding(bottom = 16.dp))
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
                            selected = currentRoute == Screen.MyProfile.route,
                            onClick = {
                                navigateToScreen(navController, Screen.MyProfile.route, drawerState, scope)
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

                    // Logout button at the bottom
                    Button(
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            scope.launch {
                                drawerState.close()
                            }
                            context.startActivity(Intent(context, AuthActivity::class.java))
                            (context as? Activity)?.finish()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        Text("Logout")
                    }
                }
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
                composable(Screen.PeopleInTown.route) { PeopleInTownScreen(navController) }
                composable(Screen.MyProfile.route) { ProfileScreen() }
                composable(Screen.Friends.route) { FriendsScreen() }
                composable(Screen.IncomingRequests.route) { IncomingRequestsScreen() }
                composable(
                    route = Screen.OtherProfile.route,
                    arguments = listOf(navArgument("userId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                    ShowProfileScreen(userId = userId,
                        
                    )
                }
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