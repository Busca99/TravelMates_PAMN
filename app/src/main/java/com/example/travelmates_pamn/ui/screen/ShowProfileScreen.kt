package com.example.travelmates_pamn.ui.screen

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.travelmates_pamn.ui.ShowProfileViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShowProfileScreen(
    userId: String = "",
    viewModel: ShowProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // Collect the UI state
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Trigger user fetch when the screen is first loaded
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.fetchUser(userId)
        }
    }

    // Handle different UI states
    when {
        uiState.isLoading -> {
            // Show loading indicator
            //CircularProgressIndicator()
        }
        uiState.error != null -> {
            // Show error message
            Text("Error: ${uiState.error}")
        }
        uiState.user != null -> {

            val user = uiState.user!!
            println(user)
            println(user.name)
            // Your profile UI rendering here

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Image Placeholder
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    // You can add an actual image here using AsyncImage from Coil
                    if(user.photoUrl.isEmpty() or (user.photoUrl == "")) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = user.name.first().toString(),
                                style = MaterialTheme.typography.headlineLarge
                            )
                        }
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(user.photoUrl)
                                .crossfade(true) // Optional: Enables a crossfade animation
                                .build(),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(), // Ensures the image fills the surface
                            contentScale = ContentScale.Crop // Ensures the image fits nicely within the circle
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                // Age and Location
                Text(
                    text = "${user.age} • ${user.hometown}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (uiState.isFriend) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = user.phoneNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))


                // Tags
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        //.heightIn(max = 120.dp)  // Constrains maximum height
                        .padding(horizontal = 8.dp,
                            vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    //maxItemsInEachRow = Int.MAX_VALUE
                ) {
                    user.interests.forEach { tag ->
                        AssistChip(
                            onClick = { /* Handle tag click */ },
                            label = { Text(tag) },
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bio
                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (!uiState.isFriend) {
                    Button(
                        onClick = { viewModel.sendFriendRequest() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = !uiState.friendRequestSent
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            if (!uiState.friendRequestSent) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Send Friend Request",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Send Friend Request",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Friend Request Sent",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Friend Request already sent!",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }


}


// Preview
// doesn't work
@Composable
@Preview(showBackground = true)
fun ShowProfileScreenPreview() {
    val userId = "7DO7lUDSipd7XUSGyXEtI0lRmcj2"

    ShowProfileScreen(userId = userId)
}

