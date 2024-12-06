package com.example.travelmates_pamn.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.travelmates_pamn.R
import com.example.travelmates_pamn.ui.ProfileViewModel

const val textBoxWidth = 0.75f

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editingState by viewModel.editingState.collectAsStateWithLifecycle()
    val warningState by viewModel.warningState.collectAsStateWithLifecycle()
    val allTags = LocalContext.current.resources.getStringArray(R.array.available_interests).toList()

    // Photo picker launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.updateEditingField(photoUri = it)
        }
    }

    // Warning dialog
    warningState?.let { warning ->
        AlertDialog(
            onDismissRequest = { /* Handled by ViewModel */ },
            title = { Text("Warning") },
            text = { Text(warning) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearWarning() }) {
                    Text("OK")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Profile Picture with Click-to-Edit in Edit Mode
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape
                    )
                    .clickable(enabled = uiState.isEditing) {
                        if (uiState.isEditing) {
                            launcher.launch("image/*")
                        }
                    }
            ) {
                // Prioritize editing state photo, then UI state photo, then default
                val photoModel = when {
                    uiState.isEditing && editingState.photoUri != null -> editingState.photoUri
                    uiState.photoUrl.isNotEmpty() -> uiState.photoUrl
                    else -> R.drawable.default_profile
                }

                AsyncImage(
                    model = photoModel,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                // Overlay for edit mode
                if (uiState.isEditing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile Picture",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name
            TextBoxForProfile(
                value = if (uiState.isEditing) editingState.name else uiState.name,
                onValueChange = {
                    viewModel.updateEditingField(name = it)
                },
                label = { Text("Name") },
                isEditing = uiState.isEditing,
                modifier = Modifier.fillMaxWidth(textBoxWidth)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Birthday/Age
            if (uiState.isEditing) {
                BirthdayInput(
                    birthday = editingState.birthday,
                    onBirthdayChange = {
                        viewModel.updateEditingField(birthday = it)
                    },
                    isEditing = true,
                    modifier = Modifier.fillMaxWidth(textBoxWidth)
                )
            } else {
                TextBoxForProfile(
                    value = uiState.age,
                    onValueChange = {},
                    label = { Text("Age") },
                    isEditing = false,
                    modifier = Modifier.fillMaxWidth(textBoxWidth)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hometown
            TextBoxForProfile(
                value = if (uiState.isEditing) editingState.hometown else uiState.hometown,
                onValueChange = {
                    viewModel.updateEditingField(hometown = it)
                },
                label = { Text("Hometown") },
                isEditing = uiState.isEditing,
                modifier = Modifier.fillMaxWidth(textBoxWidth)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Current Location
            TextBoxForProfile(
                value = if (uiState.isEditing) editingState.location else uiState.location,
                onValueChange = {
                    viewModel.updateEditingField(location = it)
                },
                label = { Text("Current Location") },
                isEditing = uiState.isEditing,
                modifier = Modifier.fillMaxWidth(textBoxWidth)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Interests Tags
            TagDropdownMenu(
                availableTags = allTags,
                selectedTags = if (uiState.isEditing) editingState.selectedTags else uiState.selectedTags,
                onTagSelect = { tag ->
                    val currentTags = if (uiState.isEditing)
                        editingState.selectedTags
                    else uiState.selectedTags

                    val newTags = if (tag !in currentTags) {
                        if (currentTags.size >= 5) {
                            // Trigger warning if trying to add more than 5 tags
                            viewModel.updateEditingField(tags = currentTags)
                            return@TagDropdownMenu
                        }
                        currentTags + tag
                    } else {
                        currentTags
                    }

                    viewModel.updateEditingField(tags = newTags)
                },
                onTagRemove = { tag ->
                    val currentTags = if (uiState.isEditing)
                        editingState.selectedTags
                    else uiState.selectedTags

                    val newTags = currentTags.filter { it != tag }

                    viewModel.updateEditingField(tags = newTags)
                },
                isEditing = uiState.isEditing,
                modifier = Modifier.fillMaxWidth(textBoxWidth)
            )


            Spacer(modifier = Modifier.height(8.dp))

            // Bio
            MultiLineTextBoxForProfile(
                value = if (uiState.isEditing) editingState.bio else uiState.bio,
                onValueChange = {
                    viewModel.updateEditingField(bio = it)
                },
                label = { Text("About Me") },
                isEditing = uiState.isEditing,
                modifier = Modifier.fillMaxWidth(textBoxWidth)
            )
        }

        // Edit/Save button
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                modifier = Modifier
                    .padding(16.dp)
                    .size(56.dp),
                onClick = {
                    if (uiState.isEditing) {
                        // Save profile when exiting edit mode
                        viewModel.saveProfile()
                    } else {
                        // Enter edit mode
                        viewModel.toggleEditMode()
                    }
                }
            ) {
                Icon(
                    imageVector = if (uiState.isEditing) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = if (uiState.isEditing) "Save Profile" else "Edit Profile"
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextBoxForProfile(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    isEditing: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        singleLine = true,
        readOnly = !isEditing,
        enabled = isEditing,
        colors = if (isEditing) ExposedDropdownMenuDefaults.textFieldColors()
                 else TextFieldDefaults.colors(
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiLineTextBoxForProfile(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    maxLines: Int = 4,  // Default to 4 lines, you can adjust this
    isEditing: Boolean = false,
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
        shape = RoundedCornerShape(8.dp),
        readOnly = !isEditing,
        enabled = isEditing,
        colors = if (isEditing) ExposedDropdownMenuDefaults.textFieldColors()
        else TextFieldDefaults.colors(
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TagDropdownMenu(
    availableTags: List<String>,
    selectedTags: List<String>,
    onTagSelect: (String) -> Unit,
    onTagRemove: (String) -> Unit,
    isEditing: Boolean = false,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Dropdown button (only interactive if editing)
        ExposedDropdownMenuBox(
            expanded = expanded && isEditing,
            onExpandedChange = {
                if (isEditing) expanded = !expanded
            },
            //shape = RoundedCornerShape(8.dp),
            //modifier = Modifier.fillMaxWidth()
        ) {
            if (isEditing) {
                TextField(
                    value = "Select at most 5 Tags",
                    onValueChange = {},
                    readOnly = true,
                    enabled = true,
                    label = { Text("Tags") },
                    trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availableTags.forEach { tag ->
                    DropdownMenuItem(
                        text = { Text(tag) },
                        onClick = {
                            onTagSelect(tag)
                            expanded = false
                        },
                        trailingIcon = {
                            Checkbox(
                                checked = tag in selectedTags,
                                onCheckedChange = {
                                    if (it) onTagSelect(tag)
                                    else onTagRemove(tag)
                                }
                            )
                        }
                    )
                }
            }
        }

        // Selected tags display
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedTags.forEach { tag ->
                FilterChip(
                    selected = true,
                    onClick = { if (isEditing) onTagRemove(tag) },
                    label = { Text(tag) },
                    trailingIcon = if (isEditing) {
                        {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove tag"
                            )
                        }
                    } else null
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayInput(
    birthday: String,
    onBirthdayChange: (String) -> Unit,
    isEditing: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = birthday,
        onValueChange = onBirthdayChange,
        label = { Text("Birthday") },
        placeholder = { Text("YYYY-MM-DD") },
        singleLine = true,
        readOnly = !isEditing,
        enabled = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
            .fillMaxWidth(0.75f),
        colors = if (isEditing)
            ExposedDropdownMenuDefaults.textFieldColors()
        else
            TextFieldDefaults.colors(
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
    )
}

/**
fun calculateAge(birthdayString: String): Int {
    try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val birthday = LocalDate.parse(birthdayString, formatter)
        val today = LocalDate.now()
        return Period.between(birthday, today).years
    } catch (e: Exception) {
        return 0 // Return 0 if date is invalid
    }
}
        */