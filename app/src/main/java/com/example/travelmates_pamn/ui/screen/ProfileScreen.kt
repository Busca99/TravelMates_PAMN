package com.example.travelmates_pamn.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.travelmates_pamn.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

const val textBoxWidth = 0.75f

@Composable
fun ProfileScreen() {
    // ToDo: add exchange with login
    // get birthday
    val allTags = listOf("Travel", "Food", "Technology", "Sports", "Music")

    var name by remember { mutableStateOf("Lisa Meyer") }
    var age by remember { mutableStateOf("date") }
    var hometown by remember { mutableStateOf("Ohio") }
    var location by remember { mutableStateOf("Las Palmas, Spain") }
    var bio by remember { mutableStateOf("I like traveling!") }
    var selectedTags by remember { mutableStateOf(listOf<String>()) }
    var birthday by remember { mutableStateOf("2001-01-01") }

    var isEditing by remember { mutableStateOf(false) }


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
            // Profile Picture
            Image(
                painter = painterResource(id = R.drawable.default_profile), // Replace with your drawable
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Name
            TextBoxForProfile(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                isEditing = isEditing,
                modifier = Modifier.fillMaxWidth(textBoxWidth)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Age
            if (isEditing) {
                BirthdayInput(
                    birthday = birthday,
                    onBirthdayChange = {birthday = it},
                    isEditing = isEditing,
                    modifier = Modifier.fillMaxWidth(textBoxWidth)
                )
            } else {
                TextBoxForProfile(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age") },
                    isEditing = isEditing,
                    modifier = Modifier.fillMaxWidth(textBoxWidth)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Home Town
            TextBoxForProfile(
                value = hometown,
                onValueChange = { hometown = it },
                label = { Text("Hometown") },
                isEditing = isEditing,
                modifier = Modifier.fillMaxWidth(textBoxWidth)
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextBoxForProfile(
                value = location,
                onValueChange = {location = it},
                label = { Text("Current Location") },
                isEditing = isEditing,
                modifier = Modifier.fillMaxWidth(textBoxWidth)
            )

            Spacer(modifier = Modifier.height(8.dp))

            TagDropdownMenu(
                availableTags = allTags,
                selectedTags = selectedTags,
                onTagSelect = { tag ->
                    if (tag !in selectedTags) {
                        selectedTags = selectedTags + tag
                    }
                },
                onTagRemove = { tag ->
                    selectedTags = selectedTags.filter { it != tag }
                },
                isEditing = isEditing,
                modifier = Modifier
                    //.align(Alignment.CenterHorizontally)
                    .fillMaxWidth(textBoxWidth)
            )

            Spacer(modifier = Modifier.height(8.dp))

            MultiLineTextBoxForProfile(
                value = bio,
                onValueChange = {bio = it},
                label = { Text("About Me") },
                isEditing = isEditing,
                modifier = Modifier.fillMaxWidth(textBoxWidth)
            )
        }

        // edit button
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            // Existing profile screen content...

            FloatingActionButton(
                modifier = Modifier
                    .padding(16.dp)
                    .size(56.dp),
                onClick = { isEditing = !isEditing }
            ) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = if (isEditing) "Save Profile" else "Edit Profile"
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
                    value = "Select Tags",
                    onValueChange = {},
                    readOnly = true,
                    enabled = isEditing,
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