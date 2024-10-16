package com.android.voyageur.ui.overview

import android.icu.util.GregorianCalendar
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.R
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripType
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripScreen(
    tripsViewModel: TripsViewModel = viewModel(factory = TripsViewModel.Factory),
    navigationActions: NavigationActions
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var creator by remember { mutableStateOf("") }
    var participants by remember { mutableStateOf("") }
    var locations by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var tripType by remember { mutableStateOf(TripType.BUSINESS) }
    var imageUri by remember { mutableStateOf<Uri?>(Uri.EMPTY) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> uri?.let { imageUri = it } }
    )

    val context = LocalContext.current
    val imageId = R.drawable.default_trip_image

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create a New Trip") },
                navigationIcon = {
                    IconButton(onClick = { navigationActions.goBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (imageUri != Uri.EMPTY) {
                    Image(
                        painter = rememberAsyncImagePainter(model = imageUri),
                        contentDescription = "Selected image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(5.dp))
                    )
                } else {
                    Image(
                        painter = painterResource(id = imageId),
                        contentDescription = "Default trip image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(5.dp))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select Image from Gallery")
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Trip") },
                    placeholder = { Text("Name the trip") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Describe the trip") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = creator,
                    onValueChange = { creator = it },
                    label = { Text("Creator") },
                    placeholder = { Text("Assign a creator") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = participants,
                    onValueChange = { participants = it },
                    label = { Text("Participants") },
                    placeholder = { Text("Name the participants, comma-separated") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = locations,
                    onValueChange = { locations = it },
                    label = { Text("Locations") },
                    placeholder = { Text("Name the locations, comma-separated") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start Date (DD/MM/YYYY)") },
                    placeholder = { Text("19/01/1975") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("End Date (DD/MM/YYYY)") },
                    placeholder = { Text("19/01/1975") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    RadioButton(
                        selected = tripType == TripType.BUSINESS,
                        onClick = { tripType = TripType.BUSINESS }
                    )
                    Text("Business")
                    RadioButton(
                        selected = tripType == TripType.TOURISM,
                        onClick = { tripType = TripType.TOURISM }
                    )
                    Text("Tourism")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    val calendar = GregorianCalendar()
                    val startParts = startDate.split("/")
                    val endParts = endDate.split("/")
                    if (startParts.size == 3 && endParts.size == 3) {
                        try {
                            calendar.set(
                                startParts[2].toInt(), startParts[1].toInt() - 1, startParts[0].toInt()
                            )
                            val startTimestamp = Timestamp(calendar.time)
                            calendar.set(
                                endParts[2].toInt(), endParts[1].toInt() - 1, endParts[0].toInt()
                            )
                            val endTimestamp = Timestamp(calendar.time)

                            val trip = Trip(
                                id = tripsViewModel.getNewTripId(),
                                creator = creator,
                                participants = participants.split(",").map { it.trim() },
                                description = description,
                                name = name,
                                locations = locations.split(";").map { locationString ->
                                    val parts = locationString.split(",").map { it.trim() }
                                    if (parts.size >= 2) {
                                        Location(
                                            country = parts[0],
                                            city = parts[1],
                                            county = parts.getOrNull(2),
                                            zip = parts.getOrNull(3)
                                        )
                                    } else {
                                        Location(
                                            country = "Unknown",
                                            city = "Unknown",
                                            county = null,
                                            zip = null
                                        )
                                    }
                                },
                                startDate = startTimestamp,
                                endDate = endTimestamp,
                                activities = listOf(),
                                type = tripType,
                                imageUri = imageUri?.toString() // Save the imageUri as a string
                            )
                            tripsViewModel.createTrip(trip)
                            navigationActions.goBack()
                        } catch (e: NumberFormatException) {
                            Toast.makeText(context, "Invalid date format", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Please enter valid start/end dates",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                enabled = name.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Save Trip")
            }
        }
    }
}