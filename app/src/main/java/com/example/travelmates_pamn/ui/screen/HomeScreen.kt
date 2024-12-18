package com.example.travelmates_pamn.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.travelmates_pamn.R
import com.example.travelmates_pamn.Screen
import com.example.travelmates_pamn.calculateDistance
import com.example.travelmates_pamn.ui.HomeViewModel
import com.google.firebase.firestore.GeoPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Use a state to track permission
    var permissionGranted by remember { mutableStateOf(false) }
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
    }

    // Check and request permissions
    LaunchedEffect(key1 = true) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                permissionGranted = true
            }

            else -> {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // Fetch nearby users
    LaunchedEffect(permissionGranted, uiState.authUser?.id) {
        if (permissionGranted && uiState.authUser?.id != null) {
            viewModel.fetchNearbyUsers() // Move this logic to ViewModel
        }
    }



    Scaffold(
        bottomBar = {
            Button(
                onClick = {
                    viewModel.navigateToPeopleInTown(
                        navController = navController,
                        drawerState = drawerState,
                        scope = scope
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Show more People in Town",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "View More",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    ) { innerPadding ->
        // Replace nested scrollables with a single scrollable container
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Use a single ScrollableColumn instead of nesting scrollable components
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // Greeting
                if (uiState.authUser != null) {
                    Text(
                        text = "Hi, ${uiState.authUser?.name}!",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    Text(
                        text = "Hi!",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                // OpenStreetMap section
                if (uiState.authUser?.location != null && uiState.authUser?.location != GeoPoint(0.0, 0.0)) {
                    Text(
                        text = "You are here",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        OpenStreetMapView(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RectangleShape),
                            latitude = uiState.authUser?.location!!.latitude,
                            longitude = uiState.authUser?.location!!.longitude
                        )

                        IconButton(
                            onClick = { viewModel.updateLocationMap(context) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .background(MaterialTheme.colorScheme.background, shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reload Location"
                            )
                        }
                    }
                }

                // People in Town section
                Text(
                    text = "Find People Close to You",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Conditional content for nearby users
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.authUser?.location == null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Could not get your location")
                        }
                    }
                    uiState.nearbyUsers.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Nobody in your area :(")
                        }
                    }
                    else -> {
                        // Nearby users grid
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.nearbyUsers.forEach { user ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(120.dp)
                                        .clickable {
                                            navController.navigate(Screen.OtherProfile.createRoute(user.id))
                                        }
                                ) {
                                    if (user.photoUrl.isNotEmpty()) {
                                        AsyncImage(
                                            model = user.photoUrl,
                                            contentDescription = "Profile picture of ${user.name}",
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
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

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = user.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${calculateDistance(uiState.authUser!!.location, user.location).toInt()} km",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun OpenStreetMapView(
    modifier: Modifier = Modifier,
    latitude: Double,
    longitude: Double
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapView by remember { mutableStateOf<MapView?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                // Configure the map
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                // Set initial location and zoom
                val mapController = controller
                mapController.setZoom(12.0)
                val startPoint = org.osmdroid.util.GeoPoint(latitude, longitude)
                mapController.setCenter(startPoint)

                // Add a marker
                val marker = Marker(this)
                marker.position = startPoint
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.icon = ContextCompat.getDrawable(context, R.drawable.map_marker)
                marker.title = "Current Location"
                overlays.add(marker)
            }
        },
        update = { view ->
            mapView = view


            // Dynamically update the map with new location
            val startPoint = org.osmdroid.util.GeoPoint(latitude, longitude)
            view.controller.setCenter(startPoint)

            // Update or add marker
            val marker = Marker(view).apply {
                position = startPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = ContextCompat.getDrawable(view.context, R.drawable.map_marker)
                title = "Current Location"
            }
            view.overlays.clear() // Clear old markers
            view.overlays.add(marker) // Add updated marker
            view.invalidate() // Refresh the map
        }
    )

    // Lifecycle management
    DisposableEffect(lifecycleOwner) {
        val lifecycleEventObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleEventObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleEventObserver)
        }
    }
}