package com.example.travelmates_pamn.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
                label = { Text("Current Location") }
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
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            MultiLineTextBoxForProfile(
                value = bio,
                onValueChange = {bio = it},
                label = { Text("About Me") }
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TagDropdownMenu(
    availableTags: List<String>,
    selectedTags: List<String>,
    onTagSelect: (String) -> Unit,
    onTagRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Dropdown button
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = "Select Tags",
                onValueChange = {},
                readOnly = true,
                label = { Text("Tags") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

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
                    onClick = { onTagRemove(tag) },
                    label = { Text(tag) }
                )
            }
        }
    }
}