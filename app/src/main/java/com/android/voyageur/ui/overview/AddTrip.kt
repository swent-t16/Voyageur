package com.android.voyageur.ui.overview

import android.annotation.SuppressLint
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
import androidx.compose.ui.platform.testTag
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
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripScreen(
    tripsViewModel: TripsViewModel = viewModel(factory = TripsViewModel.Factory),
    navigationActions: NavigationActions
) {
  var name by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var participants by remember { mutableStateOf("") }
  var locations by remember { mutableStateOf("") }
  var startDate by remember { mutableStateOf("") }
  var endDate by remember { mutableStateOf("") }
  var tripType by remember { mutableStateOf(TripType.BUSINESS) }
  var imageUri by remember { mutableStateOf("") }

  val context = LocalContext.current
  val imageId = R.drawable.default_trip_image

  val galleryLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.GetContent(),
          onResult = { uri -> uri?.let { imageUri = it.toString() } })

  fun createTripWithImage(imageUrl: String) {
    val calendar = GregorianCalendar()
    val startParts = startDate.split("/")
    val endParts = endDate.split("/")
    if (startParts.size == 3 && endParts.size == 3) {
      try {
        calendar.set(startParts[2].toInt(), startParts[1].toInt() - 1, startParts[0].toInt())
        val startTimestamp = Timestamp(calendar.time)
        calendar.set(endParts[2].toInt(), endParts[1].toInt() - 1, endParts[0].toInt())
        val endTimestamp = Timestamp(calendar.time)

        var participantList = emptyList<String>()
        if (participants.isNotEmpty()) {
          participantList = participants.split(",").map { it.trim() }.toList()
        }

        val trip =
            Trip(
                id = tripsViewModel.getNewTripId(),
                creator = Firebase.auth.uid.orEmpty(),
                participants = participantList,
                description = description,
                name = name,
                locations =
                    locations.split(";").map { locationString ->
                      val parts = locationString.split(",").map { it.trim() }
                      if (parts.size >= 2) {
                        Location(
                            country = parts[0],
                            city = parts[1],
                            county = parts.getOrNull(2),
                            zip = parts.getOrNull(3))
                      } else {
                        Location(country = "Unknown", city = "Unknown", county = null, zip = null)
                      }
                    },
                startDate = startTimestamp,
                endDate = endTimestamp,
                activities = listOf(),
                type = tripType,
                imageUri = imageUrl)
        tripsViewModel.createTrip(trip)
        navigationActions.goBack()
      } catch (e: NumberFormatException) {
        Toast.makeText(context, "Invalid date format", Toast.LENGTH_SHORT).show()
      }
    } else {
      Toast.makeText(context, "Please enter valid start/end dates", Toast.LENGTH_SHORT).show()
    }
  }

  Scaffold(
      modifier = Modifier.testTag("addTrip"),
      topBar = {
        TopAppBar(
            title = { Text("Create a New Trip", Modifier.testTag("addTripTitle")) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() }, Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                    )
                  }
            })
      }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          Column(
              modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (imageUri != "") {
                  Image(
                      painter = rememberAsyncImagePainter(model = imageUri),
                      contentDescription = "Selected image",
                      contentScale = ContentScale.Crop,
                      modifier =
                          Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(5.dp)))
                } else {
                  Image(
                      painter = painterResource(id = imageId),
                      contentDescription = "Default trip image",
                      contentScale = ContentScale.Crop,
                      modifier =
                          Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(5.dp)))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()) {
                      Text("Select Image from Gallery")

                    }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Trip") },
                    placeholder = { Text("Name the trip") },
                    modifier = Modifier.fillMaxWidth().testTag("inputTripTitle"))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Describe the trip") },
                    modifier = Modifier.fillMaxWidth().testTag("inputTripDescription"))

                OutlinedTextField(
                    value = participants,
                    onValueChange = { participants = it },
                    label = { Text("Participants") },
                    placeholder = { Text("Name the participants, comma-separated") },
                    modifier = Modifier.fillMaxWidth().testTag("inputTripParticipants"))

                OutlinedTextField(
                    value = locations,
                    onValueChange = { locations = it },
                    label = { Text("Locations") },
                    placeholder = { Text("Name the locations, comma-separated") },
                    modifier = Modifier.fillMaxWidth().testTag("inputTripLocations"))

                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start Date (DD/MM/YYYY)") },
                    placeholder = { Text("19/01/1975") },
                    modifier = Modifier.fillMaxWidth().testTag("inputStartDate"))

                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("End Date (DD/MM/YYYY)") },
                    placeholder = { Text("19/01/1975") },
                    modifier = Modifier.fillMaxWidth().testTag("inputEndDate"))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                      RadioButton(
                          onClick = { tripType = TripType.BUSINESS },
                          selected = tripType == TripType.BUSINESS,
                          modifier = Modifier.testTag("tripTypeBusiness"))
                      Text("Business")
                      RadioButton(
                          onClick = { tripType = TripType.TOURISM },
                          selected = tripType == TripType.TOURISM,
                          modifier = Modifier.testTag("tripTypeTourism"))
                      Text("Tourism")
                    }

                Spacer(modifier = Modifier.height(16.dp))
              }

          Button(
              onClick = {
                if (imageUri.isNotBlank()) {
                  val imageUriParsed = Uri.parse(imageUri)
                  tripsViewModel.uploadImageToFirebase(
                      uri = imageUriParsed,
                      onSuccess = { downloadUrl -> createTripWithImage(downloadUrl) },
                      onFailure = { exception ->
                        Toast.makeText(
                                context,
                                "Failed to upload image: ${exception.message}",
                                Toast.LENGTH_SHORT)
                            .show()
                      })
                } else {
                  createTripWithImage("")
                }
              },
              enabled = name.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank(),
              modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("tripSave")) {
                Text("Save Trip")
              }
        }
      }
}
