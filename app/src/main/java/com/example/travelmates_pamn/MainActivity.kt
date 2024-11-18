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


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

@Composable
fun PeopleInTownScreen() {
    val people = listOf(
        "Lisa Perez, 29, from Spain",
        "Lisa Perez, 29, from Spain",
        "Lisa Perez, 29, from Spain",
        "Lisa Perez, 29, from Spain",
        "Lisa Perez, 29, from Spain",
        "Lisa Perez, 29, from Spain",
        "Lisa Perez, 29, from Spain",
        "Lisa Perez, 29, from Spain",
        "Lisa Perez, 29, from Spain",
        "Lisa Perez, 29, from Spain"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(people) { person ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clickable {
                        // Azione quando la riga è cliccata
                    }
            ) {
                // Green circle
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(color = Color.Green, shape = CircleShape)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Text details
                Column {
                    Text(
                        text = "Lisa Perez",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "29, from Spain",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}


@Composable
fun ProfileScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("My Profile Screen")
    }
}

@Composable
fun FriendsScreen() {
    val people = listOf(
        "Lisa Perez, 29, from Spain",
        "Lisa Perez, 29, from Spain",
        "Lisa Perez, 29, from Spain",
        "Lisa Perez, 29, from Spain",
        "Lisa Perez, 29, from Spain",
        "Lisa Perez, 29, from Spain",
        "Lisa Perez, 29, from Spain",
        "Lisa Perez, 29, from Spain",
        "Lisa Perez, 29, from Spain",
        "Lisa Perez, 29, from Spain"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(people) { person ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clickable {
                        // Azione quando la riga è cliccata
                    }
            ) {
                // Green circle
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(color = Color.Green, shape = CircleShape)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Text details
                Column {
                    Text(
                        text = "Lisa Perez",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "29, from Spain",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun IncomingRequestsScreen() {
    val people = listOf(
        "Lisa Perez, 29, from Spain",
        "Lisa Perez, 29, from Spain",
        "Lisa Perez, 29, from Spain",
        "Lisa Perez, 29, from Spain"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(people) { person ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clickable {
                        // Azione quando la riga è cliccata
                    }
            ) {
                // Cerchio verde
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(color = Color.Green, shape = CircleShape)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Text details
                Column {
                    Text(
                        text = "Lisa Perez",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "29, from Spain",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                // Icone dei bottoni
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bottone con la X
                    IconButton(onClick = { /* Azione per X */ }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Red
                        )
                    }

                    // Bottone con il segno di spunta
                    IconButton(onClick = { /* Azione per il tick */ }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Check",
                            tint = Color.Green
                        )
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